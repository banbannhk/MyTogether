package org.th.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.th.config.SupabaseConfig;
import org.th.exception.SupabaseStorageException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing file uploads to Supabase Storage
 */
@Service
@lombok.extern.slf4j.Slf4j
public class SupabaseStorageService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif");

    @Autowired
    private SupabaseConfig supabaseConfig;

    @Autowired
    private OkHttpClient okHttpClient;

    private final Gson gson = new Gson();

    /**
     * Upload an image to Supabase Storage
     *
     * @param file   The multipart file to upload
     * @param folder The folder path in the bucket (e.g., "shops/123")
     * @return The public URL of the uploaded file
     */
    public String uploadImage(MultipartFile file, String folder) {
        validateFile(file);

        String fileName = generateFileName(file.getOriginalFilename());
        String filePath = folder + "/" + fileName;

        try {
            // Create request body
            RequestBody requestBody = RequestBody.create(
                    file.getBytes(),
                    MediaType.parse(file.getContentType()));

            // Build the upload URL
            String uploadUrl = String.format("%s/storage/v1/object/%s/%s",
                    supabaseConfig.getSupabaseUrl(),
                    supabaseConfig.getSupabaseBucket(),
                    filePath);

            // Create request
            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .addHeader("Authorization", "Bearer " + supabaseConfig.getSupabaseKey())
                    .addHeader("Content-Type", file.getContentType())
                    .post(requestBody)
                    .build();

            // Execute request
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    log.error("Failed to upload file to Supabase: {} - {}", response.code(), errorBody);
                    throw new SupabaseStorageException("Failed to upload file: " + errorBody);
                }

                log.info("Successfully uploaded file to Supabase: {}", filePath);
                return getPublicUrl(filePath);
            }

        } catch (IOException e) {
            log.error("Error uploading file to Supabase", e);
            throw new SupabaseStorageException("Error uploading file", e);
        }
    }

    /**
     * Get the public URL for a file in Supabase Storage
     *
     * @param filePath The path to the file in the bucket
     * @return The public URL
     */
    public String getPublicUrl(String filePath) {
        return String.format("%s/storage/v1/object/public/%s/%s",
                supabaseConfig.getSupabaseUrl(),
                supabaseConfig.getSupabaseBucket(),
                filePath);
    }

    /**
     * Delete a file from Supabase Storage
     *
     * @param filePath The path to the file in the bucket
     */
    public void deleteImage(String filePath) {
        try {
            String deleteUrl = String.format("%s/storage/v1/object/%s/%s",
                    supabaseConfig.getSupabaseUrl(),
                    supabaseConfig.getSupabaseBucket(),
                    filePath);

            Request request = new Request.Builder()
                    .url(deleteUrl)
                    .addHeader("Authorization", "Bearer " + supabaseConfig.getSupabaseKey())
                    .delete()
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    log.error("Failed to delete file from Supabase: {} - {}", response.code(), errorBody);
                    throw new SupabaseStorageException("Failed to delete file: " + errorBody);
                }

                log.info("Successfully deleted file from Supabase: {}", filePath);
            }

        } catch (IOException e) {
            log.error("Error deleting file from Supabase", e);
            throw new SupabaseStorageException("Error deleting file", e);
        }
    }

    /**
     * List all files in a folder
     *
     * @param folder The folder path
     * @return List of file paths
     */
    public List<String> listImages(String folder) {
        try {
            String listUrl = String.format("%s/storage/v1/object/list/%s",
                    supabaseConfig.getSupabaseUrl(),
                    supabaseConfig.getSupabaseBucket());

            // Create request body with folder prefix
            JsonObject requestJson = new JsonObject();
            requestJson.addProperty("prefix", folder);
            requestJson.addProperty("limit", 100);

            RequestBody requestBody = RequestBody.create(
                    requestJson.toString(),
                    MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url(listUrl)
                    .addHeader("Authorization", "Bearer " + supabaseConfig.getSupabaseKey())
                    .post(requestBody)
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    log.error("Failed to list files from Supabase: {} - {}", response.code(), errorBody);
                    throw new SupabaseStorageException("Failed to list files: " + errorBody);
                }

                String responseBody = response.body().string();
                JsonArray files = gson.fromJson(responseBody, JsonArray.class);

                List<String> filePaths = new ArrayList<>();
                files.forEach(element -> {
                    JsonObject fileObj = element.getAsJsonObject();
                    String name = fileObj.get("name").getAsString();
                    if (!name.endsWith("/")) { // Exclude folders
                        filePaths.add(folder + "/" + name);
                    }
                });

                return filePaths;
            }

        } catch (IOException e) {
            log.error("Error listing files from Supabase", e);
            throw new SupabaseStorageException("Error listing files", e);
        }
    }

    /**
     * Validate the uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new SupabaseStorageException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new SupabaseStorageException("File size exceeds maximum allowed size of 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new SupabaseStorageException("Invalid file type. Only images are allowed (JPEG, PNG, WebP, GIF)");
        }
    }

    /**
     * Generate a unique file name
     */
    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}

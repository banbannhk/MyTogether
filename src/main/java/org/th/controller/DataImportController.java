package org.th.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.th.dto.ApiResponse;
import org.th.service.ExcelImportService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/import")
@RequiredArgsConstructor
@Tag(name = "Admin - Data Import", description = "Excel data import APIs (Admin only)")
public class DataImportController {

    private final ExcelImportService excelImportService;

    /**
     * Import shops from Excel file
     */
    @PostMapping("/shops/excel")
    // @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Import shops from Excel", description = "Upload Excel file with shops, menu items, and operating hours")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importShopsFromExcel(
            @RequestParam("file") MultipartFile file) {

        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File is empty"));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".xlsx")) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File must be .xlsx format"));
        }

        try {
            ExcelImportService.ImportResult result = excelImportService.importShopsFromExcel(file);

            Map<String, Object> response = new HashMap<>();
            response.put("successCount", result.getSuccessCount());
            response.put("errorCount", result.getErrors().size());
            response.put("errors", result.getErrors());

            if (result.hasErrors()) {
                return ResponseEntity.ok(ApiResponse.success(
                        "Import completed with " + result.getSuccessCount() + " shops, " + result.getErrors().size()
                                + " errors",
                        response));
            }

            return ResponseEntity.ok(ApiResponse.success(
                    "Successfully imported " + result.getSuccessCount() + " shops",
                    response));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Import failed: " + e.getMessage()));
        }
    }

    /**
     * Import user activity from Excel file
     */
    @PostMapping("/activity/excel")
    @Operation(summary = "Import user activity from Excel", description = "Upload Excel file with user activities for testing")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importUserActivityFromExcel(
            @RequestParam("file") MultipartFile file) {

        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File is empty"));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".xlsx")) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File must be .xlsx format"));
        }

        try {
            ExcelImportService.ImportResult result = excelImportService.importUserActivityFromExcel(file);

            Map<String, Object> response = new HashMap<>();
            response.put("successCount", result.getSuccessCount());
            response.put("errorCount", result.getErrors().size());
            response.put("errors", result.getErrors());

            if (result.hasErrors()) {
                return ResponseEntity.ok(ApiResponse.success(
                        "Import completed with " + result.getSuccessCount() + " activities, "
                                + result.getErrors().size()
                                + " errors",
                        response));
            }

            return ResponseEntity.ok(ApiResponse.success(
                    "Successfully imported " + result.getSuccessCount() + " activities",
                    response));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Import failed: " + e.getMessage()));
        }
    }
}

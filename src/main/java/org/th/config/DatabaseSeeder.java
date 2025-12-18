package org.th.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.th.service.admin.ExcelImportService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder {

    private final ExcelImportService excelImportService;

    @EventListener(ApplicationReadyEvent.class)
    public void seedDatabase() {
        log.info("🌱 Starting database reset and seeding...");
        long start = System.currentTimeMillis();

        try {
            File seedFile = new File("database_seed.xlsx");

            if (!seedFile.exists()) {
                log.warn("⚠️ database_seed.xlsx not found. Skipping database seeding.");
                return;
            }

            // Create a MultipartFile wrapper for the seed file
            MultipartFile multipartFile = new MultipartFile() {
                @Override
                public String getName() {
                    return "database_seed.xlsx";
                }

                @Override
                public String getOriginalFilename() {
                    return "database_seed.xlsx";
                }

                @Override
                public String getContentType() {
                    return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                }

                @Override
                public boolean isEmpty() {
                    return seedFile.length() == 0;
                }

                @Override
                public long getSize() {
                    return seedFile.length();
                }

                @Override
                public byte[] getBytes() throws IOException {
                    return Files.readAllBytes(seedFile.toPath());
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    return new FileInputStream(seedFile);
                }

                @Override
                public void transferTo(File dest) throws IOException {
                    Files.copy(seedFile.toPath(), dest.toPath());
                }
            };

            // Reset and seed the database
            log.info("ℹ️ Database seeding is currently disabled explicitly.");
            // ExcelImportService.ImportResult result =
            // excelImportService.fullDatabaseReset(multipartFile);

            /*
             * if (result.hasErrors()) {
             * log.warn("⚠️ Seeding completed with errors.");
             * } else {
             * log.info("✅ Database seeded successfully!");
             * }
             */

        } catch (Exception e) {
            log.error("❌ Database seeding failed: {}", e.getMessage(), e);
            // Don't rethrow - let the app start even if seeding fails
        }
    }
}

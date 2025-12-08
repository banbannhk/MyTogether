package org.th.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.th.dto.ApiResponse;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
@Tag(name = "System", description = "System monitoring and diagnostics")
public class SystemController {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Check Database Network Latency
     * Executes a simple 'SELECT 1' query to measure round-trip time
     */
    @GetMapping("/db-latency")
    @Operation(summary = "Check DB Latency", description = "Measure network latency between App and Database (PostgreSQL)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkDbLatency() {
        long start = System.nanoTime();

        // Execute simple query
        Query query = entityManager.createNativeQuery("SELECT 1");
        query.getSingleResult();

        long end = System.nanoTime();
        long durationNs = end - start;
        double durationMs = durationNs / 1_000_000.0;

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("latency_ms", durationMs);
        metrics.put("status", "connected");

        String performance = durationMs < 20 ? "EXCELLENT (Local/Same Region)"
                : durationMs < 100 ? "GOOD (Nearby Region)"
                        : "POOR (Cross-Region/Network Issues)";
        metrics.put("performance", performance);

        return ResponseEntity.ok(ApiResponse.success("DB Latency Check Completed", metrics));
    }
}

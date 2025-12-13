package org.th.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.th.dto.ApiResponse;
import org.th.dto.UserDTO;
import org.th.service.admin.AdminService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Panel", description = "Administrative endpoints for system management")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard/stats")
    @Operation(summary = "Get Dashboard Stats", description = "Get high-level system statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        Map<String, Object> stats = adminService.getSystemStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats fetched successfully", stats));
    }

    // ==========================================
    // USER MANAGEMENT
    // ==========================================

    @GetMapping("/users")
    @Operation(summary = "Get All Users", description = "Get a paginated list of all users")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity
                .ok(ApiResponse.success("Users fetched successfully", adminService.getAllUsers(page, size, search)));
    }
}

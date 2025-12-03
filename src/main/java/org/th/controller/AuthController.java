package org.th.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.th.dto.ApiResponse;
import org.th.entity.RefreshToken;
import org.th.request.LoginRequest;
import org.th.request.RegisterRequest;
import org.th.response.AuthResponse;
import org.th.service.AuthService;
import org.th.service.DeviceTrackingService;
import org.th.service.RefreshTokenService;
import org.th.config.ratelimit.RateLimit;
import org.th.config.ratelimit.RateLimit.Tier;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and registration")
public class AuthController {

    private final AuthService authService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private DeviceTrackingService deviceTrackingService;

    @PostMapping("/register")
    @RateLimit(tier = Tier.AUTH, perUser = false)
    @Operation(summary = "Register new user", description = "Create a new user account")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @RateLimit(tier = Tier.AUTH, perUser = false)
    @Operation(summary = "Login", description = "Authenticate user and get JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh Access Token
     * POST /api/auth/refresh
     * Body: {"refreshToken": "xxx"}
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refreshToken(
            @RequestBody Map<String, String> request) {

        String refreshToken = request.get("refreshToken");

        // Verify and use token (auto-tracks usage)
        RefreshToken token = refreshTokenService.verifyAndUseToken(refreshToken);

        Map<String, Object> response = Map.of(
                "accessToken", "new_jwt_access_token_here", // TODO: Generate real JWT
                "expiresIn", 900 // 15 minutes
        );

        return ResponseEntity.ok(
                ApiResponse.success("Token refreshed successfully", response));
    }
}
package org.th.controller.mobile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.th.dto.ApiResponse;
import org.th.entity.RefreshToken;
import org.th.dto.mobile.AuthResponse;
import org.th.dto.mobile.LoginRequest;
import org.th.dto.mobile.RegisterRequest;
import org.th.service.mobile.AuthService;
import org.th.service.DeviceTrackingService;
import org.th.service.RefreshTokenService;
import org.th.config.ratelimit.RateLimit;
import org.th.config.ratelimit.RateLimit.Tier;

import java.util.Map;

@RestController
@RequestMapping("/api/mobile/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and registration")
public class AuthController {

    private final AuthService authService;
    private final org.th.service.UserActivityService userActivityService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private DeviceTrackingService deviceTrackingService;

    @PostMapping("/register")
    @RateLimit(tier = Tier.AUTH, perUser = false)
    @Operation(summary = "Register new user", description = "Create a new user account")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
            HttpServletRequest servletRequest) {
        AuthResponse response = authService.register(request);

        // Bind anonymous activity if Device ID is present
        String deviceId = servletRequest.getHeader("X-Device-ID");
        if (deviceId != null && response.id() != null) {
            userActivityService.bindDeviceHistory(deviceId, response.id());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @RateLimit(tier = Tier.AUTH, perUser = false)
    @Operation(summary = "Login", description = "Authenticate user and get JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest) {
        AuthResponse response = authService.login(request);

        // Bind anonymous activity if Device ID is present
        String deviceId = servletRequest.getHeader("X-Device-ID");
        if (deviceId != null && response.id() != null) {
            userActivityService.bindDeviceHistory(deviceId, response.id());
        }

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
package org.th.dto.mobile;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthResponse(
        @Schema(description = "JWT access token") String token,

        @Schema(description = "Token type", example = "Bearer") String type,

        @Schema(description = "User ID") Long id,

        @Schema(description = "Username") String username,

        @Schema(description = "Email") String email,

        @Schema(description = "Full name") String fullName,

        @Schema(description = "User role") String role) {
    public AuthResponse(String token, Long id, String username, String email, String fullName, String role) {
        this(token, "Bearer", id, username, email, fullName, role);
    }
}
package org.th.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record UserResponse(
        @Schema(description = "User ID")
        Long id,

        @Schema(description = "Username")
        String username,

        @Schema(description = "Email")
        String email,

        @Schema(description = "Full name")
        String fullName,

        @Schema(description = "User role")
        String role,

        @Schema(description = "Account creation date")
        LocalDateTime createdAt,

        @Schema(description = "Account active status")
        boolean isActive
) {}

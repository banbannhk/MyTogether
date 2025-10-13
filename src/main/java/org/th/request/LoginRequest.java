package org.th.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Username or email is required")
        @Schema(description = "Username or email", example = "john_doe")
        String usernameOrEmail,

        @NotBlank(message = "Password is required")
        @Schema(description = "Password", example = "Password123!")
        String password
) {}
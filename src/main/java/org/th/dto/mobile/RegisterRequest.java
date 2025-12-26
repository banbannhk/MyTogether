package org.th.dto.mobile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Username is required") @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters") @Schema(description = "Username", example = "john_doe") String username,

        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") @Schema(description = "Email address", example = "john@example.com") String email,

        @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters") @Schema(description = "Password", example = "Password123!") String password,

        @Schema(description = "Full name", example = "John Doe") String fullName,

        @Schema(description = "Privacy Policy Version", example = "v1.0") String privacyPolicyVersion) {
}
package com.davendra.calistrack_backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank
        @Email
        @Schema(description = "User's registered email address",
                example = "jane.doe@example.com",
                defaultValue = "davendra@mail.com")
        String email,
        @NotBlank
        @Schema(description = "Account password",
                example = "Dave@123")
        String password
) {
}

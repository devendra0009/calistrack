package com.davendra.calistrack_backend.user.dto;

import com.davendra.calistrack_backend.user.enums.ExperienceLevel;
import com.davendra.calistrack_backend.user.enums.Gender;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Partial profile update. Only non-null fields are applied.
 */
public record PatchMeRequest(
		@Size(min = 1, max = 100, message = "must be between 1 and 100 characters")
		String displayName,

		@DecimalMin(value = "0.01", message = "must be greater than 0")
		@DecimalMax(value = "300.00", message = "must be at most 300")
		BigDecimal heightCm,

		@DecimalMin(value = "0.01", message = "must be greater than 0")
		@DecimalMax(value = "500.00", message = "must be at most 500")
		BigDecimal weightKg,

		@Past(message = "must be a past date")
		LocalDate dateOfBirth,

		Gender gender,

		ExperienceLevel experience,

		@Size(max = 2048, message = "must be at most 2048 characters")
		String avatarUrl
) {
}

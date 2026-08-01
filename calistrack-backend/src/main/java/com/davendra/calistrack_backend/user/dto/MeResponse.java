package com.davendra.calistrack_backend.user.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record MeResponse(
		UUID id,
		String displayName,
		BigDecimal heightCm,
		BigDecimal weightKg,
		/** Computed from dateOfBirth when present; otherwise legacy stored age. */
		Integer age,
		LocalDate dateOfBirth,
		String gender,
		String experience,
		String avatarUrl,
		String role,
		GoalNodeSummary goal,
		Instant createdAt,
		Instant updatedAt
) {
}

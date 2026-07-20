package com.davendra.calistrack_backend.user.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MeResponse(
		UUID id,
		String displayName,
		BigDecimal heightCm,
		BigDecimal weightKg,
		Integer age,
		String gender,
		String experience,
		String avatarUrl,
		String role,
		GoalNodeSummary goal,
		Instant createdAt,
		Instant updatedAt
) {
}

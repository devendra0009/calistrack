package com.davendra.calistrack_backend.admin.dto;

import java.time.Instant;
import java.util.UUID;

public record AdminWorkoutPlanSummaryResponse(
		UUID id,
		String title,
		NamedRef node,
		String kind,
		String code,
		int durationDays,
		int dayCount,
		String status,
		Instant createdAt,
		Instant updatedAt
) {
}

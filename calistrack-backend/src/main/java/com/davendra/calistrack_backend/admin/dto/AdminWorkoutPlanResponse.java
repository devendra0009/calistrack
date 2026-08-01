package com.davendra.calistrack_backend.admin.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AdminWorkoutPlanResponse(
		UUID id,
		String title,
		String description,
		NamedRef node,
		String kind,
		String code,
		int durationDays,
		String status,
		List<AdminWorkoutPlanDayResponse> days,
		Instant createdAt,
		Instant updatedAt
) {
}

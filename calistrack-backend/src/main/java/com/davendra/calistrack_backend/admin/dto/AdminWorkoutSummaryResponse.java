package com.davendra.calistrack_backend.admin.dto;

import java.time.Instant;
import java.util.UUID;

/** List/summary view without nested exercise lines. */
public record AdminWorkoutSummaryResponse(
		UUID id,
		String title,
		String description,
		NamedRef goalNode,
		String difficulty,
		UUID createdByUserId,
		String status,
		int exerciseCount,
		Instant createdAt,
		Instant updatedAt
) {
}

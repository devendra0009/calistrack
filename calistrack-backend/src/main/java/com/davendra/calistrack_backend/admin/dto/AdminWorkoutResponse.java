package com.davendra.calistrack_backend.admin.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AdminWorkoutResponse(
		UUID id,
		String title,
		String description,
		NamedRef goalNode,
		String kind,
		String difficulty,
		UUID createdByUserId,
		String status,
		Instant createdAt,
		Instant updatedAt,
		List<AdminWorkoutExerciseResponse> exercises
) {
}

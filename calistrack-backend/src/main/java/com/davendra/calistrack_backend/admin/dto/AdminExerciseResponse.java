package com.davendra.calistrack_backend.admin.dto;

import java.time.Instant;
import java.util.UUID;

public record AdminExerciseResponse(
		UUID id,
		String name,
		String description,
		String category,
		String metricType,
		String difficulty,
		String thumbnailUrl,
		String demoVideoUrl,
		String status,
		Instant createdAt,
		Instant updatedAt
) {
}

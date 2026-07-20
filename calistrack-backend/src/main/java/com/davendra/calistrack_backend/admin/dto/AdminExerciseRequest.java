package com.davendra.calistrack_backend.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminExerciseRequest(
		@NotBlank @Size(max = 120) String name,
		String description,
		@NotBlank @Size(max = 20) String category,
		@NotBlank @Size(max = 20) String metricType,
		@NotBlank @Size(max = 20) String difficulty,
		String thumbnailUrl,
		String demoVideoUrl,
		@Size(max = 20) String status
) {
}

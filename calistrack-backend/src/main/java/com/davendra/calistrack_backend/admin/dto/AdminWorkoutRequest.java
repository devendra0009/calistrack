package com.davendra.calistrack_backend.admin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record AdminWorkoutRequest(
		@NotBlank @Size(max = 160) String title,
		String description,
		@NotNull UUID goalNodeId,
		@Size(max = 20) String kind,
		@NotBlank @Size(max = 20) String difficulty,
		@Size(max = 20) String status,
		@Valid List<AdminWorkoutExerciseRequest> exercises
) {
}

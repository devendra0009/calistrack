package com.davendra.calistrack_backend.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AdminWorkoutExerciseRequest(
		@NotNull UUID exerciseId,
		@Min(1) int sequence,
		Integer targetSets,
		Integer targetReps,
		Integer targetHoldSeconds,
		Integer targetRestSeconds,
		String notes,
		String demoVideoUrl
) {
}

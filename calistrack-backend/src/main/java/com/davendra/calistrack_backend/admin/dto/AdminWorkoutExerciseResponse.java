package com.davendra.calistrack_backend.admin.dto;

import java.util.UUID;

public record AdminWorkoutExerciseResponse(
		UUID id,
		NamedRef exercise,
		int sequence,
		Integer targetSets,
		Integer targetReps,
		Integer targetHoldSeconds,
		Integer targetRestSeconds,
		String notes,
		String demoVideoUrl
) {
}

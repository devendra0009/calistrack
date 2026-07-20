package com.davendra.calistrack_backend.workout.dto;

import com.davendra.calistrack_backend.workout.enums.ExerciseAttemptStatus;
import jakarta.validation.constraints.Min;

public record PatchExerciseAttemptRequest(
		@Min(0) Integer actualSets,
		@Min(0) Integer actualReps,
		@Min(0) Integer actualHoldSeconds,
		@Min(0) Integer actualRestSeconds,
		String notes,
		ExerciseAttemptStatus status
) {
}

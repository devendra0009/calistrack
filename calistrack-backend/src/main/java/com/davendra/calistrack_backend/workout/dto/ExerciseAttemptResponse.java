package com.davendra.calistrack_backend.workout.dto;

import com.davendra.calistrack_backend.workout.enums.ExerciseAttemptStatus;

import java.util.UUID;

public record ExerciseAttemptResponse(
		UUID id,
		UUID workoutSessionId,
		UUID workoutExerciseId,
		ExerciseAttemptStatus status,
		Integer actualSets,
		Integer actualReps,
		Integer actualHoldSeconds,
		Integer actualRestSeconds,
		String notes
) {
}

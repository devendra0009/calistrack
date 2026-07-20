package com.davendra.calistrack_backend.workout.dto;

import com.davendra.calistrack_backend.workout.enums.WorkoutSessionStatus;

import java.time.Instant;
import java.util.UUID;

public record CurrentWorkoutSessionResponse(
		UUID sessionId,
		UUID workoutId,
		String workoutTitle,
		String workoutDescription,
		UUID focusNodeId,
		String focusNodeName,
		WorkoutSessionStatus status,
		boolean verified,
		Instant createdAt,
		Instant updatedAt
) {
}

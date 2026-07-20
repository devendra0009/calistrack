package com.davendra.calistrack_backend.path.dto;

import java.util.UUID;

/**
 * Workout the user should perform next for a goal.
 */
public record WorkoutAssignment(
		UUID goalNodeId,
		UUID focusNodeId,
		UUID workoutId,
		String workoutTitle
) {
}

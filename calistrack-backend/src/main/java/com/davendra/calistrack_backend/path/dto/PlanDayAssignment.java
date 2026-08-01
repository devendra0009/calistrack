package com.davendra.calistrack_backend.path.dto;

import java.util.UUID;

/**
 * Workout the user should perform next: a specific day within a node's curated plan.
 */
public record PlanDayAssignment(
		UUID goalNodeId,
		UUID focusNodeId,
		UUID workoutId,
		String workoutTitle,
		UUID planId,
		UUID enrollmentId,
		int dayNumber,
		int durationDays
) {
}

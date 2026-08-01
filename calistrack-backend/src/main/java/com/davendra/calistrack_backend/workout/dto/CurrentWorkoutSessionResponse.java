package com.davendra.calistrack_backend.workout.dto;

import com.davendra.calistrack_backend.progress.enums.UserPlanEnrollmentStatus;
import com.davendra.calistrack_backend.workout.enums.WorkoutSessionStatus;

import java.time.Instant;
import java.util.UUID;

public record CurrentWorkoutSessionResponse(
		UUID sessionId,
		UUID workoutId,
		String workoutTitle,
		String workoutDescription,
		String workoutKind,
		UUID focusNodeId,
		String focusNodeName,
		WorkoutSessionStatus status,
		boolean verified,
		UUID planEnrollmentId,
		Integer planDayNumber,
		Integer planDurationDays,
		UserPlanEnrollmentStatus enrollmentStatus,
		boolean awaitingVerify,
		Instant createdAt,
		Instant updatedAt
) {
}

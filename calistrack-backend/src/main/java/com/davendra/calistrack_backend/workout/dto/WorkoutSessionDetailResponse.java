package com.davendra.calistrack_backend.workout.dto;

import com.davendra.calistrack_backend.progress.enums.UserPlanEnrollmentStatus;
import com.davendra.calistrack_backend.workout.enums.ExerciseAttemptStatus;
import com.davendra.calistrack_backend.workout.enums.WorkoutSessionStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WorkoutSessionDetailResponse(
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
		Instant startedAt,
		Instant completedAt,
		List<SessionExerciseLineDto> exercises
) {
	public record SessionExerciseLineDto(
			UUID workoutExerciseId,
			int sequence,
			UUID exerciseId,
			String exerciseName,
			String exerciseDescription,
			String exerciseMetricType,
			String thumbnailUrl,
			String demoVideoUrl,
			Integer targetSets,
			Integer targetReps,
			Integer targetHoldSeconds,
			Integer targetRestSeconds,
			String notes,
			AttemptSummaryDto attempt
	) {
	}

	public record AttemptSummaryDto(
			UUID id,
			ExerciseAttemptStatus status,
			Integer actualSets,
			Integer actualReps,
			Integer actualHoldSeconds,
			Integer actualRestSeconds,
			String notes
	) {
	}
}

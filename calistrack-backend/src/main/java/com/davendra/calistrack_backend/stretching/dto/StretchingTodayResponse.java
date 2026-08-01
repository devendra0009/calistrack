package com.davendra.calistrack_backend.stretching.dto;

import com.davendra.calistrack_backend.workout.enums.WorkoutSessionStatus;

import java.util.List;
import java.util.UUID;

public record StretchingTodayResponse(
		String planCode,
		String planTitle,
		int dayNumber,
		int durationDays,
		UUID workoutId,
		String workoutTitle,
		String workoutDescription,
		UUID sessionId,
		WorkoutSessionStatus sessionStatus,
		List<StretchExerciseLineDto> exercises
) {
	public record StretchExerciseLineDto(
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
			String notes
	) {
	}
}

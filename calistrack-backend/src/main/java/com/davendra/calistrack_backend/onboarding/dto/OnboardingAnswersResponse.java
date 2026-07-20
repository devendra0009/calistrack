package com.davendra.calistrack_backend.onboarding.dto;

import com.davendra.calistrack_backend.workout.enums.WorkoutSessionStatus;

import java.util.List;
import java.util.UUID;

public record OnboardingAnswersResponse(
		UUID goalNodeId,
		UUID focusNodeId,
		UUID sessionId,
		UUID workoutId,
		String workoutTitle,
		WorkoutSessionStatus sessionStatus,
		List<PlacedUserNodeDto> placedNodes
) {
}

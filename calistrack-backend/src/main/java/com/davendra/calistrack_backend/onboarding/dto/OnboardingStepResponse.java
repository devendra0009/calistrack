package com.davendra.calistrack_backend.onboarding.dto;

import com.davendra.calistrack_backend.onboarding.enums.OnboardingStepOutcome;
import com.davendra.calistrack_backend.workout.enums.WorkoutSessionStatus;

import java.util.List;
import java.util.UUID;

public record OnboardingStepResponse(
		OnboardingStepOutcome outcome,
		int index,
		int total,
		OnboardingQuestionDto nextQuestion,
		UUID goalNodeId,
		UUID focusNodeId,
		UUID sessionId,
		UUID workoutId,
		String workoutTitle,
		WorkoutSessionStatus sessionStatus,
		List<PlacedUserNodeDto> placedNodes
) {
	public static OnboardingStepResponse next(
			int index,
			int total,
			OnboardingQuestionDto nextQuestion
	) {
		return new OnboardingStepResponse(
				OnboardingStepOutcome.NEXT,
				index,
				total,
				nextQuestion,
				null,
				null,
				null,
				null,
				null,
				null,
				null
		);
	}

	public static OnboardingStepResponse placed(
			int index,
			int total,
			OnboardingAnswersResponse placement
	) {
		return new OnboardingStepResponse(
				OnboardingStepOutcome.PLACED,
				index,
				total,
				null,
				placement.goalNodeId(),
				placement.focusNodeId(),
				placement.sessionId(),
				placement.workoutId(),
				placement.workoutTitle(),
				placement.sessionStatus(),
				placement.placedNodes()
		);
	}
}

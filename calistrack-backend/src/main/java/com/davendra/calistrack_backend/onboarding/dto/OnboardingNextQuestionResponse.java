package com.davendra.calistrack_backend.onboarding.dto;

import java.util.UUID;

public record OnboardingNextQuestionResponse(
		UUID goalNodeId,
		int index,
		int total,
		OnboardingQuestionDto question
) {
}

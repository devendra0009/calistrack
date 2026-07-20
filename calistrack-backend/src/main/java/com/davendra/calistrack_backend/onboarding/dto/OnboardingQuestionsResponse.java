package com.davendra.calistrack_backend.onboarding.dto;

import java.util.List;
import java.util.UUID;

public record OnboardingQuestionsResponse(
		UUID goalNodeId,
		List<OnboardingQuestionDto> questions
) {
}

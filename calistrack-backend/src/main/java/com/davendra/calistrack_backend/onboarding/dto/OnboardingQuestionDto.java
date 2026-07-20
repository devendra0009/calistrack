package com.davendra.calistrack_backend.onboarding.dto;

import com.davendra.calistrack_backend.onboarding.enums.QuestionType;

import java.util.UUID;

public record OnboardingQuestionDto(
		UUID nodeId,
		String prompt,
		QuestionType type
) {
}

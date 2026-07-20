package com.davendra.calistrack_backend.onboarding.dto;

import com.davendra.calistrack_backend.onboarding.enums.QuestionType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OnboardingAnswerDto(
		@NotNull(message = "is required")
		UUID nodeId,

		@NotNull(message = "is required")
		QuestionType type,

		@NotNull(message = "is required")
		Object value
) {
}

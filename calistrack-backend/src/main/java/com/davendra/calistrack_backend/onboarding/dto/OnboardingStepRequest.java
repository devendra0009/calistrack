package com.davendra.calistrack_backend.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record OnboardingStepRequest(
		@NotNull(message = "is required")
		@Schema(description = "Goal node id",
				example = "22222222-2222-2222-2222-222222220010")
		UUID goalNodeId,

		@NotEmpty(message = "is required")
		@Valid
		@Schema(description = "Cumulative ordered answers so far (prefix of path questions)")
		List<OnboardingAnswerDto> answers
) {
}

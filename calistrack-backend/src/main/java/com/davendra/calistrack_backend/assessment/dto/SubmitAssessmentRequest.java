package com.davendra.calistrack_backend.assessment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SubmitAssessmentRequest(
		@NotNull UUID nodeId,
		@NotBlank @Size(max = 2048) String videoUrl,
		UUID workoutSessionId
) {
}

package com.davendra.calistrack_backend.admin.dto;

import com.davendra.calistrack_backend.path.enums.PlacementAnswerType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AdminPathQuestionRequest(
		@NotNull UUID goalNodeId,
		@NotNull UUID nodeId,
		@NotBlank String prompt,
		@NotNull PlacementAnswerType answerType,
		@NotNull @Min(1) Integer sortOrder
) {
}

package com.davendra.calistrack_backend.admin.dto;

import com.davendra.calistrack_backend.path.enums.PlacementAnswerType;

import java.time.Instant;
import java.util.UUID;

public record AdminPathQuestionResponse(
		UUID id,
		NamedRef goalNode,
		NamedRef node,
		String prompt,
		PlacementAnswerType answerType,
		int sortOrder,
		Instant createdAt
) {
}

package com.davendra.calistrack_backend.path.dto;

import com.davendra.calistrack_backend.path.enums.PlacementAnswerType;

import java.util.UUID;

public record PathQuestion(
		UUID nodeId,
		String prompt,
		PlacementAnswerType type
) {
}

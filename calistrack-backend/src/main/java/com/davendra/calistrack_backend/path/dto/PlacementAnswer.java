package com.davendra.calistrack_backend.path.dto;

import com.davendra.calistrack_backend.path.enums.PlacementAnswerType;

import java.util.UUID;

public record PlacementAnswer(
		UUID nodeId,
		PlacementAnswerType type,
		Object value
) {
}

package com.davendra.calistrack_backend.assessment.dto;

import com.davendra.calistrack_backend.progress.enums.UserNodeStatus;

import java.util.UUID;

public record PathAssessmentNodeResponse(
		UUID nodeId,
		String name,
		String description,
		String difficulty,
		int stepIndex,
		boolean goal,
		UserNodeStatus status,
		boolean verified,
		boolean awaitingVerify,
		String videoUrl
) {
}

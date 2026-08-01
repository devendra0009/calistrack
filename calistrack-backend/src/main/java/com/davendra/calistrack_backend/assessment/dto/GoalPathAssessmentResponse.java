package com.davendra.calistrack_backend.assessment.dto;

import java.util.List;
import java.util.UUID;

public record GoalPathAssessmentResponse(
		UUID goalNodeId,
		String goalNodeName,
		int verifiedCount,
		int totalCount,
		List<PathAssessmentNodeResponse> nodes
) {
}

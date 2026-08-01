package com.davendra.calistrack_backend.assessment.dto;

import com.davendra.calistrack_backend.assessment.enums.AssessmentStatus;

import java.time.Instant;
import java.util.UUID;

public record AssessmentResponse(
		UUID id,
		UUID nodeId,
		String nodeName,
		AssessmentStatus status,
		boolean verified,
		String videoUrl,
		Instant performedAt
) {
}

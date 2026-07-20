package com.davendra.calistrack_backend.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AdminNodeResponse(
		UUID id,
		String name,
		String description,
		String nodeType,
		NamedRef exercise,
		BigDecimal targetValue,
		String operator,
		String unitLabel,
		String difficulty,
		Integer xpReward,
		Integer estimatedMinutes,
		String status,
		Instant createdAt,
		Instant updatedAt
) {
}

package com.davendra.calistrack_backend.user.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record GoalNodeSummary(
		UUID id,
		String name,
		String description,
		String nodeType,
		String difficulty,
		BigDecimal targetValue,
		String operator,
		String unitLabel,
		Integer xpReward,
		Integer estimatedMinutes,
		String status
) {
}

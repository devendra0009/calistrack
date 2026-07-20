package com.davendra.calistrack_backend.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record AdminNodeRequest(
		@NotBlank @Size(max = 120) String name,
		String description,
		@NotBlank @Size(max = 20) String nodeType,
		@NotNull UUID exerciseId,
		@NotNull BigDecimal targetValue,
		@NotBlank @Size(max = 5) String operator,
		@NotBlank @Size(max = 20) String unitLabel,
		@NotBlank @Size(max = 20) String difficulty,
		Integer xpReward,
		Integer estimatedMinutes,
		@Size(max = 20) String status
) {
}

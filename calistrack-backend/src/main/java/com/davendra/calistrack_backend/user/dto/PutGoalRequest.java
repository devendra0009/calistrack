package com.davendra.calistrack_backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PutGoalRequest(
		@NotNull(message = "is required")
		@Schema(description = "Muscle up goal id",
				defaultValue = "22222222-2222-2222-2222-222222220010")
		UUID goalNodeId
) {
}

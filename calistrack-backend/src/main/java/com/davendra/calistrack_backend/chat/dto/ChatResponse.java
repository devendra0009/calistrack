package com.davendra.calistrack_backend.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI coach reply")
public record ChatResponse(
		@Schema(description = "Coach reply text")
		String response
) {
}

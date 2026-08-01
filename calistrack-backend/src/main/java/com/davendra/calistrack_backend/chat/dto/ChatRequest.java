package com.davendra.calistrack_backend.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "User message for the AI coach")
public record ChatRequest(
		@NotBlank
		@Size(max = 4000)
		@Schema(example = "How can I improve my pull-ups?", requiredMode = Schema.RequiredMode.REQUIRED)
		String message
) {
}

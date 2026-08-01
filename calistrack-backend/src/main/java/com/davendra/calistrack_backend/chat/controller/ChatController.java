package com.davendra.calistrack_backend.chat.controller;

import com.davendra.calistrack_backend.chat.dto.ChatRequest;
import com.davendra.calistrack_backend.chat.dto.ChatResponse;
import com.davendra.calistrack_backend.chat.service.AiCoachService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
@Tag(name = "AI Coach", description = "Calisthenics AI coach chat (stateless, no memory)")
public class ChatController {

	private final AiCoachService aiCoachService;

	public ChatController(AiCoachService aiCoachService) {
		this.aiCoachService = aiCoachService;
	}

	@PostMapping
	@Operation(
			summary = "Chat with the AI coach",
			description = "Sends a single user message to the configured coach model. "
					+ "No conversation memory; each call is independent."
	)
	public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
		return aiCoachService.chat(request);
	}
}

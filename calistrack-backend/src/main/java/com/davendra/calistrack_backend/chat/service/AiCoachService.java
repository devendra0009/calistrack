package com.davendra.calistrack_backend.chat.service;

import com.davendra.calistrack_backend.chat.dto.ChatRequest;
import com.davendra.calistrack_backend.chat.dto.ChatResponse;

/**
 * Abstraction over the AI coach provider.
 * Controllers depend only on this interface so implementations
 * (Gemma via OpenRouter, GPT, Llama, Ollama, fine-tuned, etc.) can be swapped
 * without changing API or business orchestration.
 */
public interface AiCoachService {

	ChatResponse chat(ChatRequest request);
}

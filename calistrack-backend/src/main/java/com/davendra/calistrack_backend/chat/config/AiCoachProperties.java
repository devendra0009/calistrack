package com.davendra.calistrack_backend.chat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * App-level AI coach settings (provider switch and message limits).
 * Model / base-url / API key / OpenRouter headers remain under {@code spring.ai.openai.*}.
 */
@ConfigurationProperties(prefix = "calistrack.ai.coach")
public class AiCoachProperties {

	/**
	 * Active coach implementation: {@code gemma} (default). Future: gpt, llama, ollama, fine-tuned, etc.
	 */
	private String provider = "gemma";

	/**
	 * Max accepted user message length (chars). Keep in sync with {@code ChatRequest} {@code @Size}.
	 */
	private int maxMessageLength = 4000;

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public int getMaxMessageLength() {
		return maxMessageLength;
	}

	public void setMaxMessageLength(int maxMessageLength) {
		this.maxMessageLength = maxMessageLength;
	}
}

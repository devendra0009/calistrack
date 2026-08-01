package com.davendra.calistrack_backend.chat.config;

import com.davendra.calistrack_backend.chat.prompt.AiCoachSystemPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires a dedicated {@link ChatClient} for the AI coach.
 * <p>
 * Future models (GPT, Llama, Ollama, fine-tuned) can register alternate
 * {@code ChatClient} / {@code AiCoachService} beans behind the same interface
 * without changing controllers.
 */
@Configuration
@EnableConfigurationProperties(AiCoachProperties.class)
public class ChatClientConfig {

	@Bean
	ChatClient aiCoachChatClient(
			ChatClient.Builder chatClientBuilder,
			AiCoachSystemPrompt systemPrompt
	) {
		return chatClientBuilder
				.defaultSystem(systemPrompt.content())
				.build();
	}
}

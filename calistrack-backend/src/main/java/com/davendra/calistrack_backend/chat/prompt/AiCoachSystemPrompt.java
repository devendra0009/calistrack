package com.davendra.calistrack_backend.chat.prompt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Loads the AI coach system prompt from a classpath resource so it stays
 * out of service logic and can evolve independently (and later be templated for RAG/context).
 */
@Component
public class AiCoachSystemPrompt {

	private final String content;

	public AiCoachSystemPrompt(
			@Value("classpath:prompts/ai-coach-system.txt") Resource promptResource
	) {
		this.content = load(promptResource);
	}

	public String content() {
		return content;
	}

	private static String load(Resource resource) {
		try (InputStream in = resource.getInputStream()) {
			String text = StreamUtils.copyToString(in, StandardCharsets.UTF_8).trim();
			if (text.isEmpty()) {
				throw new IllegalStateException("AI coach system prompt resource is empty");
			}
			return text;
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Failed to load AI coach system prompt", ex);
		}
	}
}

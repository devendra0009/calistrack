package com.davendra.calistrack_backend.chat.service;

import com.davendra.calistrack_backend.chat.config.AiCoachProperties;
import com.davendra.calistrack_backend.chat.dto.ChatRequest;
import com.davendra.calistrack_backend.chat.dto.ChatResponse;
import com.davendra.calistrack_backend.chat.exception.AiCoachException;
import com.davendra.calistrack_backend.common.exception.ApiException;
import com.davendra.calistrack_backend.user.entity.AppUser;
import com.davendra.calistrack_backend.user.service.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Gemma (via OpenRouter OpenAI-compatible API) implementation of {@link AiCoachService}.
 * Stateless: no chat memory, RAG, tools, or embeddings in this version.
 */
@Service
@ConditionalOnProperty(name = "calistrack.ai.coach.provider", havingValue = "gemma", matchIfMissing = true)
public class GemmaAiCoachService implements AiCoachService {

	private static final Logger log = LoggerFactory.getLogger(GemmaAiCoachService.class);

	private final ChatClient chatClient;
	private final CurrentUserService currentUserService;
	private final AiCoachProperties properties;

	public GemmaAiCoachService(
			@Qualifier("aiCoachChatClient") ChatClient chatClient,
			CurrentUserService currentUserService,
			AiCoachProperties properties
	) {
		this.chatClient = chatClient;
		this.currentUserService = currentUserService;
		this.properties = properties;
	}

	@Override
	public ChatResponse chat(ChatRequest request) {
		AppUser user = currentUserService.requireActiveUser();
		String message = request.message() == null ? "" : request.message().trim();

		if (!StringUtils.hasText(message)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "message must not be blank");
		}
		if (message.length() > properties.getMaxMessageLength()) {
			throw new ApiException(
					HttpStatus.BAD_REQUEST,
					"message must be at most " + properties.getMaxMessageLength() + " characters"
			);
		}

		log.info(
				"AI coach chat started userId={} messageLength={} provider={}",
				user.getId(),
				message.length(),
				properties.getProvider()
		);

		try {
			String reply = chatClient.prompt()
					.user(message)
					.call()
					.content();

			if (!StringUtils.hasText(reply)) {
				throw new AiCoachException(HttpStatus.BAD_GATEWAY, "AI coach returned an empty response");
			}

			String trimmed = reply.trim();
			log.info(
					"AI coach chat completed userId={} responseLength={}",
					user.getId(),
					trimmed.length()
			);
			return new ChatResponse(trimmed);
		}
		catch (AiCoachException | ApiException ex) {
			throw ex;
		}
		catch (ResourceAccessException ex) {
			log.warn("AI coach timeout/network failure userId={}: {}", user.getId(), ex.getMessage());
			throw new AiCoachException(
					HttpStatus.GATEWAY_TIMEOUT,
					"AI coach timed out. Please try again shortly.",
					ex
			);
		}
		catch (RestClientResponseException ex) {
			log.error(
					"AI coach provider HTTP error userId={} status={}",
					user.getId(),
					ex.getStatusCode().value()
			);
			HttpStatus status = ex.getStatusCode().value() == 429
					? HttpStatus.TOO_MANY_REQUESTS
					: HttpStatus.BAD_GATEWAY;
			throw new AiCoachException(
					status,
					status == HttpStatus.TOO_MANY_REQUESTS
							? "AI coach rate limit reached. Please try again shortly."
							: "AI coach request failed. Please try again later.",
					ex
			);
		}
		catch (RestClientException ex) {
			log.error("AI coach client failure userId={}: {}", user.getId(), ex.getMessage());
			throw new AiCoachException(
					HttpStatus.BAD_GATEWAY,
					"AI coach request failed. Please try again later.",
					ex
			);
		}
		catch (Exception ex) {
			log.error("AI coach unexpected failure userId={}", user.getId(), ex);
			throw new AiCoachException(
					HttpStatus.BAD_GATEWAY,
					"AI coach request failed. Please try again later.",
					ex
			);
		}
	}
}

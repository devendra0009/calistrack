package com.davendra.calistrack_backend.keepalive.config;

import com.davendra.calistrack_backend.keepalive.support.ActiveWindowEvaluator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(KeepAliveProperties.class)
public class KeepAliveConfig {

	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
	private static final Duration READ_TIMEOUT = Duration.ofSeconds(15);

	@Bean
	@ConditionalOnProperty(prefix = "calistrack.keep-alive", name = "enabled", havingValue = "true")
	ActiveWindowEvaluator activeWindowEvaluator(KeepAliveProperties properties) {
		return new ActiveWindowEvaluator(properties);
	}

	@Bean
	@ConditionalOnProperty(prefix = "calistrack.keep-alive", name = "enabled", havingValue = "true")
	RestClient keepAliveRestClient(KeepAliveProperties properties) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
		requestFactory.setReadTimeout(READ_TIMEOUT);

		return RestClient.builder()
				.baseUrl(trimTrailingSlash(properties.getBaseUrl()))
				.requestFactory(requestFactory)
				.build();
	}

	private static String trimTrailingSlash(String baseUrl) {
		if (baseUrl == null || baseUrl.isBlank()) {
			return "";
		}
		return baseUrl.replaceAll("/+$", "");
	}
}

package com.davendra.calistrack_backend.common.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

	/**
	 * Comma-separated origin patterns.
	 * Example (Render): http://localhost:*,http://127.0.0.1:*,https://*.github.io
	 */
	@Value("${app.cors.allowed-origin-patterns:http://localhost:*,http://127.0.0.1:*}")
	private String allowedOriginPatterns;

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		List<String> patterns = Arrays.stream(allowedOriginPatterns.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.toList();

		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOriginPatterns(patterns);
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setExposedHeaders(List.of("Authorization"));
		config.setAllowCredentials(true);
		config.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}

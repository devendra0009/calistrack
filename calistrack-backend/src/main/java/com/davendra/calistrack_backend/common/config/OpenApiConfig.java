package com.davendra.calistrack_backend.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OpenApiConfig {

	/**
	 * Public base URL for Swagger "Try it out" (e.g. https://calistrack-backend.onrender.com).
	 * Do not use {@code server.port} here — on Render that is an internal port (often 10000).
	 */
	@Value("${openapi.server-url:}")
	private String openApiServerUrl;

	@Value("${openapi.local-server-url:http://localhost:8084}")
	private String localServerUrl;

	@Bean
	OpenAPI calistrackOpenAPI() {
		final String bearerScheme = "bearerAuth";

		List<Server> servers = new ArrayList<>();
		if (openApiServerUrl != null && !openApiServerUrl.isBlank()) {
			servers.add(new Server()
					.url(trimTrailingSlash(openApiServerUrl.trim()))
					.description("Deployed"));
		}
		// Relative URL = whatever host you opened Swagger on (works on Render + local)
		servers.add(new Server().url("/").description("Current host"));
		servers.add(new Server()
				.url(trimTrailingSlash(localServerUrl))
				.description("Local"));

		return new OpenAPI()
				.info(new Info()
						.title("Calistrack API")
						.description("""
								Calisthenics progress tracking API.

								**Auth flow**
								1. Call `POST /api/v1/auth/register` or `POST /api/v1/auth/login`
								2. Copy `idToken` from the response
								3. Click **Authorize** and paste the token (Swagger adds `Bearer ` for you)
								4. Call protected endpoints
								""")
						.version("v1")
						.contact(new Contact().name("Calistrack")))
				.servers(servers)
				.components(new Components()
						.addSecuritySchemes(bearerScheme, new SecurityScheme()
								.name(bearerScheme)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.description("Firebase ID token from login/register (`idToken`)")))
				.addSecurityItem(new SecurityRequirement().addList(bearerScheme));
	}

	private static String trimTrailingSlash(String url) {
		if (url.endsWith("/")) {
			return url.substring(0, url.length() - 1);
		}
		return url;
	}
}

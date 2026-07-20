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

import java.util.List;

@Configuration
public class OpenApiConfig {

	@Value("${server.port:8084}")
	private String serverPort;

	@Bean
	OpenAPI calistrackOpenAPI() {
		final String bearerScheme = "bearerAuth";

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
				.servers(List.of(
						new Server()
								.url("http://localhost:" + serverPort)
								.description("Local")
				))
				.components(new Components()
						.addSecuritySchemes(bearerScheme, new SecurityScheme()
								.name(bearerScheme)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.description("Firebase ID token from login/register (`idToken`)")))
				.addSecurityItem(new SecurityRequirement().addList(bearerScheme));
	}
}

package com.davendra.calistrack_backend.common.config;

import com.davendra.calistrack_backend.auth.filter.FirebaseAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			FirebaseAuthenticationFilter firebaseAuthenticationFilter
	) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults())
				// No HTTP session — each request must carry its own Bearer token.
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				// Without this, missing auth often becomes 403 (anonymous) instead of 401.
				.anonymous(AbstractHttpConfigurer::disable)
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint((request, response, authException) -> {
							response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
							response.setContentType("application/problem+json");
							response.getWriter().write("""
									{"type":"https://calistrack.app/problems/authentication-required","title":"Unauthorized","status":401,"detail":"Authentication required — pass Authorization: Bearer <idToken>"}
									""".trim());
						})
				)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/v1/auth/**").permitAll()
						// Local provider: upload token authorizes the PUT (raw body, not multipart)
						.requestMatchers("/api/v1/media/local/upload/**").permitAll()
						// Local provider: public file URLs for <img>/<video> (no Authorization header)
						.requestMatchers("/api/v1/media/local/files/**").permitAll()
						.requestMatchers(
								"/v3/api-docs/**",
								"/swagger-ui/**",
								"/swagger-ui.html"
						).permitAll()
						.requestMatchers("/error").permitAll()
						.anyRequest().authenticated()
				)
				.addFilterBefore(firebaseAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}

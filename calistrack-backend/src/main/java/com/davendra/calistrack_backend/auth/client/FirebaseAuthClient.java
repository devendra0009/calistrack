package com.davendra.calistrack_backend.auth.client;

import com.davendra.calistrack_backend.auth.exception.AuthException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

/**
 * Calls Firebase Identity Toolkit / Secure Token REST APIs.
 * Does not mint JWTs locally — Firebase returns idToken + refreshToken.
 */
@Component
public class FirebaseAuthClient {

	private final RestClient restClient;
	private final String apiKey;

	public FirebaseAuthClient(@Value("${firebase.api-key}") String apiKey) {
		this.apiKey = apiKey;
		this.restClient = RestClient.create();
	}

	public FirebaseAuthTokens signUp(String email, String password) {
		return postIdentity(
				"accounts:signUp",
				Map.of(
						"email", email,
						"password", password,
						"returnSecureToken", true
				)
		);
	}

	public FirebaseAuthTokens signIn(String email, String password) {
		return postIdentity(
				"accounts:signInWithPassword",
				Map.of(
						"email", email,
						"password", password,
						"returnSecureToken", true
				)
		);
	}

	public FirebaseAuthTokens refresh(String refreshToken) {
		try {
			MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
			form.add("grant_type", "refresh_token");
			form.add("refresh_token", refreshToken);

			@SuppressWarnings("unchecked")
			Map<String, Object> body = restClient.post()
					.uri("https://securetoken.googleapis.com/v1/token?key={key}", apiKey)
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.body(form)
					.retrieve()
					.body(Map.class);

			if (body == null || body.get("id_token") == null) {
				throw new AuthException("Firebase refresh returned empty tokens");
			}

			return new FirebaseAuthTokens(
					(String) body.get("id_token"),
					(String) body.get("refresh_token"),
					(String) body.get("user_id"),
					null,
					body.get("expires_in") != null ? String.valueOf(body.get("expires_in")) : null
			);
		} catch (RestClientResponseException e) {
			throw new AuthException("Firebase refresh failed: " + e.getResponseBodyAsString());
		}
	}

	@SuppressWarnings("unchecked")
	private FirebaseAuthTokens postIdentity(String path, Map<String, Object> payload) {
		try {
			// Path must stay literal: {path} encodes ":" → "%3A" and Identity Toolkit 404s.
			Map<String, Object> body = restClient.post()
					.uri("https://identitytoolkit.googleapis.com/v1/" + path + "?key={key}", apiKey)
					.contentType(MediaType.APPLICATION_JSON)
					.body(payload)
					.retrieve()
					.body(Map.class);

			if (body == null || body.get("idToken") == null) {
				throw new AuthException("Firebase auth returned empty tokens");
			}

			return new FirebaseAuthTokens(
					(String) body.get("idToken"),
					(String) body.get("refreshToken"),
					(String) body.get("localId"),
					(String) body.get("email"),
					body.get("expiresIn") != null ? String.valueOf(body.get("expiresIn")) : null
			);
		} catch (RestClientResponseException e) {
			throw new AuthException("Firebase auth failed: " + e.getResponseBodyAsString());
		}
	}

	public record FirebaseAuthTokens(
			String idToken,
			String refreshToken,
			String localId,
			String email,
			String expiresIn
	) {
	}
}

package com.davendra.calistrack_backend.common.security;

import com.davendra.calistrack_backend.auth.exception.AuthException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

	private SecurityUtils() {
	}

	/**
	 * Firebase UID set as the authentication principal by {@code FirebaseAuthenticationFilter}.
	 */
	public static String requireFirebaseUid() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null
				|| !authentication.isAuthenticated()
				|| authentication.getPrincipal() == null
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			throw new AuthException("Authentication required");
		}
		Object principal = authentication.getPrincipal();
		if (!(principal instanceof String firebaseUid) || firebaseUid.isBlank()) {
			throw new AuthException("Invalid authentication principal");
		}
		return firebaseUid;
	}
}

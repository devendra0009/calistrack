package com.davendra.calistrack_backend.auth.dto;

import java.util.UUID;

public record AuthResponse(
		String idToken,
		String refreshToken,
		String expiresIn,
		UUID userId,
		String email,
		String displayName
) {
}

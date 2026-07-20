package com.davendra.calistrack_backend.media.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record CompleteUploadRequest(
		@NotNull UUID mediaId,
		/**
		 * Optional provider callback payload (e.g. Cloudinary upload response fields).
		 * Verification always happens server-side against the provider API / object store.
		 */
		Map<String, Object> providerUploadResult
) {
}

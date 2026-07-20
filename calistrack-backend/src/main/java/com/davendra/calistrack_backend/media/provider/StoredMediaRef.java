package com.davendra.calistrack_backend.media.provider;

import java.util.Map;

/**
 * Stable reference to an object already stored at a provider.
 */
public record StoredMediaRef(
		String publicId,
		String bucketName,
		Map<String, Object> providerMetadata
) {
}

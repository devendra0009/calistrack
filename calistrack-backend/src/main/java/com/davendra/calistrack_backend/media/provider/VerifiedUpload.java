package com.davendra.calistrack_backend.media.provider;

import com.davendra.calistrack_backend.media.enums.ResourceType;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Canonical metadata returned after a successful provider-side verification.
 */
public record VerifiedUpload(
		String publicId,
		String bucketName,
		String secureUrl,
		String thumbnailUrl,
		String mimeType,
		Long fileSizeBytes,
		Integer width,
		Integer height,
		BigDecimal durationSeconds,
		ResourceType resourceType,
		String checksum,
		Map<String, Object> providerMetadata
) {
}

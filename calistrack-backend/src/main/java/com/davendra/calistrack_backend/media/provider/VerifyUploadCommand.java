package com.davendra.calistrack_backend.media.provider;

import java.util.Map;
import java.util.UUID;

/**
 * Command to confirm a client-side upload completed and ask the provider to verify it.
 */
public record VerifyUploadCommand(
		UUID mediaId,
		UUID ownerUserId,
		String publicId,
		String bucketName,
		String expectedMimeType,
		Long expectedMaxBytes,
		Map<String, Object> clientUploadResult
) {
}

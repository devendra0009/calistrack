package com.davendra.calistrack_backend.media.provider;

import java.util.Map;

/**
 * Signed / pre-authorized upload instructions returned to the frontend.
 * The client uploads <strong>directly</strong> to the provider — never through this API as multipart.
 */
public record UploadAuthorization(
		String uploadUrl,
		String httpMethod,
		Map<String, String> headers,
		Map<String, String> formFields,
		String bucketName,
		String publicId,
		Map<String, Object> providerMetadata
) {
}

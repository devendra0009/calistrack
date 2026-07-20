package com.davendra.calistrack_backend.media.dto;

import com.davendra.calistrack_backend.media.enums.StorageProviderType;

import java.util.Map;
import java.util.UUID;

public record UploadRequestResponse(
		UUID mediaId,
		StorageProviderType provider,
		String uploadUrl,
		String httpMethod,
		Map<String, String> headers,
		Map<String, String> formFields,
		String publicId,
		String bucketName
) {
}

package com.davendra.calistrack_backend.media.dto;

import com.davendra.calistrack_backend.media.enums.MediaType;
import com.davendra.calistrack_backend.media.enums.MediaVisibility;
import com.davendra.calistrack_backend.media.enums.ResourceType;
import com.davendra.calistrack_backend.media.enums.StorageProviderType;
import com.davendra.calistrack_backend.media.enums.UploadStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record MediaResponse(
		UUID id,
		UUID ownerUserId,
		StorageProviderType provider,
		String bucketName,
		String publicId,
		String originalFilename,
		String mimeType,
		String extension,
		Long fileSizeBytes,
		Integer width,
		Integer height,
		BigDecimal durationSeconds,
		ResourceType resourceType,
		MediaType mediaType,
		String secureUrl,
		String thumbnailUrl,
		String checksum,
		UploadStatus uploadStatus,
		MediaVisibility visibility,
		String downloadUrl,
		Map<String, Object> providerMetadata,
		Instant createdAt,
		Instant updatedAt
) {
}

package com.davendra.calistrack_backend.media.provider;

import com.davendra.calistrack_backend.media.enums.MediaType;
import com.davendra.calistrack_backend.media.enums.MediaVisibility;
import com.davendra.calistrack_backend.media.enums.ResourceType;
import com.davendra.calistrack_backend.media.enums.StorageProviderType;

import java.util.UUID;

/**
 * Command issued by {@code MediaService} when asking a provider to authorize a direct client upload.
 */
public record AuthorizeUploadCommand(
		UUID mediaId,
		UUID ownerUserId,
		String publicId,
		String originalFilename,
		String mimeType,
		String extension,
		Long declaredFileSizeBytes,
		ResourceType resourceType,
		MediaType mediaType,
		MediaVisibility visibility
) {
}

package com.davendra.calistrack_backend.media.dto;

import com.davendra.calistrack_backend.media.enums.MediaType;
import com.davendra.calistrack_backend.media.enums.MediaVisibility;
import com.davendra.calistrack_backend.media.enums.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UploadRequestDto(
		@NotBlank @Size(max = 512) String originalFilename,
		@NotBlank @Size(max = 128) String mimeType,
		@Positive Long fileSizeBytes,
		@NotNull ResourceType resourceType,
		@NotNull MediaType mediaType,
		MediaVisibility visibility
) {
}

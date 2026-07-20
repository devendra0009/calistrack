package com.davendra.calistrack_backend.media.mapper;

import com.davendra.calistrack_backend.media.dto.MediaResponse;
import com.davendra.calistrack_backend.media.entity.Media;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MediaMapper {

	@Mapping(target = "downloadUrl", ignore = true)
	MediaResponse toResponse(Media media);

	default MediaResponse toResponse(Media media, String downloadUrl) {
		MediaResponse base = toResponse(media);
		return new MediaResponse(
				base.id(),
				base.ownerUserId(),
				base.provider(),
				base.bucketName(),
				base.publicId(),
				base.originalFilename(),
				base.mimeType(),
				base.extension(),
				base.fileSizeBytes(),
				base.width(),
				base.height(),
				base.durationSeconds(),
				base.resourceType(),
				base.mediaType(),
				base.secureUrl(),
				base.thumbnailUrl(),
				base.checksum(),
				base.uploadStatus(),
				base.visibility(),
				downloadUrl,
				base.providerMetadata(),
				base.createdAt(),
				base.updatedAt()
		);
	}
}

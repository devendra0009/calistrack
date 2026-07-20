package com.davendra.calistrack_backend.media.repo;

import com.davendra.calistrack_backend.media.entity.Media;
import com.davendra.calistrack_backend.media.enums.UploadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MediaRepository extends JpaRepository<Media, UUID> {

	Optional<Media> findByIdAndDeletedAtIsNull(UUID id);

	Optional<Media> findByIdAndOwnerUserIdAndDeletedAtIsNull(UUID id, UUID ownerUserId);

	boolean existsByIdAndOwnerUserIdAndUploadStatusAndDeletedAtIsNull(
			UUID id,
			UUID ownerUserId,
			UploadStatus uploadStatus
	);
}

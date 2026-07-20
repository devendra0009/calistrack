package com.davendra.calistrack_backend.media.service;

import com.davendra.calistrack_backend.common.exception.ApiException;
import com.davendra.calistrack_backend.media.config.MediaProperties;
import com.davendra.calistrack_backend.media.dto.CompleteUploadRequest;
import com.davendra.calistrack_backend.media.dto.MediaResponse;
import com.davendra.calistrack_backend.media.dto.UploadRequestDto;
import com.davendra.calistrack_backend.media.dto.UploadRequestResponse;
import com.davendra.calistrack_backend.media.entity.Media;
import com.davendra.calistrack_backend.media.enums.MediaVisibility;
import com.davendra.calistrack_backend.media.enums.UploadStatus;
import com.davendra.calistrack_backend.media.mapper.MediaMapper;
import com.davendra.calistrack_backend.media.provider.AuthorizeUploadCommand;
import com.davendra.calistrack_backend.media.provider.StoredMediaRef;
import com.davendra.calistrack_backend.media.provider.StorageProvider;
import com.davendra.calistrack_backend.media.provider.StorageProviderResolver;
import com.davendra.calistrack_backend.media.provider.UploadAuthorization;
import com.davendra.calistrack_backend.media.provider.VerifiedUpload;
import com.davendra.calistrack_backend.media.provider.VerifyUploadCommand;
import com.davendra.calistrack_backend.media.repo.MediaRepository;
import com.davendra.calistrack_backend.user.entity.AppUser;
import com.davendra.calistrack_backend.user.service.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

/**
 * Orchestrates direct-to-storage uploads. Depends only on {@link StorageProvider}
 * via {@link StorageProviderResolver} — never on a concrete vendor SDK.
 */
@Service
public class MediaService {

	private final MediaRepository mediaRepository;
	private final StorageProviderResolver storageProviderResolver;
	private final MediaProperties mediaProperties;
	private final MediaMapper mediaMapper;
	private final CurrentUserService currentUserService;

	public MediaService(
			MediaRepository mediaRepository,
			StorageProviderResolver storageProviderResolver,
			MediaProperties mediaProperties,
			MediaMapper mediaMapper,
			CurrentUserService currentUserService
	) {
		this.mediaRepository = mediaRepository;
		this.storageProviderResolver = storageProviderResolver;
		this.mediaProperties = mediaProperties;
		this.mediaMapper = mediaMapper;
		this.currentUserService = currentUserService;
	}

	@Transactional
	public UploadRequestResponse requestUpload(UploadRequestDto request) {
		AppUser user = currentUserService.requireActiveUser();
		validateUploadRequest(request);

		StorageProvider provider = storageProviderResolver.primary();
		String extension = extractExtension(request.originalFilename());
		String publicId = UUID.randomUUID().toString().replace("-", "");

		Media media = new Media();
		media.setOwnerUserId(user.getId());
		media.setProvider(provider.getType());
		media.setPublicId(publicId);
		media.setOriginalFilename(request.originalFilename());
		media.setMimeType(request.mimeType().toLowerCase(Locale.ROOT));
		media.setExtension(extension);
		media.setFileSizeBytes(request.fileSizeBytes());
		media.setResourceType(request.resourceType());
		media.setMediaType(request.mediaType());
		media.setUploadStatus(UploadStatus.PENDING);
		media.setVisibility(request.visibility() != null ? request.visibility() : MediaVisibility.PRIVATE);
		media.setProviderMetadata(new HashMap<>());
		media = mediaRepository.save(media);

		UploadAuthorization authorization = provider.authorizeUpload(new AuthorizeUploadCommand(
				media.getId(),
				user.getId(),
				publicId,
				request.originalFilename(),
				media.getMimeType(),
				extension,
				request.fileSizeBytes(),
				request.resourceType(),
				request.mediaType(),
				media.getVisibility()
		));

		media.setBucketName(authorization.bucketName());
		media.setPublicId(authorization.publicId());
		if (authorization.providerMetadata() != null) {
			media.setProviderMetadata(new HashMap<>(authorization.providerMetadata()));
		}
		mediaRepository.save(media);

		return new UploadRequestResponse(
				media.getId(),
				provider.getType(),
				authorization.uploadUrl(),
				authorization.httpMethod(),
				authorization.headers(),
				authorization.formFields(),
				authorization.publicId(),
				authorization.bucketName()
		);
	}

	@Transactional
	public MediaResponse completeUpload(CompleteUploadRequest request) {
		AppUser user = currentUserService.requireActiveUser();
		Media media = mediaRepository.findByIdAndOwnerUserIdAndDeletedAtIsNull(request.mediaId(), user.getId())
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Media not found"));

		if (media.getUploadStatus() == UploadStatus.VERIFIED) {
			return toResponse(media);
		}
		if (media.getUploadStatus() != UploadStatus.PENDING && media.getUploadStatus() != UploadStatus.UPLOADED) {
			throw new ApiException(HttpStatus.CONFLICT, "Media is not awaiting upload completion");
		}

		StorageProvider provider = storageProviderResolver.require(media.getProvider());
		VerifiedUpload verified = provider.verifyUpload(new VerifyUploadCommand(
				media.getId(),
				user.getId(),
				media.getPublicId(),
				media.getBucketName(),
				media.getMimeType(),
				mediaProperties.getUpload().getMaxFileSizeBytes(),
				request.providerUploadResult()
		));

		media.setPublicId(verified.publicId());
		media.setBucketName(verified.bucketName());
		media.setSecureUrl(verified.secureUrl());
		media.setThumbnailUrl(verified.thumbnailUrl());
		if (verified.mimeType() != null) {
			media.setMimeType(verified.mimeType());
		}
		media.setFileSizeBytes(verified.fileSizeBytes());
		media.setWidth(verified.width());
		media.setHeight(verified.height());
		media.setDurationSeconds(verified.durationSeconds());
		if (verified.resourceType() != null) {
			media.setResourceType(verified.resourceType());
		}
		media.setChecksum(verified.checksum());
		media.setUploadStatus(UploadStatus.VERIFIED);
		if (verified.providerMetadata() != null) {
			media.setProviderMetadata(new HashMap<>(verified.providerMetadata()));
		}
		mediaRepository.save(media);

		return toResponse(media);
	}

	@Transactional(readOnly = true)
	public MediaResponse getMedia(UUID id) {
		AppUser user = currentUserService.requireActiveUser();
		Media media = mediaRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Media not found"));
		assertCanAccess(user, media);
		return toResponse(media);
	}

	@Transactional
	public void deleteMedia(UUID id) {
		AppUser user = currentUserService.requireActiveUser();
		Media media = mediaRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Media not found"));
		assertCanAccess(user, media);

		StorageProvider provider = storageProviderResolver.require(media.getProvider());
		provider.delete(new StoredMediaRef(media.getPublicId(), media.getBucketName(), media.getProviderMetadata()));
		media.softDelete();
		mediaRepository.save(media);
	}

	private MediaResponse toResponse(Media media) {
		StorageProvider provider = storageProviderResolver.require(media.getProvider());
		String downloadUrl = media.getSecureUrl();
		if (downloadUrl == null || downloadUrl.isBlank()) {
			downloadUrl = provider.generateDownloadUrl(
					new StoredMediaRef(media.getPublicId(), media.getBucketName(), media.getProviderMetadata()),
					Duration.ofSeconds(mediaProperties.getUpload().getDownloadUrlTtlSeconds())
			);
		}
		return mediaMapper.toResponse(media, downloadUrl);
	}

	private void validateUploadRequest(UploadRequestDto request) {
		String mime = request.mimeType().toLowerCase(Locale.ROOT);
		if (!mediaProperties.getUpload().getAllowedMimeTypes().contains(mime)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "MIME type not allowed: " + mime);
		}
		if (request.fileSizeBytes() != null
				&& request.fileSizeBytes() > mediaProperties.getUpload().getMaxFileSizeBytes()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Declared file size exceeds limit");
		}
	}

	private void assertCanAccess(AppUser user, Media media) {
		boolean owner = user.getId().equals(media.getOwnerUserId());
		boolean admin = "ADMIN".equals(user.getRole());
		if (!owner && !admin) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Not allowed to access this media");
		}
	}

	private static String extractExtension(String filename) {
		if (filename == null) {
			return null;
		}
		int idx = filename.lastIndexOf('.');
		if (idx < 0 || idx == filename.length() - 1) {
			return null;
		}
		return filename.substring(idx + 1).toLowerCase(Locale.ROOT);
	}
}

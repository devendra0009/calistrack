package com.davendra.calistrack_backend.media.provider;

import com.davendra.calistrack_backend.media.enums.StorageProviderType;

import java.time.Duration;

/**
 * Strategy interface for object-storage backends.
 * <p>
 * Implementations must never require the Spring MVC layer to accept {@code multipart/form-data}.
 * Clients upload directly using the credentials / signed URL returned from {@link #authorizeUpload}.
 */
public interface StorageProvider {

	StorageProviderType getType();

	UploadAuthorization authorizeUpload(AuthorizeUploadCommand command);

	VerifiedUpload verifyUpload(VerifyUploadCommand command);

	void delete(StoredMediaRef ref);

	String generateDownloadUrl(StoredMediaRef ref, Duration ttl);
}

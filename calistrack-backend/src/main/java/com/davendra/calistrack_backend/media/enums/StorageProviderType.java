package com.davendra.calistrack_backend.media.enums;

/**
 * Pluggable object-storage backends. New providers (Azure, GCS, …) add a value here
 * and a {@link com.davendra.calistrack_backend.media.provider.StorageProvider} bean —
 * {@code MediaService} stays unchanged.
 */
public enum StorageProviderType {
	CLOUDINARY,
	AWS_S3,
	LOCAL
}

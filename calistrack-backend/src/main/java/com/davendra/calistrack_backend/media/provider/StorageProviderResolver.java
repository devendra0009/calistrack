package com.davendra.calistrack_backend.media.provider;

import com.davendra.calistrack_backend.common.exception.ApiException;
import com.davendra.calistrack_backend.media.config.MediaProperties;
import com.davendra.calistrack_backend.media.enums.StorageProviderType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves the configured primary provider and historical providers by type.
 * Switching {@code media.primary-provider} only affects new uploads.
 */
@Component
public class StorageProviderResolver {

	private final MediaProperties mediaProperties;
	private final Map<StorageProviderType, StorageProvider> providers;

	public StorageProviderResolver(MediaProperties mediaProperties, List<StorageProvider> providerList) {
		this.mediaProperties = mediaProperties;
		this.providers = new EnumMap<>(StorageProviderType.class);
		for (StorageProvider provider : providerList) {
			this.providers.put(provider.getType(), provider);
		}
	}

	public StorageProvider primary() {
		return require(mediaProperties.getPrimaryProvider());
	}

	public StorageProvider require(StorageProviderType type) {
		StorageProvider provider = providers.get(type);
		if (provider == null) {
			throw new ApiException(
					HttpStatus.SERVICE_UNAVAILABLE,
					"Storage provider not configured: " + type
			);
		}
		return provider;
	}
}

package com.davendra.calistrack_backend.media.config;

import com.davendra.calistrack_backend.media.enums.StorageProviderType;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Allows {@code media.primary-provider: aws} (and similar aliases) in config.
 */
@Component
@ConfigurationPropertiesBinding
public class StorageProviderTypeConverter implements Converter<String, StorageProviderType> {

	@Override
	public StorageProviderType convert(String source) {
		if (source == null || source.isBlank()) {
			return StorageProviderType.CLOUDINARY;
		}
		String normalized = source.trim().toUpperCase().replace('-', '_');
		return switch (normalized) {
			case "AWS", "S3", "AWS_S3" -> StorageProviderType.AWS_S3;
			case "CLOUDINARY" -> StorageProviderType.CLOUDINARY;
			case "LOCAL" -> StorageProviderType.LOCAL;
			default -> StorageProviderType.valueOf(normalized);
		};
	}
}

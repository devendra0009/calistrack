package com.davendra.calistrack_backend.media.config;

import com.cloudinary.Cloudinary;
import com.davendra.calistrack_backend.media.enums.StorageProviderType;
import com.davendra.calistrack_backend.media.provider.StorageProvider;
import com.davendra.calistrack_backend.media.provider.aws.AwsS3StorageProvider;
import com.davendra.calistrack_backend.media.provider.cloudinary.CloudinaryStorageProvider;
import com.davendra.calistrack_backend.media.provider.local.LocalStorageProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(MediaProperties.class)
public class MediaProviderConfig {

	@Bean
	@ConditionalOnProperty(prefix = "media.cloudinary", name = "enabled", havingValue = "true")
	Cloudinary cloudinary(MediaProperties properties) {
		MediaProperties.Cloudinary cfg = properties.getCloudinary();
		if (!cfg.isConfigured()) {
			throw new IllegalStateException("media.cloudinary.enabled=true but credentials are incomplete");
		}
		Map<String, String> config = new HashMap<>();
		config.put("cloud_name", cfg.getCloudName());
		config.put("api_key", cfg.getApiKey());
		config.put("api_secret", cfg.getApiSecret());
		config.put("secure", "true");
		return new Cloudinary(config);
	}

	@Bean
	@ConditionalOnProperty(prefix = "media.cloudinary", name = "enabled", havingValue = "true")
	StorageProvider cloudinaryStorageProvider(Cloudinary cloudinary, MediaProperties properties) {
		return new CloudinaryStorageProvider(cloudinary, properties);
	}

	@Bean
	@ConditionalOnProperty(prefix = "media.aws", name = "enabled", havingValue = "true")
	S3Client s3Client(MediaProperties properties) {
		MediaProperties.Aws cfg = properties.getAws();
		if (!cfg.isConfigured()) {
			throw new IllegalStateException("media.aws.enabled=true but credentials/bucket are incomplete");
		}
		var builder = S3Client.builder()
				.region(Region.of(cfg.getRegion()))
				.credentialsProvider(StaticCredentialsProvider.create(
						AwsBasicCredentials.create(cfg.getAccessKeyId(), cfg.getSecretAccessKey())
				));
		if (cfg.getEndpointOverride() != null && !cfg.getEndpointOverride().isBlank()) {
			builder.endpointOverride(URI.create(cfg.getEndpointOverride()))
					.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
		}
		return builder.build();
	}

	@Bean
	@ConditionalOnProperty(prefix = "media.aws", name = "enabled", havingValue = "true")
	S3Presigner s3Presigner(MediaProperties properties) {
		MediaProperties.Aws cfg = properties.getAws();
		var builder = S3Presigner.builder()
				.region(Region.of(cfg.getRegion()))
				.credentialsProvider(StaticCredentialsProvider.create(
						AwsBasicCredentials.create(cfg.getAccessKeyId(), cfg.getSecretAccessKey())
				));
		if (cfg.getEndpointOverride() != null && !cfg.getEndpointOverride().isBlank()) {
			builder.endpointOverride(URI.create(cfg.getEndpointOverride()))
					.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
		}
		return builder.build();
	}

	@Bean
	@ConditionalOnProperty(prefix = "media.aws", name = "enabled", havingValue = "true")
	StorageProvider awsS3StorageProvider(S3Client s3Client, S3Presigner s3Presigner, MediaProperties properties) {
		return new AwsS3StorageProvider(s3Client, s3Presigner, properties);
	}

	@Bean
	LocalStorageProvider localStorageProvider(MediaProperties properties) {
		return new LocalStorageProvider(properties);
	}

	@Bean
	MediaProviderStartupValidator mediaProviderStartupValidator(MediaProperties properties) {
		return new MediaProviderStartupValidator(properties);
	}

	static final class MediaProviderStartupValidator {

		MediaProviderStartupValidator(MediaProperties properties) {
			StorageProviderType primary = properties.getPrimaryProvider();
			switch (primary) {
				case CLOUDINARY -> {
					if (!properties.getCloudinary().isEnabled() || !properties.getCloudinary().isConfigured()) {
						throw new IllegalStateException(
								"media.primary-provider=cloudinary requires media.cloudinary.enabled=true and credentials"
						);
					}
				}
				case AWS_S3 -> {
					if (!properties.getAws().isEnabled() || !properties.getAws().isConfigured()) {
						throw new IllegalStateException(
								"media.primary-provider=aws requires media.aws.enabled=true and credentials"
						);
					}
				}
				case LOCAL -> {
					// always available
				}
			}
		}
	}
}

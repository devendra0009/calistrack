package com.davendra.calistrack_backend.media.config;

import com.davendra.calistrack_backend.media.enums.StorageProviderType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "media")
public class MediaProperties {

	/**
	 * Active backend for new uploads: cloudinary | aws_s3 | local
	 * (bound case-insensitively to {@link StorageProviderType}).
	 */
	private StorageProviderType primaryProvider = StorageProviderType.CLOUDINARY;

	private final Upload upload = new Upload();
	private final Cloudinary cloudinary = new Cloudinary();
	private final Aws aws = new Aws();
	private final Local local = new Local();

	public StorageProviderType getPrimaryProvider() {
		return primaryProvider;
	}

	public void setPrimaryProvider(StorageProviderType primaryProvider) {
		this.primaryProvider = primaryProvider;
	}

	public Upload getUpload() {
		return upload;
	}

	public Cloudinary getCloudinary() {
		return cloudinary;
	}

	public Aws getAws() {
		return aws;
	}

	public Local getLocal() {
		return local;
	}

	public static class Upload {
		private long maxFileSizeBytes = 52_428_800L;
		private long downloadUrlTtlSeconds = 3_600L;
		private List<String> allowedMimeTypes = new ArrayList<>(List.of(
				"image/jpeg", "image/png", "image/webp", "image/gif",
				"video/mp4", "video/webm", "video/quicktime",
				"application/pdf"
		));

		public long getMaxFileSizeBytes() {
			return maxFileSizeBytes;
		}

		public void setMaxFileSizeBytes(long maxFileSizeBytes) {
			this.maxFileSizeBytes = maxFileSizeBytes;
		}

		public long getDownloadUrlTtlSeconds() {
			return downloadUrlTtlSeconds;
		}

		public void setDownloadUrlTtlSeconds(long downloadUrlTtlSeconds) {
			this.downloadUrlTtlSeconds = downloadUrlTtlSeconds;
		}

		public List<String> getAllowedMimeTypes() {
			return allowedMimeTypes;
		}

		public void setAllowedMimeTypes(List<String> allowedMimeTypes) {
			this.allowedMimeTypes = allowedMimeTypes;
		}
	}

	public static class Cloudinary {
		private boolean enabled = false;
		private String cloudName = "";
		private String apiKey = "";
		private String apiSecret = "";
		private String folder = "calistrack";
		private String uploadPreset = "";

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public String getCloudName() {
			return cloudName;
		}

		public void setCloudName(String cloudName) {
			this.cloudName = cloudName;
		}

		public String getApiKey() {
			return apiKey;
		}

		public void setApiKey(String apiKey) {
			this.apiKey = apiKey;
		}

		public String getApiSecret() {
			return apiSecret;
		}

		public void setApiSecret(String apiSecret) {
			this.apiSecret = apiSecret;
		}

		public String getFolder() {
			return folder;
		}

		public void setFolder(String folder) {
			this.folder = folder;
		}

		public String getUploadPreset() {
			return uploadPreset;
		}

		public void setUploadPreset(String uploadPreset) {
			this.uploadPreset = uploadPreset;
		}

		public boolean isConfigured() {
			return isPresent(cloudName) && isPresent(apiKey) && isPresent(apiSecret);
		}
	}

	public static class Aws {
		private boolean enabled = false;
		private String region = "us-east-1";
		private String bucket = "";
		private String accessKeyId = "";
		private String secretAccessKey = "";
		private String keyPrefix = "calistrack/";
		private String endpointOverride = "";

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public String getRegion() {
			return region;
		}

		public void setRegion(String region) {
			this.region = region;
		}

		public String getBucket() {
			return bucket;
		}

		public void setBucket(String bucket) {
			this.bucket = bucket;
		}

		public String getAccessKeyId() {
			return accessKeyId;
		}

		public void setAccessKeyId(String accessKeyId) {
			this.accessKeyId = accessKeyId;
		}

		public String getSecretAccessKey() {
			return secretAccessKey;
		}

		public void setSecretAccessKey(String secretAccessKey) {
			this.secretAccessKey = secretAccessKey;
		}

		public String getKeyPrefix() {
			return keyPrefix;
		}

		public void setKeyPrefix(String keyPrefix) {
			this.keyPrefix = keyPrefix;
		}

		public String getEndpointOverride() {
			return endpointOverride;
		}

		public void setEndpointOverride(String endpointOverride) {
			this.endpointOverride = endpointOverride;
		}

		public boolean isConfigured() {
			return isPresent(bucket) && isPresent(accessKeyId) && isPresent(secretAccessKey);
		}
	}

	public static class Local {
		private String basePath = "./media-uploads";
		private String publicBaseUrl = "http://localhost:8084/api/v1/media/local";

		public String getBasePath() {
			return basePath;
		}

		public void setBasePath(String basePath) {
			this.basePath = basePath;
		}

		public String getPublicBaseUrl() {
			return publicBaseUrl;
		}

		public void setPublicBaseUrl(String publicBaseUrl) {
			this.publicBaseUrl = publicBaseUrl;
		}
	}

	private static boolean isPresent(String value) {
		return value != null && !value.isBlank();
	}
}

package com.davendra.calistrack_backend.media.provider.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.davendra.calistrack_backend.common.exception.ApiException;
import com.davendra.calistrack_backend.media.config.MediaProperties;
import com.davendra.calistrack_backend.media.enums.ResourceType;
import com.davendra.calistrack_backend.media.enums.StorageProviderType;
import com.davendra.calistrack_backend.media.provider.AuthorizeUploadCommand;
import com.davendra.calistrack_backend.media.provider.StoredMediaRef;
import com.davendra.calistrack_backend.media.provider.StorageProvider;
import com.davendra.calistrack_backend.media.provider.UploadAuthorization;
import com.davendra.calistrack_backend.media.provider.VerifiedUpload;
import com.davendra.calistrack_backend.media.provider.VerifyUploadCommand;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class CloudinaryStorageProvider implements StorageProvider {

	private final Cloudinary cloudinary;
	private final MediaProperties.Cloudinary config;

	public CloudinaryStorageProvider(Cloudinary cloudinary, MediaProperties properties) {
		this.cloudinary = cloudinary;
		this.config = properties.getCloudinary();
	}

	@Override
	public StorageProviderType getType() {
		return StorageProviderType.CLOUDINARY;
	}

	@Override
	public UploadAuthorization authorizeUpload(AuthorizeUploadCommand command) {
		long timestamp = System.currentTimeMillis() / 1000L;
		String folder = config.getFolder() + "/" + command.mediaType().name().toLowerCase();
		String publicId = command.publicId();

		Map<String, Object> paramsToSign = new TreeMap<>();
		paramsToSign.put("public_id", publicId);
		paramsToSign.put("timestamp", timestamp);
		paramsToSign.put("folder", folder);

		String signature = sign(paramsToSign, config.getApiSecret());

		Map<String, String> formFields = new LinkedHashMap<>();
		formFields.put("api_key", config.getApiKey());
		formFields.put("timestamp", String.valueOf(timestamp));
		formFields.put("signature", signature);
		formFields.put("public_id", publicId);
		formFields.put("folder", folder);
		if (config.getUploadPreset() != null && !config.getUploadPreset().isBlank()) {
			formFields.put("upload_preset", config.getUploadPreset());
		}

		String resourcePath = toCloudinaryResource(command.resourceType());
		String uploadUrl = "https://api.cloudinary.com/v1_1/"
				+ config.getCloudName()
				+ "/"
				+ resourcePath
				+ "/upload";

		String fullPublicId = folder + "/" + publicId;

		Map<String, Object> metadata = new LinkedHashMap<>();
		metadata.put("folder", folder);
		metadata.put("cloudName", config.getCloudName());
		metadata.put("resourcePath", resourcePath);
		metadata.put("shortPublicId", publicId);

		return new UploadAuthorization(
				uploadUrl,
				"POST",
				Map.of(),
				formFields,
				config.getCloudName(),
				fullPublicId,
				metadata
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public VerifiedUpload verifyUpload(VerifyUploadCommand command) {
		try {
			String resourceType = resolveResourceType(command);
			Map<String, Object> result = cloudinary.api().resource(
					command.publicId(),
					ObjectUtils.asMap(
							"resource_type", resourceType,
							"type", "upload"
					)
			);

			Long bytes = toLong(result.get("bytes"));
			if (command.expectedMaxBytes() != null && bytes != null && bytes > command.expectedMaxBytes()) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "Uploaded file exceeds allowed size");
			}

			String format = stringOrNull(result.get("format"));
			String mimeType = stringOrNull(result.get("resource_type"));
			if (command.clientUploadResult() != null && command.clientUploadResult().get("mimeType") != null) {
				mimeType = String.valueOf(command.clientUploadResult().get("mimeType"));
			} else if (format != null) {
				mimeType = guessMime(resourceType, format);
			}

			String secureUrl = stringOrNull(result.get("secure_url"));
			String thumbnailUrl = secureUrl;
			if ("video".equals(resourceType) && secureUrl != null) {
				thumbnailUrl = secureUrl.replace("/upload/", "/upload/so_0/");
			}

			Map<String, Object> metadata = new LinkedHashMap<>(result);
			metadata.remove("api_key");

			return new VerifiedUpload(
					stringOrNull(result.get("public_id")) != null
							? String.valueOf(result.get("public_id"))
							: command.publicId(),
					config.getCloudName(),
					secureUrl,
					thumbnailUrl,
					mimeType != null ? mimeType : command.expectedMimeType(),
					bytes,
					toInteger(result.get("width")),
					toInteger(result.get("height")),
					toDecimal(result.get("duration")),
					fromCloudinaryResource(resourceType),
					stringOrNull(result.get("etag")),
					metadata
			);
		} catch (ApiException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ApiException(
					HttpStatus.BAD_REQUEST,
					"Cloudinary upload verification failed: " + ex.getMessage()
			);
		}
	}

	@Override
	public void delete(StoredMediaRef ref) {
		try {
			String resourceType = "image";
			if (ref.providerMetadata() != null && ref.providerMetadata().get("resource_type") != null) {
				resourceType = String.valueOf(ref.providerMetadata().get("resource_type"));
			}
			cloudinary.uploader().destroy(
					ref.publicId(),
					ObjectUtils.asMap("resource_type", resourceType, "invalidate", true)
			);
		} catch (Exception ex) {
			throw new ApiException(HttpStatus.BAD_GATEWAY, "Failed to delete Cloudinary asset: " + ex.getMessage());
		}
	}

	@Override
	public String generateDownloadUrl(StoredMediaRef ref, Duration ttl) {
		if (ref.providerMetadata() != null && ref.providerMetadata().get("secure_url") != null) {
			return String.valueOf(ref.providerMetadata().get("secure_url"));
		}
		String resourceType = "image";
		if (ref.providerMetadata() != null && ref.providerMetadata().get("resource_type") != null) {
			resourceType = String.valueOf(ref.providerMetadata().get("resource_type"));
		}
		return cloudinary.url()
				.resourceType(resourceType)
				.secure(true)
				.generate(ref.publicId());
	}

	private String sign(Map<String, Object> paramsToSign, String apiSecret) {
		StringBuilder toSign = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String, Object> entry : paramsToSign.entrySet()) {
			if (entry.getValue() == null || String.valueOf(entry.getValue()).isBlank()) {
				continue;
			}
			if (!first) {
				toSign.append('&');
			}
			toSign.append(entry.getKey()).append('=').append(entry.getValue());
			first = false;
		}
		toSign.append(apiSecret);
		return sha1Hex(toSign.toString());
	}

	private static String sha1Hex(String input) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-1 not available", e);
		}
	}

	private String resolveResourceType(VerifyUploadCommand command) {
		if (command.clientUploadResult() != null && command.clientUploadResult().get("resourceType") != null) {
			String value = String.valueOf(command.clientUploadResult().get("resourceType")).toLowerCase();
			if (!"auto".equals(value)) {
				return value;
			}
		}
		if (command.expectedMimeType() != null && command.expectedMimeType().startsWith("video/")) {
			return "video";
		}
		if (command.expectedMimeType() != null && command.expectedMimeType().startsWith("image/")) {
			return "image";
		}
		if (command.expectedMimeType() != null && !command.expectedMimeType().startsWith("image/")
				&& !command.expectedMimeType().startsWith("video/")) {
			return "raw";
		}
		return "image";
	}

	private static String toCloudinaryResource(ResourceType resourceType) {
		return switch (resourceType) {
			case VIDEO -> "video";
			case RAW -> "raw";
			case IMAGE, AUTO -> "image";
		};
	}

	private static ResourceType fromCloudinaryResource(String resourceType) {
		if (resourceType == null) {
			return ResourceType.AUTO;
		}
		return switch (resourceType.toLowerCase()) {
			case "video" -> ResourceType.VIDEO;
			case "raw" -> ResourceType.RAW;
			case "image" -> ResourceType.IMAGE;
			default -> ResourceType.AUTO;
		};
	}

	private static String guessMime(String resourceType, String format) {
		if ("video".equals(resourceType)) {
			return "video/" + format;
		}
		if ("image".equals(resourceType)) {
			return "image/" + format;
		}
		return "application/octet-stream";
	}

	private static String stringOrNull(Object value) {
		return value == null ? null : String.valueOf(value);
	}

	private static Long toLong(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Number number) {
			return number.longValue();
		}
		return Long.parseLong(String.valueOf(value));
	}

	private static Integer toInteger(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Number number) {
			return number.intValue();
		}
		return Integer.parseInt(String.valueOf(value));
	}

	private static BigDecimal toDecimal(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof BigDecimal bd) {
			return bd;
		}
		if (value instanceof Number number) {
			return BigDecimal.valueOf(number.doubleValue());
		}
		return new BigDecimal(String.valueOf(value));
	}
}

package com.davendra.calistrack_backend.media.provider.aws;

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
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

public class AwsS3StorageProvider implements StorageProvider {

	private final S3Client s3Client;
	private final S3Presigner s3Presigner;
	private final MediaProperties.Aws config;

	public AwsS3StorageProvider(S3Client s3Client, S3Presigner s3Presigner, MediaProperties properties) {
		this.s3Client = s3Client;
		this.s3Presigner = s3Presigner;
		this.config = properties.getAws();
	}

	@Override
	public StorageProviderType getType() {
		return StorageProviderType.AWS_S3;
	}

	@Override
	public UploadAuthorization authorizeUpload(AuthorizeUploadCommand command) {
		String key = buildObjectKey(command);
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(config.getBucket())
				.key(key)
				.contentType(command.mimeType())
				.build();

		var presigned = s3Presigner.presignPutObject(PutObjectPresignRequest.builder()
				.signatureDuration(Duration.ofMinutes(15))
				.putObjectRequest(putObjectRequest)
				.build());

		Map<String, Object> metadata = new LinkedHashMap<>();
		metadata.put("key", key);
		metadata.put("region", config.getRegion());

		return new UploadAuthorization(
				presigned.url().toString(),
				"PUT",
				Map.of("Content-Type", command.mimeType()),
				Map.of(),
				config.getBucket(),
				key,
				metadata
		);
	}

	@Override
	public VerifiedUpload verifyUpload(VerifyUploadCommand command) {
		String key = command.publicId();
		try {
			HeadObjectResponse head = s3Client.headObject(HeadObjectRequest.builder()
					.bucket(config.getBucket())
					.key(key)
					.build());

			Long size = head.contentLength();
			if (command.expectedMaxBytes() != null && size != null && size > command.expectedMaxBytes()) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "Uploaded file exceeds allowed size");
			}

			String contentType = head.contentType() != null ? head.contentType() : command.expectedMimeType();
			String etag = head.eTag() != null ? head.eTag().replace("\"", "") : null;

			Map<String, Object> metadata = new LinkedHashMap<>();
			metadata.put("key", key);
			metadata.put("eTag", etag);
			metadata.put("contentType", contentType);

			String secureUrl = s3Presigner.presignGetObject(GetObjectPresignRequest.builder()
					.signatureDuration(Duration.ofHours(1))
					.getObjectRequest(b -> b.bucket(config.getBucket()).key(key))
					.build()).url().toString();

			return new VerifiedUpload(
					key,
					config.getBucket(),
					secureUrl,
					null,
					contentType,
					size,
					null,
					null,
					null,
					inferResourceType(contentType),
					etag,
					metadata
			);
		} catch (NoSuchKeyException ex) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "S3 object not found — upload may have failed");
		} catch (ApiException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "S3 upload verification failed: " + ex.getMessage());
		}
	}

	@Override
	public void delete(StoredMediaRef ref) {
		try {
			s3Client.deleteObject(DeleteObjectRequest.builder()
					.bucket(ref.bucketName() != null ? ref.bucketName() : config.getBucket())
					.key(ref.publicId())
					.build());
		} catch (Exception ex) {
			throw new ApiException(HttpStatus.BAD_GATEWAY, "Failed to delete S3 object: " + ex.getMessage());
		}
	}

	@Override
	public String generateDownloadUrl(StoredMediaRef ref, Duration ttl) {
		String bucket = ref.bucketName() != null ? ref.bucketName() : config.getBucket();
		return s3Presigner.presignGetObject(GetObjectPresignRequest.builder()
				.signatureDuration(ttl)
				.getObjectRequest(b -> b.bucket(bucket).key(ref.publicId()))
				.build()).url().toString();
	}

	private String buildObjectKey(AuthorizeUploadCommand command) {
		String prefix = config.getKeyPrefix() == null ? "" : config.getKeyPrefix();
		if (!prefix.isEmpty() && !prefix.endsWith("/")) {
			prefix = prefix + "/";
		}
		String ext = command.extension() == null || command.extension().isBlank()
				? ""
				: "." + command.extension().replace(".", "");
		return prefix
				+ command.mediaType().name().toLowerCase()
				+ "/"
				+ command.ownerUserId()
				+ "/"
				+ command.publicId()
				+ ext;
	}

	private static ResourceType inferResourceType(String mimeType) {
		if (mimeType == null) {
			return ResourceType.RAW;
		}
		if (mimeType.startsWith("image/")) {
			return ResourceType.IMAGE;
		}
		if (mimeType.startsWith("video/")) {
			return ResourceType.VIDEO;
		}
		return ResourceType.RAW;
	}
}

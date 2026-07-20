package com.davendra.calistrack_backend.media.provider.local;

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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dev/test provider. Clients upload with HTTP PUT + raw body (never multipart/form-data).
 */
public class LocalStorageProvider implements StorageProvider {

	private final MediaProperties.Local config;
	private final Path root;
	private final ConcurrentHashMap<String, PendingLocalUpload> pending = new ConcurrentHashMap<>();

	public LocalStorageProvider(MediaProperties properties) {
		this.config = properties.getLocal();
		this.root = Path.of(config.getBasePath()).toAbsolutePath().normalize();
		try {
			Files.createDirectories(root);
		} catch (IOException e) {
			throw new IllegalStateException("Cannot create local media directory: " + root, e);
		}
	}

	@Override
	public StorageProviderType getType() {
		return StorageProviderType.LOCAL;
	}

	@Override
	public UploadAuthorization authorizeUpload(AuthorizeUploadCommand command) {
		String token = UUID.randomUUID().toString().replace("-", "");
		String relativePath = command.mediaType().name().toLowerCase()
				+ "/"
				+ command.ownerUserId()
				+ "/"
				+ command.publicId()
				+ (command.extension() == null || command.extension().isBlank()
				? ""
				: "." + command.extension().replace(".", ""));

		PendingLocalUpload pendingUpload = new PendingLocalUpload(
				command.mediaId(),
				command.ownerUserId(),
				relativePath,
				command.mimeType(),
				command.declaredFileSizeBytes(),
				Instant.now().plus(Duration.ofMinutes(15))
		);
		pending.put(token, pendingUpload);

		String uploadUrl = trimTrailingSlash(config.getPublicBaseUrl()) + "/upload/" + token;

		Map<String, Object> metadata = new LinkedHashMap<>();
		metadata.put("relativePath", relativePath);
		metadata.put("uploadToken", token);

		return new UploadAuthorization(
				uploadUrl,
				"PUT",
				Map.of("Content-Type", command.mimeType()),
				Map.of(),
				"local",
				relativePath,
				metadata
		);
	}

	/**
	 * Called by {@link com.davendra.calistrack_backend.media.controller.LocalStorageUploadController}.
	 * Accepts a raw body stream — not multipart.
	 */
	public void acceptRawUpload(String token, String contentType, long contentLength, InputStream body) {
		PendingLocalUpload pendingUpload = pending.get(token);
		if (pendingUpload == null || pendingUpload.expiresAt().isBefore(Instant.now())) {
			pending.remove(token);
			throw new ApiException(HttpStatus.BAD_REQUEST, "Local upload token is invalid or expired");
		}
		if (pendingUpload.expectedMimeType() != null
				&& contentType != null
				&& !pendingUpload.expectedMimeType().equalsIgnoreCase(contentType)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Content-Type does not match authorized upload");
		}
		if (pendingUpload.maxBytes() != null && contentLength > pendingUpload.maxBytes()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Uploaded file exceeds allowed size");
		}

		Path target = root.resolve(pendingUpload.relativePath()).normalize();
		if (!target.startsWith(root)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid local upload path");
		}

		try {
			Files.createDirectories(target.getParent());
			Files.copy(body, target, StandardCopyOption.REPLACE_EXISTING);
			pendingUpload.markStored(Files.size(target), sha256(target));
		} catch (IOException e) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store local upload");
		}
	}

	@Override
	public VerifiedUpload verifyUpload(VerifyUploadCommand command) {
		String relativePath = command.publicId();
		Path target = root.resolve(relativePath).normalize();
		if (!target.startsWith(root) || !Files.isRegularFile(target)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Local file not found — upload may have failed");
		}

		try {
			long size = Files.size(target);
			if (command.expectedMaxBytes() != null && size > command.expectedMaxBytes()) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "Uploaded file exceeds allowed size");
			}

			String checksum = sha256(target);
			String secureUrl = trimTrailingSlash(config.getPublicBaseUrl()) + "/files/" + relativePath.replace("\\", "/");

			Map<String, Object> metadata = new LinkedHashMap<>();
			metadata.put("relativePath", relativePath);
			metadata.put("absolutePath", target.toString());

			return new VerifiedUpload(
					relativePath,
					"local",
					secureUrl,
					null,
					command.expectedMimeType(),
					size,
					null,
					null,
					null,
					inferResourceType(command.expectedMimeType()),
					checksum,
					metadata
			);
		} catch (ApiException ex) {
			throw ex;
		} catch (IOException e) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Local upload verification failed");
		}
	}

	@Override
	public void delete(StoredMediaRef ref) {
		Path target = root.resolve(ref.publicId()).normalize();
		if (!target.startsWith(root)) {
			return;
		}
		try {
			Files.deleteIfExists(target);
		} catch (IOException e) {
			throw new ApiException(HttpStatus.BAD_GATEWAY, "Failed to delete local file");
		}
	}

	@Override
	public String generateDownloadUrl(StoredMediaRef ref, Duration ttl) {
		return trimTrailingSlash(config.getPublicBaseUrl()) + "/files/" + ref.publicId().replace("\\", "/");
	}

	public Path resolveFile(String relativePath) {
		Path target = root.resolve(relativePath).normalize();
		if (!target.startsWith(root) || !Files.isRegularFile(target)) {
			throw new ApiException(HttpStatus.NOT_FOUND, "Local media file not found");
		}
		return target;
	}

	private static String sha256(Path file) throws IOException {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			try (InputStream in = Files.newInputStream(file)) {
				byte[] buffer = new byte[8192];
				int read;
				while ((read = in.read(buffer)) > 0) {
					digest.update(buffer, 0, read);
				}
			}
			return HexFormat.of().formatHex(digest.digest());
		} catch (Exception e) {
			throw new IOException("Unable to checksum file", e);
		}
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

	private static String trimTrailingSlash(String value) {
		if (value == null) {
			return "";
		}
		return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
	}

	private static final class PendingLocalUpload {
		private final UUID mediaId;
		private final UUID ownerUserId;
		private final String relativePath;
		private final String expectedMimeType;
		private final Long maxBytes;
		private final Instant expiresAt;
		private volatile boolean stored;
		private volatile Long storedBytes;
		private volatile String checksum;

		private PendingLocalUpload(
				UUID mediaId,
				UUID ownerUserId,
				String relativePath,
				String expectedMimeType,
				Long maxBytes,
				Instant expiresAt
		) {
			this.mediaId = mediaId;
			this.ownerUserId = ownerUserId;
			this.relativePath = relativePath;
			this.expectedMimeType = expectedMimeType;
			this.maxBytes = maxBytes;
			this.expiresAt = expiresAt;
		}

		UUID mediaId() {
			return mediaId;
		}

		UUID ownerUserId() {
			return ownerUserId;
		}

		String relativePath() {
			return relativePath;
		}

		String expectedMimeType() {
			return expectedMimeType;
		}

		Long maxBytes() {
			return maxBytes;
		}

		Instant expiresAt() {
			return expiresAt;
		}

		void markStored(long bytes, String checksum) {
			this.stored = true;
			this.storedBytes = bytes;
			this.checksum = checksum;
		}
	}
}

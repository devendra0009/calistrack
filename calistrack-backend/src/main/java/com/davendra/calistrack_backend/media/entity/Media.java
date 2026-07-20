package com.davendra.calistrack_backend.media.entity;

import com.davendra.calistrack_backend.media.enums.MediaType;
import com.davendra.calistrack_backend.media.enums.MediaVisibility;
import com.davendra.calistrack_backend.media.enums.ResourceType;
import com.davendra.calistrack_backend.media.enums.StorageProviderType;
import com.davendra.calistrack_backend.media.enums.UploadStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "media")
@Getter
@Setter
public class Media {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "owner_user_id", nullable = false)
	private UUID ownerUserId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private StorageProviderType provider;

	@Column(name = "bucket_name", length = 255)
	private String bucketName;

	@Column(name = "public_id", nullable = false, length = 512)
	private String publicId;

	@Column(name = "original_filename", length = 512)
	private String originalFilename;

	@Column(name = "mime_type", nullable = false, length = 128)
	private String mimeType;

	@Column(length = 32)
	private String extension;

	@Column(name = "file_size_bytes")
	private Long fileSizeBytes;

	private Integer width;

	private Integer height;

	@Column(name = "duration_seconds", precision = 12, scale = 3)
	private BigDecimal durationSeconds;

	@Enumerated(EnumType.STRING)
	@Column(name = "resource_type", nullable = false, length = 32)
	private ResourceType resourceType;

	@Enumerated(EnumType.STRING)
	@Column(name = "media_type", nullable = false, length = 64)
	private MediaType mediaType;

	@Column(name = "secure_url", columnDefinition = "text")
	private String secureUrl;

	@Column(name = "thumbnail_url", columnDefinition = "text")
	private String thumbnailUrl;

	@Column(length = 128)
	private String checksum;

	@Enumerated(EnumType.STRING)
	@Column(name = "upload_status", nullable = false, length = 32)
	private UploadStatus uploadStatus;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private MediaVisibility visibility;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "provider_metadata", nullable = false, columnDefinition = "jsonb")
	private Map<String, Object> providerMetadata = new HashMap<>();

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
		if (providerMetadata == null) {
			providerMetadata = new HashMap<>();
		}
		if (uploadStatus == null) {
			uploadStatus = UploadStatus.PENDING;
		}
		if (visibility == null) {
			visibility = MediaVisibility.PRIVATE;
		}
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}

	public boolean isDeleted() {
		return deletedAt != null;
	}

	public void softDelete() {
		this.deletedAt = Instant.now();
		this.uploadStatus = UploadStatus.DELETED;
	}
}

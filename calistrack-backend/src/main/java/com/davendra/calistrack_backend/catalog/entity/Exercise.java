package com.davendra.calistrack_backend.catalog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "exercise")
@Getter
@Setter
public class Exercise {

	public static final String STATUS_ACTIVE = "ACTIVE";

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, unique = true, length = 120)
	private String name;

	@Column(columnDefinition = "text")
	private String description;

	@Column(nullable = false, length = 20)
	private String category;

	@Column(name = "metric_type", nullable = false, length = 20)
	private String metricType;

	@Column(nullable = false, length = 20)
	private String difficulty;

	@Column(name = "thumbnail_url", columnDefinition = "text")
	private String thumbnailUrl;

	@Column(name = "demo_video_url", columnDefinition = "text")
	private String demoVideoUrl;

	@Column(nullable = false, length = 20)
	private String status = STATUS_ACTIVE;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}
}

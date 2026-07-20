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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "node")
@Getter
@Setter
public class Node {

	public static final String STATUS_ACTIVE = "ACTIVE";

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, unique = true, length = 120)
	private String name;

	@Column(columnDefinition = "text")
	private String description;

	@Column(name = "node_type", nullable = false, length = 20)
	private String nodeType;

	@Column(name = "exercise_id", nullable = false)
	private UUID exerciseId;

	@Column(name = "target_value", nullable = false, precision = 10, scale = 2)
	private BigDecimal targetValue;

	@Column(nullable = false, length = 5)
	private String operator;

	@Column(name = "unit_label", nullable = false, length = 20)
	private String unitLabel;

	@Column(nullable = false, length = 20)
	private String difficulty;

	@Column(name = "xp_reward")
	private Integer xpReward;

	@Column(name = "estimated_minutes")
	private Integer estimatedMinutes;

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

	public boolean isActive() {
		return STATUS_ACTIVE.equals(status);
	}
}

package com.davendra.calistrack_backend.progress.entity;

import com.davendra.calistrack_backend.catalog.entity.Node;
import com.davendra.calistrack_backend.progress.enums.UserNodeStatus;
import com.davendra.calistrack_backend.user.entity.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
		name = "user_node",
		uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "node_id" })
)
@Getter
@Setter
public class UserNode {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "node_id", nullable = false)
	private Node node;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private UserNodeStatus status;

	@Column(name = "progress_percentage", nullable = false, precision = 5, scale = 2)
	private BigDecimal progressPercentage = BigDecimal.ZERO;

	@Column(nullable = false)
	private boolean verified = false;

	@Column(name = "verified_by_ai", nullable = false)
	private boolean verifiedByAi = false;

	@Column(name = "last_attempt_at")
	private Instant lastAttemptAt;

	@Column(name = "best_score", precision = 5, scale = 2)
	private BigDecimal bestScore;

	@Column(name = "current_score", precision = 5, scale = 2)
	private BigDecimal currentScore;

	@Column(name = "unlocked_at")
	private Instant unlockedAt;

	@Column(length = 20)
	private String mastery;

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

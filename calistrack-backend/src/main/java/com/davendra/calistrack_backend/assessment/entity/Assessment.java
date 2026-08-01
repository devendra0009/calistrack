package com.davendra.calistrack_backend.assessment.entity;

import com.davendra.calistrack_backend.assessment.enums.AssessmentStatus;
import com.davendra.calistrack_backend.catalog.entity.Node;
import com.davendra.calistrack_backend.user.entity.AppUser;
import com.davendra.calistrack_backend.workout.entity.WorkoutSession;
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
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "assessment")
@Getter
@Setter
public class Assessment {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "node_id", nullable = false)
	private Node node;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workout_session_id")
	private WorkoutSession workoutSession;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AssessmentStatus status = AssessmentStatus.NOT_ATTEMPTED;

	@Column(name = "video_url", columnDefinition = "text")
	private String videoUrl;

	@Column(name = "attempt_score", precision = 5, scale = 2)
	private BigDecimal attemptScore;

	@Column(name = "ai_form_score", precision = 5, scale = 2)
	private BigDecimal aiFormScore;

	@Column(nullable = false)
	private boolean verified = false;

	@Column(columnDefinition = "text")
	private String remarks;

	@Column(name = "performed_at", nullable = false)
	private Instant performedAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
		if (performedAt == null) {
			performedAt = now;
		}
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}
}

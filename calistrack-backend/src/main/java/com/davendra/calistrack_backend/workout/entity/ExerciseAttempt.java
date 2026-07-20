package com.davendra.calistrack_backend.workout.entity;

import com.davendra.calistrack_backend.catalog.entity.WorkoutExercise;
import com.davendra.calistrack_backend.workout.enums.ExerciseAttemptStatus;
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
		name = "exercise_attempt",
		uniqueConstraints = @UniqueConstraint(
				name = "uq_session_workout_exercise",
				columnNames = {"workout_session_id", "workout_exercise_id"}
		)
)
@Getter
@Setter
public class ExerciseAttempt {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "workout_session_id", nullable = false)
	private WorkoutSession workoutSession;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "workout_exercise_id", nullable = false)
	private WorkoutExercise workoutExercise;

	@Column(name = "actual_sets")
	private Integer actualSets;

	@Column(name = "actual_reps")
	private Integer actualReps;

	@Column(name = "actual_hold_seconds")
	private Integer actualHoldSeconds;

	@Column(name = "actual_rest_seconds")
	private Integer actualRestSeconds;

	@Column(name = "video_url", columnDefinition = "text")
	private String videoUrl;

	@Column(columnDefinition = "text")
	private String notes;

	@Column(name = "ai_score", precision = 5, scale = 2)
	private BigDecimal aiScore;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ExerciseAttemptStatus status = ExerciseAttemptStatus.IN_PROGRESS;

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

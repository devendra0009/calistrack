package com.davendra.calistrack_backend.catalog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
		name = "workout_exercise",
		uniqueConstraints = @UniqueConstraint(
				name = "uq_workout_sequence",
				columnNames = {"workout_id", "sequence"}
		)
)
@Getter
@Setter
public class WorkoutExercise {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "workout_id", nullable = false)
	private Workout workout;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "exercise_id", nullable = false)
	private Exercise exercise;

	@Column(nullable = false)
	private int sequence;

	@Column(name = "target_sets")
	private Integer targetSets;

	@Column(name = "target_reps")
	private Integer targetReps;

	@Column(name = "target_hold_seconds")
	private Integer targetHoldSeconds;

	@Column(name = "target_rest_seconds")
	private Integer targetRestSeconds;

	@Column(columnDefinition = "text")
	private String notes;

	@Column(name = "demo_video_url", columnDefinition = "text")
	private String demoVideoUrl;
}

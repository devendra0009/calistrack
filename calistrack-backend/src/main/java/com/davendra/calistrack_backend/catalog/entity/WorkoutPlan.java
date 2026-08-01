package com.davendra.calistrack_backend.catalog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workout_plan")
@Getter
@Setter
public class WorkoutPlan {

	public static final String STATUS_ACTIVE = "ACTIVE";
	public static final String STATUS_DEPRECATED = "DEPRECATED";
	public static final String KIND_SKILL = "SKILL";
	public static final String KIND_DAILY_ROUTINE = "DAILY_ROUTINE";
	public static final String CODE_MORNING_STRETCH = "morning_stretch";

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "node_id", nullable = false)
	private Node node;

	@Column(nullable = false, length = 160)
	private String title;

	@Column(columnDefinition = "text")
	private String description;

	/** SKILL (path plan) or DAILY_ROUTINE (e.g. morning stretch). */
	@Column(nullable = false, length = 20)
	private String kind = KIND_SKILL;

	/** Stable lookup key, e.g. morning_stretch. Unique when set. */
	@Column(length = 64)
	private String code;

	@Column(name = "duration_days", nullable = false)
	private int durationDays;

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

	public boolean isDailyRoutine() {
		return KIND_DAILY_ROUTINE.equals(kind);
	}
}

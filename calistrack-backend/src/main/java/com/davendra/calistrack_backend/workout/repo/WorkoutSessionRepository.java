package com.davendra.calistrack_backend.workout.repo;

import com.davendra.calistrack_backend.workout.entity.WorkoutSession;
import com.davendra.calistrack_backend.workout.enums.WorkoutSessionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, UUID> {

	boolean existsByUserId(UUID userId);

	boolean existsByUserIdAndWorkout_Kind(UUID userId, String workoutKind);

	boolean existsByUserIdAndWorkout_KindAndStatusIn(
			UUID userId,
			String workoutKind,
			Collection<WorkoutSessionStatus> statuses
	);

	@EntityGraph(attributePaths = {"workout", "workout.goalNode", "user", "planEnrollment", "planEnrollment.plan"})
	Optional<WorkoutSession> findFirstByUserIdAndWorkout_KindAndStatusInOrderByCreatedAtDesc(
			UUID userId,
			String workoutKind,
			Collection<WorkoutSessionStatus> statuses
	);

	@EntityGraph(attributePaths = {"workout", "workout.goalNode", "user", "planEnrollment", "planEnrollment.plan"})
	Optional<WorkoutSession> findFirstByUserIdAndWorkout_KindAndStatusOrderByCompletedAtDesc(
			UUID userId,
			String workoutKind,
			WorkoutSessionStatus status
	);

	@EntityGraph(attributePaths = {"workout", "workout.goalNode", "user", "planEnrollment", "planEnrollment.plan"})
	Optional<WorkoutSession> findFirstByUserIdAndWorkout_KindOrderByCreatedAtDesc(UUID userId, String workoutKind);

	@EntityGraph(attributePaths = {"workout", "workout.goalNode", "user", "planEnrollment", "planEnrollment.plan"})
	List<WorkoutSession> findByUserIdOrderByCreatedAtDesc(UUID userId);

	@EntityGraph(attributePaths = {"workout", "workout.goalNode", "user", "planEnrollment", "planEnrollment.plan"})
	Optional<WorkoutSession> findWithDetailsById(UUID id);

	/**
	 * Lean stretch lookup — workout + goalNode only (no enrollment graph).
	 */
	@EntityGraph(attributePaths = {"workout", "workout.goalNode"})
	@Query("""
			select s from WorkoutSession s
			where s.user.id = :userId
			  and s.workout.kind = :kind
			  and s.status in :statuses
			order by s.createdAt desc
			limit 1
			""")
	Optional<WorkoutSession> findLatestOpenStretch(
			@Param("userId") UUID userId,
			@Param("kind") String kind,
			@Param("statuses") Collection<WorkoutSessionStatus> statuses
	);

	@EntityGraph(attributePaths = {"workout", "workout.goalNode"})
	@Query("""
			select s from WorkoutSession s
			where s.user.id = :userId
			  and s.workout.kind = :kind
			  and s.status = :status
			order by s.completedAt desc
			limit 1
			""")
	Optional<WorkoutSession> findLatestCompletedStretch(
			@Param("userId") UUID userId,
			@Param("kind") String kind,
			@Param("status") WorkoutSessionStatus status
	);
}

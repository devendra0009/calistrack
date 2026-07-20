package com.davendra.calistrack_backend.workout.repo;

import com.davendra.calistrack_backend.workout.entity.WorkoutSession;
import com.davendra.calistrack_backend.workout.enums.WorkoutSessionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, UUID> {

	boolean existsByUserId(UUID userId);

	boolean existsByUserIdAndStatusIn(UUID userId, Collection<WorkoutSessionStatus> statuses);

	@EntityGraph(attributePaths = {"workout", "workout.goalNode", "user"})
	Optional<WorkoutSession> findFirstByUserIdAndStatusInOrderByCreatedAtDesc(
			UUID userId,
			Collection<WorkoutSessionStatus> statuses
	);

	@EntityGraph(attributePaths = {"workout", "workout.goalNode", "user"})
	Optional<WorkoutSession> findFirstByUserIdOrderByCreatedAtDesc(UUID userId);

	@EntityGraph(attributePaths = {"workout", "workout.goalNode", "user"})
	Optional<WorkoutSession> findWithDetailsById(UUID id);
}

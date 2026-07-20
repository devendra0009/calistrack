package com.davendra.calistrack_backend.catalog.repo;

import com.davendra.calistrack_backend.catalog.entity.Workout;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkoutRepository extends JpaRepository<Workout, UUID> {

	/**
	 * Fetch workout + goal node in one query (avoids LazyInitializationException on goalNode).
	 */
	@EntityGraph(attributePaths = "goalNode")
	Optional<Workout> findFirstByGoalNode_IdAndStatusOrderByCreatedAtAsc(UUID goalNodeId, String status);

	@Query("""
			select w from Workout w
			join fetch w.goalNode
			where w.id = :id
			""")
	Optional<Workout> findByIdWithGoalNode(@Param("id") UUID id);

	@EntityGraph(attributePaths = "goalNode")
	List<Workout> findAllByOrderByTitleAsc();

	@EntityGraph(attributePaths = "goalNode")
	List<Workout> findByStatusOrderByTitleAsc(String status);

	@EntityGraph(attributePaths = "goalNode")
	List<Workout> findByGoalNode_IdOrderByTitleAsc(UUID goalNodeId);
}

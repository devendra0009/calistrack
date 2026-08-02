package com.davendra.calistrack_backend.catalog.repo;

import com.davendra.calistrack_backend.catalog.entity.WorkoutPlan;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, UUID> {

	@EntityGraph(attributePaths = "node")
	List<WorkoutPlan> findAllByOrderByTitleAsc();

	@EntityGraph(attributePaths = "node")
	List<WorkoutPlan> findByStatusOrderByTitleAsc(String status);

	@EntityGraph(attributePaths = "node")
	List<WorkoutPlan> findByNode_IdOrderByTitleAsc(UUID nodeId);

	@EntityGraph(attributePaths = "node")
	Optional<WorkoutPlan> findFirstByNode_IdAndStatusOrderByCreatedAtAsc(UUID nodeId, String status);

	@EntityGraph(attributePaths = "node")
	Optional<WorkoutPlan> findFirstByCodeAndStatus(String code, String status);

	/** Stretch catalog lookup — no node join. */
	@Query("""
			select p from WorkoutPlan p
			where p.code = :code and p.status = :status
			""")
	Optional<WorkoutPlan> findLeanByCodeAndStatus(
			@Param("code") String code,
			@Param("status") String status
	);

	boolean existsByNode_IdAndStatus(UUID nodeId, String status);

	@Query("""
			select p from WorkoutPlan p join fetch p.node where p.id = :id
			""")
	Optional<WorkoutPlan> findByIdWithNode(@Param("id") UUID id);
}

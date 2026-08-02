package com.davendra.calistrack_backend.catalog.repo;

import com.davendra.calistrack_backend.catalog.entity.WorkoutPlanDay;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkoutPlanDayRepository extends JpaRepository<WorkoutPlanDay, UUID> {

	@EntityGraph(attributePaths = {"workout", "workout.goalNode"})
	List<WorkoutPlanDay> findByPlan_IdOrderByDayNumberAsc(UUID planId);

	@EntityGraph(attributePaths = {"workout", "workout.goalNode", "plan", "plan.node"})
	Optional<WorkoutPlanDay> findByPlan_IdAndDayNumber(UUID planId, int dayNumber);

	/** Stretch today — workout (+ goalNode for startSession DTO); no plan.node. */
	@EntityGraph(attributePaths = {"workout", "workout.goalNode"})
	@Query("""
			select d from WorkoutPlanDay d
			where d.plan.id = :planId and d.dayNumber = :dayNumber
			""")
	Optional<WorkoutPlanDay> findLeanByPlanIdAndDayNumber(
			@Param("planId") UUID planId,
			@Param("dayNumber") int dayNumber
	);

	Optional<WorkoutPlanDay> findByIdAndPlan_Id(UUID id, UUID planId);

	long countByPlan_Id(UUID planId);

	void deleteByPlan_Id(UUID planId);
}

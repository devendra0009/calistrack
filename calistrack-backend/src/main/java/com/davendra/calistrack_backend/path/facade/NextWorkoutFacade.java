package com.davendra.calistrack_backend.path.facade;

import com.davendra.calistrack_backend.path.dto.PlacementResult;
import com.davendra.calistrack_backend.path.dto.WorkoutAssignment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Single entry point for “what workout should the user do now?” for a goal.
 * Callers pass the current focus node (from placement or progress); they do not
 * look up workouts themselves.
 */
public interface NextWorkoutFacade {

	/**
	 * Resolve the active workout for the user's current focus on a goal path.
	 *
	 * @param goalNodeId  user's goal skill
	 * @param focusNodeId first non-completed skill on the path (or the goal if all passed)
	 */
	WorkoutAssignment nextWorkout(UUID goalNodeId, UUID focusNodeId);

	default WorkoutAssignment nextWorkout(PlacementResult placement) {
		return nextWorkout(placement.goalNodeId(), placement.focusNodeId());
	}

	/**
	 * After finishing a focus node, find the next path node that has an active workout.
	 * Nodes between the completed focus and that next focus are returned as {@code skippedNodeIds}
	 * (auto-advanced when no workout template exists).
	 */
	Optional<NextPathStep> nextWorkoutAfter(UUID goalNodeId, UUID completedFocusNodeId);

	record NextPathStep(
			WorkoutAssignment assignment,
			List<UUID> skippedNodeIds
	) {
	}
}

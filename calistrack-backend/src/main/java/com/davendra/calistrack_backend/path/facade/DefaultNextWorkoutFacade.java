package com.davendra.calistrack_backend.path.facade;

import com.davendra.calistrack_backend.catalog.entity.Workout;
import com.davendra.calistrack_backend.catalog.repo.WorkoutRepository;
import com.davendra.calistrack_backend.common.exception.ApiException;
import com.davendra.calistrack_backend.path.catalog.GoalPathCatalog;
import com.davendra.calistrack_backend.path.dto.WorkoutAssignment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DefaultNextWorkoutFacade implements NextWorkoutFacade {

	private final GoalPathCatalog goalPathCatalog;
	private final WorkoutRepository workoutRepository;

	public DefaultNextWorkoutFacade(GoalPathCatalog goalPathCatalog, WorkoutRepository workoutRepository) {
		this.goalPathCatalog = goalPathCatalog;
		this.workoutRepository = workoutRepository;
	}

	@Override
	public WorkoutAssignment nextWorkout(UUID goalNodeId, UUID focusNodeId) {
		List<UUID> path = goalPathCatalog.pathNodeIds(goalNodeId);
		if (!path.contains(focusNodeId)) {
			throw new ApiException(
					HttpStatus.BAD_REQUEST,
					"Focus node " + focusNodeId + " is not on path for goal " + goalNodeId
			);
		}

		Workout workout = workoutRepository
				.findFirstByGoalNode_IdAndStatusOrderByCreatedAtAsc(focusNodeId, Workout.STATUS_ACTIVE)
				.orElseThrow(() -> new ApiException(
						HttpStatus.NOT_FOUND,
						"No active workout found for focus node: " + focusNodeId
				));

		return new WorkoutAssignment(
				goalNodeId,
				focusNodeId,
				workout.getId(),
				workout.getTitle()
		);
	}

	@Override
	public Optional<NextPathStep> nextWorkoutAfter(UUID goalNodeId, UUID completedFocusNodeId) {
		List<UUID> path = goalPathCatalog.pathNodeIds(goalNodeId);
		int idx = path.indexOf(completedFocusNodeId);
		if (idx < 0) {
			throw new ApiException(
					HttpStatus.BAD_REQUEST,
					"Focus node " + completedFocusNodeId + " is not on path for goal " + goalNodeId
			);
		}

		List<UUID> skipped = new ArrayList<>();
		for (int i = idx + 1; i < path.size(); i++) {
			UUID candidate = path.get(i);
			Optional<Workout> workout = workoutRepository
					.findFirstByGoalNode_IdAndStatusOrderByCreatedAtAsc(candidate, Workout.STATUS_ACTIVE);
			if (workout.isPresent()) {
				WorkoutAssignment assignment = new WorkoutAssignment(
						goalNodeId,
						candidate,
						workout.get().getId(),
						workout.get().getTitle()
				);
				return Optional.of(new NextPathStep(assignment, List.copyOf(skipped)));
			}
			skipped.add(candidate);
		}
		return Optional.empty();
	}
}

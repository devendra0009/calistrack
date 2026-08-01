package com.davendra.calistrack_backend.workout.controller;

import com.davendra.calistrack_backend.workout.dto.CurrentWorkoutSessionResponse;
import com.davendra.calistrack_backend.workout.dto.ExerciseAttemptResponse;
import com.davendra.calistrack_backend.workout.dto.WorkoutSessionDetailResponse;
import com.davendra.calistrack_backend.workout.service.WorkoutSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workout-sessions")
@Tag(name = "Workout sessions", description = "Current and historical training sessions")
public class WorkoutSessionController {

	private final WorkoutSessionService workoutSessionService;

	public WorkoutSessionController(WorkoutSessionService workoutSessionService) {
		this.workoutSessionService = workoutSessionService;
	}

	@GetMapping
	@Operation(
			summary = "List workout sessions",
			description = "Returns all sessions for the current user, newest first (includes verified flag)"
	)
	public List<CurrentWorkoutSessionResponse> listSessions() {
		return workoutSessionService.listForCurrentUser();
	}

	@GetMapping("/current")
	@Operation(
			summary = "Get current workout session",
			description = "Returns the open PENDING/IN_PROGRESS session, or the most recent session if none are open"
	)
	public CurrentWorkoutSessionResponse getCurrent() {
		return workoutSessionService.requireCurrent();
	}

	@GetMapping("/{sessionId}")
	@Operation(
			summary = "Get workout session detail",
			description = "Returns session metadata plus workout exercise lines and any existing attempts"
	)
	public WorkoutSessionDetailResponse getDetail(@PathVariable UUID sessionId) {
		return workoutSessionService.getDetail(sessionId);
	}

	@PostMapping("/{sessionId}/begin")
	@Operation(
			summary = "Begin training",
			description = "PENDING → IN_PROGRESS and sets startedAt (starts the session timer)"
	)
	public CurrentWorkoutSessionResponse beginSession(@PathVariable UUID sessionId) {
		return workoutSessionService.beginSession(sessionId);
	}

	@PostMapping("/{sessionId}/exercises/{workoutExerciseId}/complete")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
			summary = "Mark exercise completed",
			description = "Creates a COMPLETED exercise_attempt for this line (no sets/reps entry required)"
	)
	public ExerciseAttemptResponse markExerciseCompleted(
			@PathVariable UUID sessionId,
			@PathVariable UUID workoutExerciseId
	) {
		return workoutSessionService.markExerciseCompleted(sessionId, workoutExerciseId);
	}

	@PostMapping("/{sessionId}/exercises/{workoutExerciseId}/start")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
			summary = "Mark exercise completed (legacy alias)",
			description = "Same as /complete — kept for older clients"
	)
	public ExerciseAttemptResponse startExercise(
			@PathVariable UUID sessionId,
			@PathVariable UUID workoutExerciseId
	) {
		return workoutSessionService.markExerciseCompleted(sessionId, workoutExerciseId);
	}

	@PostMapping("/{sessionId}/complete")
	@Operation(
			summary = "Finish workout session",
			description = "Requires exercise_attempt count (COMPLETED) == workout_exercise count; sets verified=false"
	)
	public CurrentWorkoutSessionResponse completeSession(@PathVariable UUID sessionId) {
		return workoutSessionService.completeSession(sessionId);
	}
}

package com.davendra.calistrack_backend.workout.controller;

import com.davendra.calistrack_backend.workout.dto.ExerciseAttemptResponse;
import com.davendra.calistrack_backend.workout.dto.PatchExerciseAttemptRequest;
import com.davendra.calistrack_backend.workout.service.WorkoutSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/exercise-attempts")
@Tag(name = "Exercise attempts", description = "Log sets/reps for a started workout line")
public class ExerciseAttemptController {

	private final WorkoutSessionService workoutSessionService;

	public ExerciseAttemptController(WorkoutSessionService workoutSessionService) {
		this.workoutSessionService = workoutSessionService;
	}

	@PatchMapping("/{attemptId}")
	@Operation(
			summary = "Update exercise attempt",
			description = "Log actual sets/reps/hold/rest and optionally mark COMPLETED or SKIPPED"
	)
	public ExerciseAttemptResponse patchAttempt(
			@PathVariable UUID attemptId,
			@Valid @RequestBody PatchExerciseAttemptRequest request
	) {
		return workoutSessionService.patchAttempt(attemptId, request);
	}
}

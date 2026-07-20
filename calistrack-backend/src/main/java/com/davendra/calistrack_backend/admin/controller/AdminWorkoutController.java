package com.davendra.calistrack_backend.admin.controller;

import com.davendra.calistrack_backend.admin.dto.AdminWorkoutExerciseRequest;
import com.davendra.calistrack_backend.admin.dto.AdminWorkoutExerciseResponse;
import com.davendra.calistrack_backend.admin.dto.AdminWorkoutRequest;
import com.davendra.calistrack_backend.admin.dto.AdminWorkoutResponse;
import com.davendra.calistrack_backend.admin.dto.AdminWorkoutSummaryResponse;
import com.davendra.calistrack_backend.admin.service.AdminWorkoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/workouts")
@Tag(name = "Admin — Workouts", description = "Catalog CRUD for workouts and their exercise lines (ADMIN role)")
public class AdminWorkoutController {

	private final AdminWorkoutService adminWorkoutService;

	public AdminWorkoutController(AdminWorkoutService adminWorkoutService) {
		this.adminWorkoutService = adminWorkoutService;
	}

	@GetMapping
	@Operation(
			summary = "List workouts",
			description = "Summaries with goalNode {id, name} and exerciseCount. "
					+ "Optional ?status= and ?goalNodeId= filters"
	)
	public List<AdminWorkoutSummaryResponse> list(
			@RequestParam(required = false) String status,
			@RequestParam(required = false) UUID goalNodeId
	) {
		return adminWorkoutService.list(status, goalNodeId);
	}

	@GetMapping("/{id}")
	@Operation(
			summary = "Get workout detail",
			description = "Includes goalNode name and each workout_exercise with nested exercise {id, name}"
	)
	public AdminWorkoutResponse get(@PathVariable UUID id) {
		return adminWorkoutService.get(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
			summary = "Create workout",
			description = "Optionally include exercises[] to create workout_exercise lines in the same request"
	)
	public AdminWorkoutResponse create(@Valid @RequestBody AdminWorkoutRequest request) {
		return adminWorkoutService.create(request);
	}

	@PutMapping("/{id}")
	@Operation(
			summary = "Update workout",
			description = "If exercises is non-null, replaces all workout_exercise lines"
	)
	public AdminWorkoutResponse update(@PathVariable UUID id, @Valid @RequestBody AdminWorkoutRequest request) {
		return adminWorkoutService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Deprecate workout", description = "Sets status=DEPRECATED (soft delete)")
	public AdminWorkoutResponse deprecate(@PathVariable UUID id) {
		return adminWorkoutService.deprecate(id);
	}

	@PostMapping("/{workoutId}/exercises")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Add exercise line to workout")
	public AdminWorkoutExerciseResponse addExercise(
			@PathVariable UUID workoutId,
			@Valid @RequestBody AdminWorkoutExerciseRequest request
	) {
		return adminWorkoutService.addExercise(workoutId, request);
	}

	@PutMapping("/{workoutId}/exercises/{workoutExerciseId}")
	@Operation(summary = "Update workout exercise line")
	public AdminWorkoutExerciseResponse updateExercise(
			@PathVariable UUID workoutId,
			@PathVariable UUID workoutExerciseId,
			@Valid @RequestBody AdminWorkoutExerciseRequest request
	) {
		return adminWorkoutService.updateExercise(workoutId, workoutExerciseId, request);
	}

	@DeleteMapping("/{workoutId}/exercises/{workoutExerciseId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Remove workout exercise line")
	public void deleteExercise(
			@PathVariable UUID workoutId,
			@PathVariable UUID workoutExerciseId
	) {
		adminWorkoutService.deleteExercise(workoutId, workoutExerciseId);
	}
}

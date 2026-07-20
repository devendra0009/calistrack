package com.davendra.calistrack_backend.admin.controller;

import com.davendra.calistrack_backend.admin.dto.AdminExerciseRequest;
import com.davendra.calistrack_backend.admin.dto.AdminExerciseResponse;
import com.davendra.calistrack_backend.admin.service.AdminExerciseService;
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
@RequestMapping("/api/v1/admin/exercises")
@Tag(name = "Admin — Exercises", description = "Catalog CRUD for exercises (ADMIN role)")
public class AdminExerciseController {

	private final AdminExerciseService adminExerciseService;

	public AdminExerciseController(AdminExerciseService adminExerciseService) {
		this.adminExerciseService = adminExerciseService;
	}

	@GetMapping
	@Operation(summary = "List exercises", description = "Optional ?status=ACTIVE|COMING_SOON|DEPRECATED filter")
	public List<AdminExerciseResponse> list(@RequestParam(required = false) String status) {
		return adminExerciseService.list(status);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get exercise by id")
	public AdminExerciseResponse get(@PathVariable UUID id) {
		return adminExerciseService.get(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create exercise")
	public AdminExerciseResponse create(@Valid @RequestBody AdminExerciseRequest request) {
		return adminExerciseService.create(request);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update exercise")
	public AdminExerciseResponse update(@PathVariable UUID id, @Valid @RequestBody AdminExerciseRequest request) {
		return adminExerciseService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Deprecate exercise", description = "Sets status=DEPRECATED (soft delete)")
	public AdminExerciseResponse deprecate(@PathVariable UUID id) {
		return adminExerciseService.deprecate(id);
	}
}

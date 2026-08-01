package com.davendra.calistrack_backend.admin.controller;

import com.davendra.calistrack_backend.admin.dto.AdminWorkoutPlanDayRequest;
import com.davendra.calistrack_backend.admin.dto.AdminWorkoutPlanDayResponse;
import com.davendra.calistrack_backend.admin.dto.AdminWorkoutPlanRequest;
import com.davendra.calistrack_backend.admin.dto.AdminWorkoutPlanResponse;
import com.davendra.calistrack_backend.admin.dto.AdminWorkoutPlanSummaryResponse;
import com.davendra.calistrack_backend.admin.service.AdminWorkoutPlanService;
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
@RequestMapping("/api/v1/admin/workout-plans")
@Tag(name = "Admin — Workout Plans", description = "Curated multi-day plans per skill node (ADMIN role)")
public class AdminWorkoutPlanController {

	private final AdminWorkoutPlanService adminWorkoutPlanService;

	public AdminWorkoutPlanController(AdminWorkoutPlanService adminWorkoutPlanService) {
		this.adminWorkoutPlanService = adminWorkoutPlanService;
	}

	@GetMapping
	@Operation(summary = "List workout plans", description = "Optional ?status= and ?nodeId= filters")
	public List<AdminWorkoutPlanSummaryResponse> list(
			@RequestParam(required = false) String status,
			@RequestParam(required = false) UUID nodeId
	) {
		return adminWorkoutPlanService.list(status, nodeId);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get workout plan detail with ordered days")
	public AdminWorkoutPlanResponse get(@PathVariable UUID id) {
		return adminWorkoutPlanService.get(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create workout plan", description = "Include days[] covering 1..durationDays")
	public AdminWorkoutPlanResponse create(@Valid @RequestBody AdminWorkoutPlanRequest request) {
		return adminWorkoutPlanService.create(request);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update workout plan", description = "If days is non-null, replaces all plan days")
	public AdminWorkoutPlanResponse update(
			@PathVariable UUID id,
			@Valid @RequestBody AdminWorkoutPlanRequest request
	) {
		return adminWorkoutPlanService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Deprecate workout plan", description = "Sets status=DEPRECATED")
	public AdminWorkoutPlanResponse deprecate(@PathVariable UUID id) {
		return adminWorkoutPlanService.deprecate(id);
	}

	@PostMapping("/{planId}/days")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Add a day to a plan")
	public AdminWorkoutPlanDayResponse addDay(
			@PathVariable UUID planId,
			@Valid @RequestBody AdminWorkoutPlanDayRequest request
	) {
		return adminWorkoutPlanService.addDay(planId, request);
	}

	@PutMapping("/{planId}/days/{dayId}")
	@Operation(summary = "Update a plan day")
	public AdminWorkoutPlanDayResponse updateDay(
			@PathVariable UUID planId,
			@PathVariable UUID dayId,
			@Valid @RequestBody AdminWorkoutPlanDayRequest request
	) {
		return adminWorkoutPlanService.updateDay(planId, dayId, request);
	}

	@DeleteMapping("/{planId}/days/{dayId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Remove a plan day")
	public void deleteDay(@PathVariable UUID planId, @PathVariable UUID dayId) {
		adminWorkoutPlanService.deleteDay(planId, dayId);
	}
}

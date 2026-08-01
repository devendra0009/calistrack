package com.davendra.calistrack_backend.stretching.controller;

import com.davendra.calistrack_backend.stretching.dto.StretchingTodayResponse;
import com.davendra.calistrack_backend.stretching.service.StretchingService;
import com.davendra.calistrack_backend.workout.dto.CurrentWorkoutSessionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stretching")
@Tag(name = "Stretching", description = "Morning stretch daily routine (independent of skill workouts)")
public class StretchingController {

	private final StretchingService stretchingService;

	public StretchingController(StretchingService stretchingService) {
		this.stretchingService = stretchingService;
	}

	@GetMapping("/today")
	@Operation(
			summary = "Get current stretch day",
			description = "Returns the user's current Day 1–7 stretch routine. "
					+ "Day advances only after the user completes that stretch session."
	)
	public StretchingTodayResponse getToday() {
		return stretchingService.getToday();
	}

	@PostMapping("/sessions")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
			summary = "Start today's stretch session",
			description = "Creates an IN_PROGRESS stretch session for the current day. "
					+ "Idempotent if a stretch session is already open. "
					+ "Does not conflict with an open skill workout session."
	)
	public CurrentWorkoutSessionResponse startSession() {
		return stretchingService.startSession();
	}
}

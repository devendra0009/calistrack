package com.davendra.calistrack_backend.activity.controller;

import com.davendra.calistrack_backend.activity.dto.ActivityCalendarResponse;
import com.davendra.calistrack_backend.activity.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/activity")
@Tag(name = "Activity", description = "Daily activity calendar from completed workouts and stretches")
public class ActivityController {

	private final ActivityService activityService;

	public ActivityController(ActivityService activityService) {
		this.activityService = activityService;
	}

	@GetMapping
	@Operation(
			summary = "Activity calendar",
			description = "Returns days in [from, to] where the user completed at least one "
					+ "skill workout or stretch session. Dates are bucketed in the given IANA timezone. "
					+ "Only COMPLETED sessions count. Max range: 90 days."
	)
	public ActivityCalendarResponse getCalendar(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false, defaultValue = "UTC") String timezone
	) {
		return activityService.getCalendar(from, to, timezone);
	}
}

package com.davendra.calistrack_backend.admin.dto;

import java.util.UUID;

public record AdminWorkoutPlanDayResponse(
		UUID id,
		int dayNumber,
		NamedRef workout
) {
}

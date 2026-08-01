package com.davendra.calistrack_backend.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AdminWorkoutPlanDayRequest(
		@NotNull @Min(1) Integer dayNumber,
		@NotNull UUID workoutId
) {
}

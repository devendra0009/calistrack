package com.davendra.calistrack_backend.admin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record AdminWorkoutPlanRequest(
		@NotBlank @Size(max = 160) String title,
		String description,
		@NotNull UUID nodeId,
		@Size(max = 20) String kind,
		@Size(max = 64) String code,
		@NotNull @Min(1) Integer durationDays,
		@Size(max = 20) String status,
		@Valid List<AdminWorkoutPlanDayRequest> days
) {
}

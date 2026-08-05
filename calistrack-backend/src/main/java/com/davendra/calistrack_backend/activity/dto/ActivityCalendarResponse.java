package com.davendra.calistrack_backend.activity.dto;

import java.time.LocalDate;
import java.util.List;

public record ActivityCalendarResponse(
		LocalDate from,
		LocalDate to,
		String timezone,
		List<ActivityDayDto> days
) {
	public record ActivityDayDto(
			LocalDate date,
			int count,
			int skillCount,
			int stretchCount
	) {
	}
}

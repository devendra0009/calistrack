package com.davendra.calistrack_backend.activity.service;

import com.davendra.calistrack_backend.activity.dto.ActivityCalendarResponse;
import com.davendra.calistrack_backend.activity.dto.ActivityCalendarResponse.ActivityDayDto;
import com.davendra.calistrack_backend.catalog.entity.Workout;
import com.davendra.calistrack_backend.common.exception.ApiException;
import com.davendra.calistrack_backend.user.entity.AppUser;
import com.davendra.calistrack_backend.user.service.CurrentUserService;
import com.davendra.calistrack_backend.workout.entity.WorkoutSession;
import com.davendra.calistrack_backend.workout.enums.WorkoutSessionStatus;
import com.davendra.calistrack_backend.workout.repo.WorkoutSessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ActivityService {

	/** Cap so clients can grow from 7 → 30/60 without unbounded scans. */
	public static final int MAX_RANGE_DAYS = 90;

	private final CurrentUserService currentUserService;
	private final WorkoutSessionRepository workoutSessionRepository;

	public ActivityService(
			CurrentUserService currentUserService,
			WorkoutSessionRepository workoutSessionRepository
	) {
		this.currentUserService = currentUserService;
		this.workoutSessionRepository = workoutSessionRepository;
	}

	@Transactional(readOnly = true)
	public ActivityCalendarResponse getCalendar(LocalDate from, LocalDate to, String timezone) {
		if (from == null || to == null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "from and to are required (YYYY-MM-DD)");
		}
		if (to.isBefore(from)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "to must be on or after from");
		}
		long spanDays = ChronoUnit.DAYS.between(from, to) + 1;
		if (spanDays > MAX_RANGE_DAYS) {
			throw new ApiException(
					HttpStatus.BAD_REQUEST,
					"Date range cannot exceed " + MAX_RANGE_DAYS + " days"
			);
		}

		ZoneId zone = resolveZone(timezone);
		AppUser user = currentUserService.requireActiveUser();

		Instant fromInstant = from.atStartOfDay(zone).toInstant();
		Instant toExclusive = to.plusDays(1).atStartOfDay(zone).toInstant();

		List<WorkoutSession> sessions = workoutSessionRepository.findCompletedInRange(
				user.getId(),
				WorkoutSessionStatus.COMPLETED,
				fromInstant,
				toExclusive
		);

		Map<LocalDate, int[]> byDate = new LinkedHashMap<>();
		for (WorkoutSession session : sessions) {
			if (session.getCompletedAt() == null) {
				continue;
			}
			LocalDate day = session.getCompletedAt().atZone(zone).toLocalDate();
			int[] counts = byDate.computeIfAbsent(day, d -> new int[2]);
			String kind = session.getWorkout() != null ? session.getWorkout().getKind() : null;
			if (Workout.KIND_STRETCH.equals(kind)) {
				counts[1] += 1;
			} else {
				counts[0] += 1;
			}
		}

		List<ActivityDayDto> days = new ArrayList<>();
		for (Map.Entry<LocalDate, int[]> entry : byDate.entrySet()) {
			int skill = entry.getValue()[0];
			int stretch = entry.getValue()[1];
			days.add(new ActivityDayDto(entry.getKey(), skill + stretch, skill, stretch));
		}
		days.sort(Comparator.comparing(ActivityDayDto::date));

		return new ActivityCalendarResponse(from, to, zone.getId(), days);
	}

	private static ZoneId resolveZone(String timezone) {
		if (timezone == null || timezone.isBlank()) {
			return ZoneId.of("UTC");
		}
		try {
			return ZoneId.of(timezone.trim());
		} catch (DateTimeException ex) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid timezone: " + timezone);
		}
	}
}

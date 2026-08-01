package com.davendra.calistrack_backend.stretching.service;

import com.davendra.calistrack_backend.catalog.entity.Exercise;
import com.davendra.calistrack_backend.catalog.entity.Workout;
import com.davendra.calistrack_backend.catalog.entity.WorkoutExercise;
import com.davendra.calistrack_backend.catalog.entity.WorkoutPlan;
import com.davendra.calistrack_backend.catalog.entity.WorkoutPlanDay;
import com.davendra.calistrack_backend.catalog.repo.WorkoutExerciseRepository;
import com.davendra.calistrack_backend.catalog.repo.WorkoutPlanDayRepository;
import com.davendra.calistrack_backend.catalog.repo.WorkoutPlanRepository;
import com.davendra.calistrack_backend.common.exception.ApiException;
import com.davendra.calistrack_backend.stretching.dto.StretchingTodayResponse;
import com.davendra.calistrack_backend.stretching.dto.StretchingTodayResponse.StretchExerciseLineDto;
import com.davendra.calistrack_backend.user.entity.AppUser;
import com.davendra.calistrack_backend.user.service.CurrentUserService;
import com.davendra.calistrack_backend.workout.dto.CurrentWorkoutSessionResponse;
import com.davendra.calistrack_backend.workout.entity.WorkoutSession;
import com.davendra.calistrack_backend.workout.enums.WorkoutSessionStatus;
import com.davendra.calistrack_backend.workout.repo.WorkoutSessionRepository;
import com.davendra.calistrack_backend.workout.service.WorkoutSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * Morning stretch daily routine: personal Day 1–7 cycle that advances only on complete.
 */
@Service
public class StretchingService {

	private final CurrentUserService currentUserService;
	private final WorkoutPlanRepository workoutPlanRepository;
	private final WorkoutPlanDayRepository workoutPlanDayRepository;
	private final WorkoutExerciseRepository workoutExerciseRepository;
	private final WorkoutSessionRepository workoutSessionRepository;
	private final WorkoutSessionService workoutSessionService;

	public StretchingService(
			CurrentUserService currentUserService,
			WorkoutPlanRepository workoutPlanRepository,
			WorkoutPlanDayRepository workoutPlanDayRepository,
			WorkoutExerciseRepository workoutExerciseRepository,
			WorkoutSessionRepository workoutSessionRepository,
			WorkoutSessionService workoutSessionService
	) {
		this.currentUserService = currentUserService;
		this.workoutPlanRepository = workoutPlanRepository;
		this.workoutPlanDayRepository = workoutPlanDayRepository;
		this.workoutExerciseRepository = workoutExerciseRepository;
		this.workoutSessionRepository = workoutSessionRepository;
		this.workoutSessionService = workoutSessionService;
	}

	@Transactional(readOnly = true)
	public StretchingTodayResponse getToday() {
		AppUser user = currentUserService.requireActiveUser();
		WorkoutPlan plan = requireMorningStretchPlan();
		ResolvedDay resolved = resolveCurrentDay(user, plan);
		return toTodayResponse(plan, resolved);
	}

	@Transactional
	public CurrentWorkoutSessionResponse startSession() {
		AppUser user = currentUserService.requireActiveUser();
		WorkoutPlan plan = requireMorningStretchPlan();

		Optional<WorkoutSession> open = workoutSessionRepository
				.findFirstByUserIdAndWorkout_KindAndStatusInOrderByCreatedAtDesc(
						user.getId(),
						Workout.KIND_STRETCH,
						EnumSet.of(WorkoutSessionStatus.PENDING, WorkoutSessionStatus.IN_PROGRESS)
				);
		if (open.isPresent()) {
			return workoutSessionService.toCurrentResponsePublic(open.get());
		}

		ResolvedDay resolved = resolveCurrentDay(user, plan);
		WorkoutSession session = workoutSessionService.createStretchSession(
				user,
				resolved.day().getWorkout(),
				resolved.dayNumber()
		);
		return workoutSessionService.toCurrentResponsePublic(session);
	}

	private ResolvedDay resolveCurrentDay(AppUser user, WorkoutPlan plan) {
		Optional<WorkoutSession> open = workoutSessionRepository
				.findFirstByUserIdAndWorkout_KindAndStatusInOrderByCreatedAtDesc(
						user.getId(),
						Workout.KIND_STRETCH,
						EnumSet.of(WorkoutSessionStatus.PENDING, WorkoutSessionStatus.IN_PROGRESS)
				);
		if (open.isPresent()) {
			WorkoutSession session = open.get();
			int dayNumber = session.getPlanDayNumber() != null ? session.getPlanDayNumber() : 1;
			WorkoutPlanDay day = requirePlanDay(plan.getId(), dayNumber);
			return new ResolvedDay(dayNumber, day, session);
		}

		Optional<WorkoutSession> lastCompleted = workoutSessionRepository
				.findFirstByUserIdAndWorkout_KindAndStatusOrderByCompletedAtDesc(
						user.getId(),
						Workout.KIND_STRETCH,
						WorkoutSessionStatus.COMPLETED
				);

		int dayNumber = 1;
		if (lastCompleted.isPresent()) {
			Integer finished = lastCompleted.get().getPlanDayNumber();
			int finishedDay = finished != null ? finished : 1;
			dayNumber = finishedDay >= plan.getDurationDays() ? 1 : finishedDay + 1;
		}

		WorkoutPlanDay day = requirePlanDay(plan.getId(), dayNumber);
		return new ResolvedDay(dayNumber, day, null);
	}

	private WorkoutPlan requireMorningStretchPlan() {
		return workoutPlanRepository
				.findFirstByCodeAndStatus(WorkoutPlan.CODE_MORNING_STRETCH, WorkoutPlan.STATUS_ACTIVE)
				.orElseThrow(() -> new ApiException(
						HttpStatus.NOT_FOUND,
						"Morning stretch plan is not configured"
				));
	}

	private WorkoutPlanDay requirePlanDay(java.util.UUID planId, int dayNumber) {
		return workoutPlanDayRepository.findByPlan_IdAndDayNumber(planId, dayNumber)
				.orElseThrow(() -> new ApiException(
						HttpStatus.NOT_FOUND,
						"Stretch plan day " + dayNumber + " not found"
				));
	}

	private StretchingTodayResponse toTodayResponse(WorkoutPlan plan, ResolvedDay resolved) {
		Workout workout = resolved.day().getWorkout();
		List<StretchExerciseLineDto> exercises = workoutExerciseRepository
				.findByWorkout_IdOrderBySequenceAsc(workout.getId())
				.stream()
				.map(this::toExerciseLine)
				.toList();

		WorkoutSession session = resolved.openSession();
		return new StretchingTodayResponse(
				plan.getCode(),
				plan.getTitle(),
				resolved.dayNumber(),
				plan.getDurationDays(),
				workout.getId(),
				workout.getTitle(),
				workout.getDescription(),
				session != null ? session.getId() : null,
				session != null ? session.getStatus() : null,
				exercises
		);
	}

	private StretchExerciseLineDto toExerciseLine(WorkoutExercise line) {
		Exercise exercise = line.getExercise();
		String demoVideoUrl = line.getDemoVideoUrl() != null && !line.getDemoVideoUrl().isBlank()
				? line.getDemoVideoUrl()
				: exercise.getDemoVideoUrl();
		return new StretchExerciseLineDto(
				line.getId(),
				line.getSequence(),
				exercise.getId(),
				exercise.getName(),
				exercise.getDescription(),
				exercise.getMetricType(),
				exercise.getThumbnailUrl(),
				demoVideoUrl,
				line.getTargetSets(),
				line.getTargetReps(),
				line.getTargetHoldSeconds(),
				line.getTargetRestSeconds(),
				line.getNotes()
		);
	}

	private record ResolvedDay(
			int dayNumber,
			WorkoutPlanDay day,
			WorkoutSession openSession
	) {
	}
}

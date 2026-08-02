package com.davendra.calistrack_backend.stretching.service;

import com.davendra.calistrack_backend.catalog.entity.Exercise;
import com.davendra.calistrack_backend.catalog.entity.WorkoutExercise;
import com.davendra.calistrack_backend.catalog.entity.WorkoutPlan;
import com.davendra.calistrack_backend.catalog.entity.WorkoutPlanDay;
import com.davendra.calistrack_backend.catalog.repo.WorkoutExerciseRepository;
import com.davendra.calistrack_backend.catalog.repo.WorkoutPlanDayRepository;
import com.davendra.calistrack_backend.catalog.repo.WorkoutPlanRepository;
import com.davendra.calistrack_backend.common.exception.ApiException;
import com.davendra.calistrack_backend.stretching.dto.StretchingTodayResponse.StretchExerciseLineDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Cached static stretch catalog (plan / plan-day / exercise lines). User session state stays uncached.
 */
@Service
public class StretchCatalogService {

	public static final String PLAN_CACHE = "morningStretchPlan";
	public static final String PLAN_DAY_CACHE = "stretchPlanDay";
	public static final String EXERCISES_CACHE = "stretchWorkoutExercises";

	private final WorkoutPlanRepository workoutPlanRepository;
	private final WorkoutPlanDayRepository workoutPlanDayRepository;
	private final WorkoutExerciseRepository workoutExerciseRepository;

	public StretchCatalogService(
			WorkoutPlanRepository workoutPlanRepository,
			WorkoutPlanDayRepository workoutPlanDayRepository,
			WorkoutExerciseRepository workoutExerciseRepository
	) {
		this.workoutPlanRepository = workoutPlanRepository;
		this.workoutPlanDayRepository = workoutPlanDayRepository;
		this.workoutExerciseRepository = workoutExerciseRepository;
	}

	@Transactional(readOnly = true)
	@Cacheable(cacheNames = PLAN_CACHE, key = "'" + WorkoutPlan.CODE_MORNING_STRETCH + "'")
	public WorkoutPlan requireMorningStretchPlan() {
		return workoutPlanRepository
				.findLeanByCodeAndStatus(WorkoutPlan.CODE_MORNING_STRETCH, WorkoutPlan.STATUS_ACTIVE)
				.orElseThrow(() -> new ApiException(
						HttpStatus.NOT_FOUND,
						"Morning stretch plan is not configured"
				));
	}

	@Transactional(readOnly = true)
	@Cacheable(cacheNames = PLAN_DAY_CACHE, key = "#planId + ':' + #dayNumber")
	public WorkoutPlanDay requirePlanDay(UUID planId, int dayNumber) {
		return workoutPlanDayRepository.findLeanByPlanIdAndDayNumber(planId, dayNumber)
				.orElseThrow(() -> new ApiException(
						HttpStatus.NOT_FOUND,
						"Stretch plan day " + dayNumber + " not found"
				));
	}

	@Transactional(readOnly = true)
	@Cacheable(cacheNames = EXERCISES_CACHE, key = "#workoutId")
	public List<StretchExerciseLineDto> exerciseLines(UUID workoutId) {
		return workoutExerciseRepository.findByWorkout_IdOrderBySequenceAsc(workoutId).stream()
				.map(StretchCatalogService::toExerciseLine)
				.toList();
	}

	private static StretchExerciseLineDto toExerciseLine(WorkoutExercise line) {
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
}

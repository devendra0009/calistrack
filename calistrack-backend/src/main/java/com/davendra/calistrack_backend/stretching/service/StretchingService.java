package com.davendra.calistrack_backend.stretching.service;

import com.davendra.calistrack_backend.catalog.entity.Workout;
import com.davendra.calistrack_backend.catalog.entity.WorkoutPlan;
import com.davendra.calistrack_backend.catalog.entity.WorkoutPlanDay;
import com.davendra.calistrack_backend.stretching.dto.StretchingTodayResponse;
import com.davendra.calistrack_backend.stretching.dto.StretchingTodayResponse.StretchExerciseLineDto;
import com.davendra.calistrack_backend.user.entity.AppUser;
import com.davendra.calistrack_backend.user.service.CurrentUserService;
import com.davendra.calistrack_backend.workout.dto.CurrentWorkoutSessionResponse;
import com.davendra.calistrack_backend.workout.entity.WorkoutSession;
import com.davendra.calistrack_backend.workout.enums.WorkoutSessionStatus;
import com.davendra.calistrack_backend.workout.repo.WorkoutSessionRepository;
import com.davendra.calistrack_backend.workout.service.WorkoutSessionService;
import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Morning stretch daily routine: personal Day 1–7 cycle that advances only on complete.
 */
@Service
public class StretchingService {

	private static final Logger log = LoggerFactory.getLogger(StretchingService.class);

	private final CurrentUserService currentUserService;
	private final StretchCatalogService stretchCatalogService;
	private final WorkoutSessionRepository workoutSessionRepository;
	private final WorkoutSessionService workoutSessionService;
	private final CacheManager cacheManager;

	public StretchingService(
			CurrentUserService currentUserService,
			StretchCatalogService stretchCatalogService,
			WorkoutSessionRepository workoutSessionRepository,
			WorkoutSessionService workoutSessionService,
			CacheManager cacheManager
	) {
		this.currentUserService = currentUserService;
		this.stretchCatalogService = stretchCatalogService;
		this.workoutSessionRepository = workoutSessionRepository;
		this.workoutSessionService = workoutSessionService;
		this.cacheManager = cacheManager;
	}

	@Transactional(readOnly = true)
	public StretchingTodayResponse getToday() {
		long startedNs = System.nanoTime();
		AppUser user = currentUserService.requireActiveUser();

		boolean planHit = cacheHas(StretchCatalogService.PLAN_CACHE, WorkoutPlan.CODE_MORNING_STRETCH);
		log.info(
				"stretching.today.cache.before userId={} planHit={} planKeys={} planDayKeys={} exerciseKeys={}",
				user.getId(),
				planHit,
				cacheKeys(StretchCatalogService.PLAN_CACHE),
				cacheKeys(StretchCatalogService.PLAN_DAY_CACHE),
				cacheKeys(StretchCatalogService.EXERCISES_CACHE)
		);

		WorkoutPlan plan = stretchCatalogService.requireMorningStretchPlan();
		DayResolution dayResolution = resolveDayNumber(user, plan);

		String planDayKey = plan.getId() + ":" + dayResolution.dayNumber();
		boolean planDayHit = cacheHas(StretchCatalogService.PLAN_DAY_CACHE, planDayKey);

		WorkoutPlanDay day = stretchCatalogService.requirePlanDay(plan.getId(), dayResolution.dayNumber());
		UUID workoutId = day.getWorkout().getId();
		boolean exercisesHit = cacheHas(StretchCatalogService.EXERCISES_CACHE, workoutId);

		List<StretchExerciseLineDto> exercises = stretchCatalogService.exerciseLines(workoutId);
		WorkoutSession session = dayResolution.openSession();
		StretchingTodayResponse response = new StretchingTodayResponse(
				plan.getCode(),
				plan.getTitle(),
				dayResolution.dayNumber(),
				plan.getDurationDays(),
				workoutId,
				day.getWorkout().getTitle(),
				day.getWorkout().getDescription(),
				session != null ? session.getId() : null,
				session != null ? session.getStatus() : null,
				exercises
		);

		long elapsedMs = (System.nanoTime() - startedNs) / 1_000_000L;
		log.info(
				"stretching.today userId={} day={} elapsedMs={} cacheHits={{plan={}, planDay={}, exercises={}}} planKeys={} planDayKeys={} exerciseKeys={}",
				user.getId(),
				dayResolution.dayNumber(),
				elapsedMs,
				planHit,
				planDayHit,
				exercisesHit,
				cacheKeys(StretchCatalogService.PLAN_CACHE),
				cacheKeys(StretchCatalogService.PLAN_DAY_CACHE),
				cacheKeys(StretchCatalogService.EXERCISES_CACHE)
		);
		return response;
	}

	@Transactional
	public CurrentWorkoutSessionResponse startSession() {
		AppUser user = currentUserService.requireActiveUser();
		WorkoutPlan plan = stretchCatalogService.requireMorningStretchPlan();

		Optional<WorkoutSession> open = workoutSessionRepository.findLatestOpenStretch(
				user.getId(),
				Workout.KIND_STRETCH,
				EnumSet.of(WorkoutSessionStatus.PENDING, WorkoutSessionStatus.IN_PROGRESS)
		);
		if (open.isPresent()) {
			return workoutSessionService.toCurrentResponsePublic(open.get());
		}

		DayResolution dayResolution = resolveDayNumber(user, plan);
		WorkoutPlanDay day = stretchCatalogService.requirePlanDay(plan.getId(), dayResolution.dayNumber());
		WorkoutSession session = workoutSessionService.createStretchSession(
				user,
				day.getWorkout(),
				dayResolution.dayNumber()
		);
		return workoutSessionService.toCurrentResponsePublic(session);
	}

	/**
	 * Resolves which day the user is on from session history only (no catalog cache reads).
	 */
	private DayResolution resolveDayNumber(AppUser user, WorkoutPlan plan) {
		Optional<WorkoutSession> open = workoutSessionRepository.findLatestOpenStretch(
				user.getId(),
				Workout.KIND_STRETCH,
				EnumSet.of(WorkoutSessionStatus.PENDING, WorkoutSessionStatus.IN_PROGRESS)
		);
		if (open.isPresent()) {
			WorkoutSession session = open.get();
			int dayNumber = session.getPlanDayNumber() != null ? session.getPlanDayNumber() : 1;
			return new DayResolution(dayNumber, session);
		}

		Optional<WorkoutSession> lastCompleted = workoutSessionRepository.findLatestCompletedStretch(
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
		return new DayResolution(dayNumber, null);
	}

	private boolean cacheHas(String cacheName, Object key) {
		org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
		if (cache == null) {
			return false;
		}
		return cache.get(key) != null;
	}

	private Set<String> cacheKeys(String cacheName) {
		org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
		if (cache == null) {
			return Set.of();
		}
		Object nativeCache = cache.getNativeCache();
		if (!(nativeCache instanceof Cache<?, ?> caffeine)) {
			return Set.of("(non-caffeine)");
		}
		Set<String> keys = new TreeSet<>();
		for (Object key : caffeine.asMap().keySet()) {
			keys.add(String.valueOf(key));
		}
		return keys;
	}

	private record DayResolution(int dayNumber, WorkoutSession openSession) {
	}
}

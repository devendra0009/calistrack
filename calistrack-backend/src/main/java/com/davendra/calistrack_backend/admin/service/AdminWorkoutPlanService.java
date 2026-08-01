package com.davendra.calistrack_backend.admin.service;

import com.davendra.calistrack_backend.admin.dto.AdminWorkoutPlanDayRequest;
import com.davendra.calistrack_backend.admin.dto.AdminWorkoutPlanDayResponse;
import com.davendra.calistrack_backend.admin.dto.AdminWorkoutPlanRequest;
import com.davendra.calistrack_backend.admin.dto.AdminWorkoutPlanResponse;
import com.davendra.calistrack_backend.admin.dto.AdminWorkoutPlanSummaryResponse;
import com.davendra.calistrack_backend.admin.dto.NamedRef;
import com.davendra.calistrack_backend.catalog.entity.Node;
import com.davendra.calistrack_backend.catalog.entity.Workout;
import com.davendra.calistrack_backend.catalog.entity.WorkoutPlan;
import com.davendra.calistrack_backend.catalog.entity.WorkoutPlanDay;
import com.davendra.calistrack_backend.catalog.repo.NodeRepository;
import com.davendra.calistrack_backend.catalog.repo.WorkoutPlanDayRepository;
import com.davendra.calistrack_backend.catalog.repo.WorkoutPlanRepository;
import com.davendra.calistrack_backend.catalog.repo.WorkoutRepository;
import com.davendra.calistrack_backend.common.exception.ApiException;
import com.davendra.calistrack_backend.user.service.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AdminWorkoutPlanService {

	private final CurrentUserService currentUserService;
	private final WorkoutPlanRepository workoutPlanRepository;
	private final WorkoutPlanDayRepository workoutPlanDayRepository;
	private final NodeRepository nodeRepository;
	private final WorkoutRepository workoutRepository;

	public AdminWorkoutPlanService(
			CurrentUserService currentUserService,
			WorkoutPlanRepository workoutPlanRepository,
			WorkoutPlanDayRepository workoutPlanDayRepository,
			NodeRepository nodeRepository,
			WorkoutRepository workoutRepository
	) {
		this.currentUserService = currentUserService;
		this.workoutPlanRepository = workoutPlanRepository;
		this.workoutPlanDayRepository = workoutPlanDayRepository;
		this.nodeRepository = nodeRepository;
		this.workoutRepository = workoutRepository;
	}

	@Transactional(readOnly = true)
	public List<AdminWorkoutPlanSummaryResponse> list(String status, UUID nodeId) {
		currentUserService.requireAdmin();
		List<WorkoutPlan> plans;
		if (nodeId != null) {
			plans = workoutPlanRepository.findByNode_IdOrderByTitleAsc(nodeId);
			if (StringUtils.hasText(status)) {
				String s = status.trim();
				plans = plans.stream().filter(p -> s.equals(p.getStatus())).toList();
			}
		} else if (StringUtils.hasText(status)) {
			plans = workoutPlanRepository.findByStatusOrderByTitleAsc(status.trim());
		} else {
			plans = workoutPlanRepository.findAllByOrderByTitleAsc();
		}
		return plans.stream().map(this::toSummary).toList();
	}

	@Transactional(readOnly = true)
	public AdminWorkoutPlanResponse get(UUID id) {
		currentUserService.requireAdmin();
		WorkoutPlan plan = requirePlan(id);
		List<WorkoutPlanDay> days = workoutPlanDayRepository.findByPlan_IdOrderByDayNumberAsc(id);
		return toDetail(plan, days);
	}

	@Transactional
	public AdminWorkoutPlanResponse create(AdminWorkoutPlanRequest request) {
		currentUserService.requireAdmin();
		Node node = requireNode(request.nodeId());
		String status = StringUtils.hasText(request.status())
				? request.status().trim()
				: WorkoutPlan.STATUS_ACTIVE;

		if (WorkoutPlan.STATUS_ACTIVE.equals(status)
				&& workoutPlanRepository.existsByNode_IdAndStatus(node.getId(), WorkoutPlan.STATUS_ACTIVE)) {
			throw new ApiException(
					HttpStatus.CONFLICT,
					"Node already has an ACTIVE workout plan — deprecate it first"
			);
		}

		WorkoutPlan plan = new WorkoutPlan();
		plan.setTitle(request.title().trim());
		plan.setDescription(request.description());
		plan.setNode(node);
		plan.setKind(resolvePlanKind(request.kind()));
		plan.setCode(normalizeCode(request.code()));
		plan.setDurationDays(request.durationDays());
		plan.setStatus(status);
		WorkoutPlan saved = workoutPlanRepository.save(plan);

		List<WorkoutPlanDay> days = replaceDays(saved, request.days());
		assertDaysMatchDuration(saved, days);
		return toDetail(saved, days);
	}

	@Transactional
	public AdminWorkoutPlanResponse update(UUID id, AdminWorkoutPlanRequest request) {
		currentUserService.requireAdmin();
		WorkoutPlan plan = requirePlan(id);
		Node node = requireNode(request.nodeId());
		String status = StringUtils.hasText(request.status()) ? request.status().trim() : plan.getStatus();

		if (WorkoutPlan.STATUS_ACTIVE.equals(status)) {
			workoutPlanRepository.findFirstByNode_IdAndStatusOrderByCreatedAtAsc(node.getId(), WorkoutPlan.STATUS_ACTIVE)
					.filter(existing -> !existing.getId().equals(plan.getId()))
					.ifPresent(existing -> {
						throw new ApiException(
								HttpStatus.CONFLICT,
								"Node already has an ACTIVE workout plan — deprecate it first"
						);
					});
		}

		plan.setTitle(request.title().trim());
		plan.setDescription(request.description());
		plan.setNode(node);
		if (StringUtils.hasText(request.kind())) {
			plan.setKind(resolvePlanKind(request.kind()));
		}
		if (request.code() != null) {
			plan.setCode(normalizeCode(request.code()));
		}
		plan.setDurationDays(request.durationDays());
		plan.setStatus(status);
		WorkoutPlan saved = workoutPlanRepository.save(plan);

		List<WorkoutPlanDay> days;
		if (request.days() != null) {
			days = replaceDays(saved, request.days());
		} else {
			days = workoutPlanDayRepository.findByPlan_IdOrderByDayNumberAsc(saved.getId());
		}
		assertDaysMatchDuration(saved, days);
		return toDetail(saved, days);
	}

	@Transactional
	public AdminWorkoutPlanResponse deprecate(UUID id) {
		currentUserService.requireAdmin();
		WorkoutPlan plan = requirePlan(id);
		plan.setStatus(WorkoutPlan.STATUS_DEPRECATED);
		WorkoutPlan saved = workoutPlanRepository.save(plan);
		List<WorkoutPlanDay> days = workoutPlanDayRepository.findByPlan_IdOrderByDayNumberAsc(id);
		return toDetail(saved, days);
	}

	@Transactional
	public AdminWorkoutPlanDayResponse addDay(UUID planId, AdminWorkoutPlanDayRequest request) {
		currentUserService.requireAdmin();
		WorkoutPlan plan = requirePlan(planId);
		if (workoutPlanDayRepository.findByPlan_IdAndDayNumber(planId, request.dayNumber()).isPresent()) {
			throw new ApiException(HttpStatus.CONFLICT, "Day " + request.dayNumber() + " already exists");
		}
		WorkoutPlanDay day = buildDay(plan, request);
		day = workoutPlanDayRepository.save(day);
		return toDayResponse(day);
	}

	@Transactional
	public AdminWorkoutPlanDayResponse updateDay(UUID planId, UUID dayId, AdminWorkoutPlanDayRequest request) {
		currentUserService.requireAdmin();
		WorkoutPlanDay day = workoutPlanDayRepository.findByIdAndPlan_Id(dayId, planId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Plan day not found"));
		workoutPlanDayRepository.findByPlan_IdAndDayNumber(planId, request.dayNumber())
				.filter(other -> !other.getId().equals(dayId))
				.ifPresent(other -> {
					throw new ApiException(HttpStatus.CONFLICT, "Day " + request.dayNumber() + " already exists");
				});
		day.setDayNumber(request.dayNumber());
		day.setWorkout(requireWorkoutForNode(request.workoutId(), day.getPlan().getNode().getId()));
		return toDayResponse(workoutPlanDayRepository.save(day));
	}

	@Transactional
	public void deleteDay(UUID planId, UUID dayId) {
		currentUserService.requireAdmin();
		WorkoutPlanDay day = workoutPlanDayRepository.findByIdAndPlan_Id(dayId, planId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Plan day not found"));
		workoutPlanDayRepository.delete(day);
	}

	private List<WorkoutPlanDay> replaceDays(WorkoutPlan plan, List<AdminWorkoutPlanDayRequest> requests) {
		workoutPlanDayRepository.deleteByPlan_Id(plan.getId());
		workoutPlanDayRepository.flush();
		if (requests == null || requests.isEmpty()) {
			return List.of();
		}
		Set<Integer> seen = new HashSet<>();
		List<WorkoutPlanDay> days = requests.stream().map(req -> {
			if (!seen.add(req.dayNumber())) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "Duplicate dayNumber: " + req.dayNumber());
			}
			return buildDay(plan, req);
		}).toList();
		return workoutPlanDayRepository.saveAll(days);
	}

	private WorkoutPlanDay buildDay(WorkoutPlan plan, AdminWorkoutPlanDayRequest request) {
		Workout workout = requireWorkoutForNode(request.workoutId(), plan.getNode().getId());
		WorkoutPlanDay day = new WorkoutPlanDay();
		day.setPlan(plan);
		day.setDayNumber(request.dayNumber());
		day.setWorkout(workout);
		return day;
	}

	private void assertDaysMatchDuration(WorkoutPlan plan, List<WorkoutPlanDay> days) {
		if (days.size() != plan.getDurationDays()) {
			throw new ApiException(
					HttpStatus.BAD_REQUEST,
					"Plan must have exactly " + plan.getDurationDays() + " days (found " + days.size() + ")"
			);
		}
		for (int i = 1; i <= plan.getDurationDays(); i++) {
			final int dayNum = i;
			boolean present = days.stream().anyMatch(d -> d.getDayNumber() == dayNum);
			if (!present) {
				throw new ApiException(
						HttpStatus.BAD_REQUEST,
						"Plan days must be contiguous 1.." + plan.getDurationDays() + " (missing day " + dayNum + ")"
				);
			}
		}
	}

	private WorkoutPlan requirePlan(UUID id) {
		return workoutPlanRepository.findByIdWithNode(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Workout plan not found"));
	}

	private Node requireNode(UUID id) {
		return nodeRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Node not found"));
	}

	private Workout requireWorkoutForNode(UUID workoutId, UUID nodeId) {
		Workout workout = workoutRepository.findByIdWithGoalNode(workoutId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Workout not found"));
		if (!workout.getGoalNode().getId().equals(nodeId)) {
			throw new ApiException(
					HttpStatus.BAD_REQUEST,
					"Workout must belong to the same node as the plan"
			);
		}
		if (!workout.isActive()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Workout is not ACTIVE");
		}
		return workout;
	}

	private String resolvePlanKind(String kind) {
		if (!StringUtils.hasText(kind)) {
			return WorkoutPlan.KIND_SKILL;
		}
		String value = kind.trim();
		if (WorkoutPlan.KIND_SKILL.equals(value) || WorkoutPlan.KIND_DAILY_ROUTINE.equals(value)) {
			return value;
		}
		throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid workout plan kind: " + value);
	}

	private String normalizeCode(String code) {
		if (!StringUtils.hasText(code)) {
			return null;
		}
		return code.trim();
	}

	private AdminWorkoutPlanSummaryResponse toSummary(WorkoutPlan plan) {
		int dayCount = (int) workoutPlanDayRepository.countByPlan_Id(plan.getId());
		return new AdminWorkoutPlanSummaryResponse(
				plan.getId(),
				plan.getTitle(),
				new NamedRef(plan.getNode().getId(), plan.getNode().getName()),
				plan.getKind(),
				plan.getCode(),
				plan.getDurationDays(),
				dayCount,
				plan.getStatus(),
				plan.getCreatedAt(),
				plan.getUpdatedAt()
		);
	}

	private AdminWorkoutPlanResponse toDetail(WorkoutPlan plan, List<WorkoutPlanDay> days) {
		return new AdminWorkoutPlanResponse(
				plan.getId(),
				plan.getTitle(),
				plan.getDescription(),
				new NamedRef(plan.getNode().getId(), plan.getNode().getName()),
				plan.getKind(),
				plan.getCode(),
				plan.getDurationDays(),
				plan.getStatus(),
				days.stream().map(this::toDayResponse).toList(),
				plan.getCreatedAt(),
				plan.getUpdatedAt()
		);
	}

	private AdminWorkoutPlanDayResponse toDayResponse(WorkoutPlanDay day) {
		Workout workout = day.getWorkout();
		return new AdminWorkoutPlanDayResponse(
				day.getId(),
				day.getDayNumber(),
				new NamedRef(workout.getId(), workout.getTitle())
		);
	}
}

package com.davendra.calistrack_backend.path.facade;

import com.davendra.calistrack_backend.catalog.entity.Workout;
import com.davendra.calistrack_backend.catalog.entity.WorkoutPlan;
import com.davendra.calistrack_backend.catalog.entity.WorkoutPlanDay;
import com.davendra.calistrack_backend.catalog.repo.WorkoutPlanDayRepository;
import com.davendra.calistrack_backend.catalog.repo.WorkoutPlanRepository;
import com.davendra.calistrack_backend.common.exception.ApiException;
import com.davendra.calistrack_backend.path.catalog.GoalPathCatalog;
import com.davendra.calistrack_backend.path.dto.PlacementResult;
import com.davendra.calistrack_backend.path.dto.PlanDayAssignment;
import com.davendra.calistrack_backend.progress.entity.UserPlanEnrollment;
import com.davendra.calistrack_backend.progress.enums.UserPlanEnrollmentStatus;
import com.davendra.calistrack_backend.progress.repo.UserPlanEnrollmentRepository;
import com.davendra.calistrack_backend.user.entity.AppUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Resolves curated multi-day plans for a focus node and advances day / node progression.
 */
@Service
public class PlanProgressionService {

	private final GoalPathCatalog goalPathCatalog;
	private final WorkoutPlanRepository workoutPlanRepository;
	private final WorkoutPlanDayRepository workoutPlanDayRepository;
	private final UserPlanEnrollmentRepository enrollmentRepository;

	public PlanProgressionService(
			GoalPathCatalog goalPathCatalog,
			WorkoutPlanRepository workoutPlanRepository,
			WorkoutPlanDayRepository workoutPlanDayRepository,
			UserPlanEnrollmentRepository enrollmentRepository
	) {
		this.goalPathCatalog = goalPathCatalog;
		this.workoutPlanRepository = workoutPlanRepository;
		this.workoutPlanDayRepository = workoutPlanDayRepository;
		this.enrollmentRepository = enrollmentRepository;
	}

	@Transactional
	public PlanDayAssignment enrollAndAssignDay1(AppUser user, UUID goalNodeId, UUID focusNodeId) {
		assertFocusOnPath(goalNodeId, focusNodeId);
		WorkoutPlan plan = requireActivePlan(focusNodeId);
		WorkoutPlanDay day1 = requirePlanDay(plan.getId(), 1);

		UserPlanEnrollment enrollment = new UserPlanEnrollment();
		enrollment.setUser(user);
		enrollment.setPlan(plan);
		enrollment.setNode(plan.getNode());
		enrollment.setCurrentDay(1);
		enrollment.setStatus(UserPlanEnrollmentStatus.ACTIVE);
		enrollment = enrollmentRepository.save(enrollment);

		return toAssignment(goalNodeId, plan, enrollment, day1);
	}

	@Transactional
	public PlanDayAssignment enrollAndAssignDay1(AppUser user, PlacementResult placement) {
		return enrollAndAssignDay1(user, placement.goalNodeId(), placement.focusNodeId());
	}

	/**
	 * After finishing the current plan day: bump to next day, or mark enrollment AWAITING_VERIFY.
	 *
	 * @return next day assignment, or empty when the plan is done (awaiting node verify)
	 */
	@Transactional
	public Optional<PlanDayAssignment> advanceAfterDayComplete(AppUser user, UserPlanEnrollment enrollment) {
		if (!enrollment.getUser().getId().equals(user.getId())) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Plan enrollment does not belong to current user");
		}
		if (enrollment.getStatus() != UserPlanEnrollmentStatus.ACTIVE) {
			return Optional.empty();
		}

		WorkoutPlan plan = enrollment.getPlan();
		int finishedDay = enrollment.getCurrentDay();
		if (finishedDay >= plan.getDurationDays()) {
			enrollment.setStatus(UserPlanEnrollmentStatus.AWAITING_VERIFY);
			enrollmentRepository.save(enrollment);
			return Optional.empty();
		}

		int nextDay = finishedDay + 1;
		WorkoutPlanDay day = requirePlanDay(plan.getId(), nextDay);
		enrollment.setCurrentDay(nextDay);
		enrollmentRepository.save(enrollment);

		UUID goalNodeId = user.getCurrentGoalNode() != null ? user.getCurrentGoalNode().getId() : plan.getNode().getId();
		return Optional.of(toAssignment(goalNodeId, plan, enrollment, day));
	}

	/**
	 * After node assessment PASS: complete enrollment and open Day 1 of the next path node that has a plan.
	 */
	@Transactional
	public Optional<NextNodePlanStep> enrollNextNodeAfterPass(AppUser user, UUID goalNodeId, UUID completedNodeId) {
		assertFocusOnPath(goalNodeId, completedNodeId);

		enrollmentRepository
				.findFirstByUser_IdAndNode_IdAndStatusInOrderByCreatedAtDesc(
						user.getId(),
						completedNodeId,
						EnumSet.of(UserPlanEnrollmentStatus.AWAITING_VERIFY, UserPlanEnrollmentStatus.ACTIVE)
				)
				.ifPresent(enrollment -> {
					enrollment.setStatus(UserPlanEnrollmentStatus.COMPLETED);
					enrollment.setCompletedAt(Instant.now());
					enrollmentRepository.save(enrollment);
				});

		List<UUID> path = goalPathCatalog.pathNodeIds(goalNodeId);
		int idx = path.indexOf(completedNodeId);
		List<UUID> skipped = new ArrayList<>();
		for (int i = idx + 1; i < path.size(); i++) {
			UUID candidate = path.get(i);
			Optional<WorkoutPlan> plan = workoutPlanRepository
					.findFirstByNode_IdAndStatusOrderByCreatedAtAsc(candidate, WorkoutPlan.STATUS_ACTIVE);
			if (plan.isPresent()) {
				PlanDayAssignment assignment = enrollAndAssignDay1(user, goalNodeId, candidate);
				return Optional.of(new NextNodePlanStep(assignment, List.copyOf(skipped)));
			}
			skipped.add(candidate);
		}
		return Optional.empty();
	}

	@Transactional(readOnly = true)
	public Optional<UserPlanEnrollment> findActiveOrAwaiting(AppUser user) {
		return enrollmentRepository.findFirstByUser_IdAndStatusInOrderByUpdatedAtDesc(
				user.getId(),
				EnumSet.of(UserPlanEnrollmentStatus.ACTIVE, UserPlanEnrollmentStatus.AWAITING_VERIFY)
		);
	}

	@Transactional(readOnly = true)
	public UserPlanEnrollment requireEnrollment(UUID enrollmentId) {
		return enrollmentRepository.findById(enrollmentId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Plan enrollment not found"));
	}

	private WorkoutPlan requireActivePlan(UUID nodeId) {
		return workoutPlanRepository
				.findFirstByNode_IdAndStatusOrderByCreatedAtAsc(nodeId, WorkoutPlan.STATUS_ACTIVE)
				.orElseThrow(() -> new ApiException(
						HttpStatus.NOT_FOUND,
						"No active workout plan found for focus node: " + nodeId
				));
	}

	private WorkoutPlanDay requirePlanDay(UUID planId, int dayNumber) {
		return workoutPlanDayRepository
				.findByPlan_IdAndDayNumber(planId, dayNumber)
				.orElseThrow(() -> new ApiException(
						HttpStatus.NOT_FOUND,
						"Plan day " + dayNumber + " not found for plan " + planId
				));
	}

	private void assertFocusOnPath(UUID goalNodeId, UUID focusNodeId) {
		List<UUID> path = goalPathCatalog.pathNodeIds(goalNodeId);
		if (!path.contains(focusNodeId)) {
			throw new ApiException(
					HttpStatus.BAD_REQUEST,
					"Focus node " + focusNodeId + " is not on path for goal " + goalNodeId
			);
		}
	}

	private PlanDayAssignment toAssignment(
			UUID goalNodeId,
			WorkoutPlan plan,
			UserPlanEnrollment enrollment,
			WorkoutPlanDay day
	) {
		Workout workout = day.getWorkout();
		return new PlanDayAssignment(
				goalNodeId,
				plan.getNode().getId(),
				workout.getId(),
				workout.getTitle(),
				plan.getId(),
				enrollment.getId(),
				day.getDayNumber(),
				plan.getDurationDays()
		);
	}

	public record NextNodePlanStep(
			PlanDayAssignment assignment,
			List<UUID> skippedNodeIds
	) {
	}
}

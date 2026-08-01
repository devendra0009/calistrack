package com.davendra.calistrack_backend.assessment.service;

import com.davendra.calistrack_backend.assessment.dto.AssessmentResponse;
import com.davendra.calistrack_backend.assessment.dto.GoalPathAssessmentResponse;
import com.davendra.calistrack_backend.assessment.dto.PathAssessmentNodeResponse;
import com.davendra.calistrack_backend.assessment.dto.SubmitAssessmentRequest;
import com.davendra.calistrack_backend.assessment.entity.Assessment;
import com.davendra.calistrack_backend.assessment.enums.AssessmentStatus;
import com.davendra.calistrack_backend.assessment.repo.AssessmentRepository;
import com.davendra.calistrack_backend.catalog.entity.Node;
import com.davendra.calistrack_backend.catalog.repo.NodeRepository;
import com.davendra.calistrack_backend.common.exception.ApiException;
import com.davendra.calistrack_backend.path.catalog.GoalPathCatalog;
import com.davendra.calistrack_backend.path.dto.PlanDayAssignment;
import com.davendra.calistrack_backend.path.facade.PlanProgressionService;
import com.davendra.calistrack_backend.path.facade.PlanProgressionService.NextNodePlanStep;
import com.davendra.calistrack_backend.progress.entity.UserNode;
import com.davendra.calistrack_backend.progress.entity.UserPlanEnrollment;
import com.davendra.calistrack_backend.progress.enums.UserNodeStatus;
import com.davendra.calistrack_backend.progress.enums.UserPlanEnrollmentStatus;
import com.davendra.calistrack_backend.progress.repo.UserNodeRepository;
import com.davendra.calistrack_backend.progress.repo.UserPlanEnrollmentRepository;
import com.davendra.calistrack_backend.user.entity.AppUser;
import com.davendra.calistrack_backend.user.service.CurrentUserService;
import com.davendra.calistrack_backend.workout.entity.WorkoutSession;
import com.davendra.calistrack_backend.workout.enums.WorkoutSessionStatus;
import com.davendra.calistrack_backend.workout.repo.WorkoutSessionRepository;
import com.davendra.calistrack_backend.workout.service.WorkoutSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AssessmentService {

	private final AssessmentRepository assessmentRepository;
	private final UserNodeRepository userNodeRepository;
	private final UserPlanEnrollmentRepository enrollmentRepository;
	private final NodeRepository nodeRepository;
	private final WorkoutSessionRepository workoutSessionRepository;
	private final GoalPathCatalog goalPathCatalog;
	private final PlanProgressionService planProgressionService;
	private final WorkoutSessionService workoutSessionService;
	private final CurrentUserService currentUserService;

	public AssessmentService(
			AssessmentRepository assessmentRepository,
			UserNodeRepository userNodeRepository,
			UserPlanEnrollmentRepository enrollmentRepository,
			NodeRepository nodeRepository,
			WorkoutSessionRepository workoutSessionRepository,
			GoalPathCatalog goalPathCatalog,
			PlanProgressionService planProgressionService,
			WorkoutSessionService workoutSessionService,
			CurrentUserService currentUserService
	) {
		this.assessmentRepository = assessmentRepository;
		this.userNodeRepository = userNodeRepository;
		this.enrollmentRepository = enrollmentRepository;
		this.nodeRepository = nodeRepository;
		this.workoutSessionRepository = workoutSessionRepository;
		this.goalPathCatalog = goalPathCatalog;
		this.planProgressionService = planProgressionService;
		this.workoutSessionService = workoutSessionService;
		this.currentUserService = currentUserService;
	}

	@Transactional(readOnly = true)
	public GoalPathAssessmentResponse getPathForCurrentUser() {
		AppUser user = requireUserWithGoal();
		Node goal = user.getCurrentGoalNode();
		List<UUID> pathIds = goalPathCatalog.pathNodeIds(goal.getId());

		Map<UUID, Node> nodesById = nodeRepository.findAllById(pathIds).stream()
				.collect(Collectors.toMap(Node::getId, Function.identity()));

		Map<UUID, UserNode> userNodesByNodeId = userNodeRepository.findByUser_Id(user.getId()).stream()
				.collect(Collectors.toMap(un -> un.getNode().getId(), Function.identity(), (a, b) -> a));

		Set<UUID> awaitingNodeIds = enrollmentRepository.findByUser_IdOrderByCreatedAtDesc(user.getId()).stream()
				.filter(e -> e.getStatus() == UserPlanEnrollmentStatus.AWAITING_VERIFY)
				.map(e -> e.getNode().getId())
				.collect(Collectors.toSet());

		Map<UUID, String> latestVideoByNode = latestVideoUrls(user.getId(), pathIds);

		List<PathAssessmentNodeResponse> nodes = new java.util.ArrayList<>(pathIds.size());
		int verifiedCount = 0;
		for (int i = 0; i < pathIds.size(); i++) {
			UUID nodeId = pathIds.get(i);
			Node node = nodesById.get(nodeId);
			if (node == null) {
				throw new ApiException(HttpStatus.NOT_FOUND, "Path node not found: " + nodeId);
			}
			UserNode userNode = userNodesByNodeId.get(nodeId);
			boolean verified = userNode != null && userNode.isVerified();
			if (verified) {
				verifiedCount += 1;
			}
			nodes.add(new PathAssessmentNodeResponse(
					node.getId(),
					node.getName(),
					node.getDescription(),
					node.getDifficulty(),
					i + 1,
					nodeId.equals(goal.getId()),
					userNode != null ? userNode.getStatus() : UserNodeStatus.LOCKED,
					verified,
					awaitingNodeIds.contains(nodeId),
					latestVideoByNode.get(nodeId)
			));
		}

		return new GoalPathAssessmentResponse(
				goal.getId(),
				goal.getName(),
				verifiedCount,
				nodes.size(),
				nodes
		);
	}

	/**
	 * Upload proof marks the skill verified. If the node's plan is {@code AWAITING_VERIFY},
	 * unlocks the next path node's Day 1 session.
	 */
	@Transactional
	public AssessmentResponse submitProof(SubmitAssessmentRequest request) {
		AppUser user = requireUserWithGoal();
		Node goal = user.getCurrentGoalNode();
		Set<UUID> pathIds = new HashSet<>(goalPathCatalog.pathNodeIds(goal.getId()));

		if (!pathIds.contains(request.nodeId())) {
			throw new ApiException(
					HttpStatus.BAD_REQUEST,
					"Node is not on your current goal path"
			);
		}

		Node node = nodeRepository.findById(request.nodeId())
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Node not found"));

		WorkoutSession session = null;
		if (request.workoutSessionId() != null) {
			session = workoutSessionRepository.findById(request.workoutSessionId())
					.filter(s -> s.getUser().getId().equals(user.getId()))
					.orElseThrow(() -> new ApiException(
							HttpStatus.NOT_FOUND,
							"Workout session not found"
					));
		}

		Assessment assessment = new Assessment();
		assessment.setUser(user);
		assessment.setNode(node);
		assessment.setWorkoutSession(session);
		assessment.setVideoUrl(request.videoUrl().trim());
		assessment.setStatus(AssessmentStatus.PASSED);
		assessment.setVerified(true);
		assessment.setPerformedAt(Instant.now());
		assessment.setRemarks("Self-verified via assessment video (MVP)");
		assessment = assessmentRepository.save(assessment);

		markUserNodeVerified(user, node);

		Optional<UserPlanEnrollment> awaiting = enrollmentRepository
				.findFirstByUser_IdAndNode_IdAndStatusInOrderByCreatedAtDesc(
						user.getId(),
						node.getId(),
						EnumSet.of(UserPlanEnrollmentStatus.AWAITING_VERIFY)
				);

		if (awaiting.isPresent()) {
			markLatestSessionVerified(user, node.getId());
			unlockNextNodePlan(user, goal.getId(), node.getId());
		}

		return new AssessmentResponse(
				assessment.getId(),
				node.getId(),
				node.getName(),
				assessment.getStatus(),
				assessment.isVerified(),
				assessment.getVideoUrl(),
				assessment.getPerformedAt()
		);
	}

	private void unlockNextNodePlan(AppUser user, UUID goalNodeId, UUID completedNodeId) {
		Optional<NextNodePlanStep> step = planProgressionService.enrollNextNodeAfterPass(
				user,
				goalNodeId,
				completedNodeId
		);
		if (step.isEmpty()) {
			return;
		}

		NextNodePlanStep next = step.get();
		for (UUID skippedId : next.skippedNodeIds()) {
			markUserNodeCompleted(user, skippedId);
		}

		PlanDayAssignment assignment = next.assignment();
		markUserNodeAvailable(user, assignment.focusNodeId());
		workoutSessionService.createPending(user, assignment);
	}

	private void markLatestSessionVerified(AppUser user, UUID nodeId) {
		workoutSessionRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
				.filter(s -> s.getStatus() == WorkoutSessionStatus.COMPLETED)
				.filter(s -> s.getWorkout().isSkill())
				.filter(s -> s.getWorkout().getGoalNode().getId().equals(nodeId))
				.findFirst()
				.ifPresent(s -> {
					s.setVerified(true);
					workoutSessionRepository.save(s);
				});
	}

	private void markUserNodeVerified(AppUser user, Node node) {
		UserNode userNode = userNodeRepository
				.findByUser_IdAndNode_Id(user.getId(), node.getId())
				.orElseGet(() -> {
					UserNode created = new UserNode();
					created.setUser(user);
					created.setNode(node);
					created.setStatus(UserNodeStatus.COMPLETED);
					created.setProgressPercentage(BigDecimal.valueOf(100));
					created.setUnlockedAt(Instant.now());
					return created;
				});

		userNode.setVerified(true);
		userNode.setStatus(UserNodeStatus.COMPLETED);
		userNode.setProgressPercentage(BigDecimal.valueOf(100));
		userNode.setLastAttemptAt(Instant.now());
		if (userNode.getUnlockedAt() == null) {
			userNode.setUnlockedAt(Instant.now());
		}
		userNodeRepository.save(userNode);
	}

	private void markUserNodeCompleted(AppUser user, UUID nodeId) {
		userNodeRepository.findByUser_IdAndNode_Id(user.getId(), nodeId).ifPresent(userNode -> {
			userNode.setStatus(UserNodeStatus.COMPLETED);
			userNode.setProgressPercentage(BigDecimal.valueOf(100));
			userNode.setLastAttemptAt(Instant.now());
			if (userNode.getUnlockedAt() == null) {
				userNode.setUnlockedAt(Instant.now());
			}
			userNodeRepository.save(userNode);
		});
	}

	private void markUserNodeAvailable(AppUser user, UUID nodeId) {
		userNodeRepository.findByUser_IdAndNode_Id(user.getId(), nodeId).ifPresent(userNode -> {
			if (userNode.getStatus() == UserNodeStatus.COMPLETED) {
				return;
			}
			userNode.setStatus(UserNodeStatus.AVAILABLE);
			if (userNode.getUnlockedAt() == null) {
				userNode.setUnlockedAt(Instant.now());
			}
			userNodeRepository.save(userNode);
		});
	}

	private Map<UUID, String> latestVideoUrls(UUID userId, List<UUID> pathIds) {
		if (pathIds.isEmpty()) {
			return Map.of();
		}
		Map<UUID, String> latest = new HashMap<>();
		for (Assessment assessment : assessmentRepository
				.findByUser_IdAndNode_IdInOrderByCreatedAtDesc(userId, pathIds)) {
			UUID nodeId = assessment.getNode().getId();
			if (!latest.containsKey(nodeId) && assessment.getVideoUrl() != null) {
				latest.put(nodeId, assessment.getVideoUrl());
			}
		}
		return latest;
	}

	private AppUser requireUserWithGoal() {
		AppUser user = currentUserService.requireActiveUserWithGoal();
		if (user.getCurrentGoalNode() == null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Set a goal before assessing skills");
		}
		return user;
	}
}

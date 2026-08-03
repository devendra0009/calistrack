package com.davendra.calistrack_backend.onboarding.service;

import com.davendra.calistrack_backend.catalog.entity.Node;
import com.davendra.calistrack_backend.catalog.repo.NodeRepository;
import com.davendra.calistrack_backend.common.exception.ApiException;
import com.davendra.calistrack_backend.onboarding.dto.OnboardingAnswerDto;
import com.davendra.calistrack_backend.onboarding.dto.OnboardingAnswersRequest;
import com.davendra.calistrack_backend.onboarding.dto.OnboardingAnswersResponse;
import com.davendra.calistrack_backend.onboarding.dto.OnboardingNextQuestionResponse;
import com.davendra.calistrack_backend.onboarding.dto.OnboardingQuestionDto;
import com.davendra.calistrack_backend.onboarding.dto.OnboardingQuestionsResponse;
import com.davendra.calistrack_backend.onboarding.dto.OnboardingStatusResponse;
import com.davendra.calistrack_backend.onboarding.dto.OnboardingStepRequest;
import com.davendra.calistrack_backend.onboarding.dto.OnboardingStepResponse;
import com.davendra.calistrack_backend.onboarding.dto.PlacedUserNodeDto;
import com.davendra.calistrack_backend.onboarding.enums.QuestionType;
import com.davendra.calistrack_backend.path.dto.NodePlacement;
import com.davendra.calistrack_backend.path.dto.PlacementAnswer;
import com.davendra.calistrack_backend.path.dto.PlacementResult;
import com.davendra.calistrack_backend.path.dto.PlanDayAssignment;
import com.davendra.calistrack_backend.path.enums.PlacementAnswerType;
import com.davendra.calistrack_backend.path.facade.PlanProgressionService;
import com.davendra.calistrack_backend.path.service.PathPlacementEngine;
import com.davendra.calistrack_backend.progress.entity.UserNode;
import com.davendra.calistrack_backend.progress.enums.UserNodeStatus;
import com.davendra.calistrack_backend.progress.repo.UserNodeRepository;
import com.davendra.calistrack_backend.user.entity.AppUser;
import com.davendra.calistrack_backend.user.repo.AppUserRepository;
import com.davendra.calistrack_backend.user.service.CurrentUserService;
import com.davendra.calistrack_backend.workout.entity.WorkoutSession;
import com.davendra.calistrack_backend.workout.service.WorkoutSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * HTTP use-case orchestrator: validate user state, place on path, enroll Day 1 of focus plan.
 */
@Service
public class OnboardingService {

	private final NodeRepository nodeRepository;
	private final UserNodeRepository userNodeRepository;
	private final AppUserRepository appUserRepository;
	private final OnboardingQuestionProvider questionProvider;
	private final PathPlacementEngine pathPlacementEngine;
	private final PlanProgressionService planProgressionService;
	private final CurrentUserService currentUserService;
	private final WorkoutSessionService workoutSessionService;

	public OnboardingService(
			NodeRepository nodeRepository,
			UserNodeRepository userNodeRepository,
			AppUserRepository appUserRepository,
			OnboardingQuestionProvider questionProvider,
			PathPlacementEngine pathPlacementEngine,
			PlanProgressionService planProgressionService,
			CurrentUserService currentUserService,
			WorkoutSessionService workoutSessionService
	) {
		this.nodeRepository = nodeRepository;
		this.userNodeRepository = userNodeRepository;
		this.appUserRepository = appUserRepository;
		this.questionProvider = questionProvider;
		this.pathPlacementEngine = pathPlacementEngine;
		this.planProgressionService = planProgressionService;
		this.currentUserService = currentUserService;
		this.workoutSessionService = workoutSessionService;
	}

	@Transactional(readOnly = true)
	public OnboardingStatusResponse getStatus() {
		AppUser user = currentUserService.requireActiveUser();
		return new OnboardingStatusResponse(workoutSessionService.hasAnySession(user));
	}

	@Transactional(readOnly = true)
	public OnboardingQuestionsResponse getQuestions(UUID goalNodeId) {
		requireActiveGoalNode(goalNodeId);
		return new OnboardingQuestionsResponse(
				goalNodeId,
				questionProvider.getQuestions(goalNodeId)
		);
	}

	@Transactional(readOnly = true)
	public OnboardingNextQuestionResponse getNextQuestion(UUID goalNodeId, int index) {
		requireActiveGoalNode(goalNodeId);
		List<OnboardingQuestionDto> questions = questionProvider.getQuestions(goalNodeId);
		if (questions.isEmpty()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "No placement questions for this goal");
		}
		if (index < 0 || index >= questions.size()) {
			throw new ApiException(
					HttpStatus.BAD_REQUEST,
					"Question index out of range: " + index + " (total " + questions.size() + ")"
			);
		}
		return new OnboardingNextQuestionResponse(
				goalNodeId,
				index,
				questions.size(),
				questions.get(index)
		);
	}

	@Transactional
	public OnboardingStepResponse submitStep(OnboardingStepRequest request) {
		AppUser user = currentUserService.requireActiveUser();
		UUID goalNodeId = request.goalNodeId();
		requireActiveGoalNode(goalNodeId);

		if (userNodeRepository.existsByUserId(user.getId())) {
			throw new ApiException(HttpStatus.CONFLICT, "Onboarding placement already completed");
		}

		List<OnboardingQuestionDto> questions = questionProvider.getQuestions(goalNodeId);
		if (questions.isEmpty()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "No placement questions for this goal");
		}

		List<OnboardingAnswerDto> answers = request.answers();
		if (answers.size() > questions.size()) {
			throw new ApiException(
					HttpStatus.BAD_REQUEST,
					"Expected at most " + questions.size() + " answers, got " + answers.size()
			);
		}

		for (int i = 0; i < answers.size(); i++) {
			OnboardingQuestionDto expectedQuestion = questions.get(i);
			OnboardingAnswerDto answer = answers.get(i);
			if (!expectedQuestion.nodeId().equals(answer.nodeId())) {
				throw new ApiException(
						HttpStatus.BAD_REQUEST,
						"Answer at index " + i + " must be for node " + expectedQuestion.nodeId()
				);
			}
			if (expectedQuestion.type() != answer.type()) {
				throw new ApiException(
						HttpStatus.BAD_REQUEST,
						"Answer type mismatch for node " + answer.nodeId()
				);
			}
		}

		int lastIndex = answers.size() - 1;
		List<PlacementAnswer> placementAnswers = toPlacementAnswers(answers);

		// Earlier answers in the prefix must have passed; otherwise client skipped a fail.
		for (int i = 0; i < lastIndex; i++) {
			if (!pathPlacementEngine.isAnswerPassed(placementAnswers.get(i))) {
				throw new ApiException(
						HttpStatus.BAD_REQUEST,
						"Answer at index " + i + " failed; cannot continue past a failed question"
				);
			}
		}

		boolean lastPassed = pathPlacementEngine.isAnswerPassed(placementAnswers.get(lastIndex));
		int total = questions.size();

		if (lastPassed && lastIndex + 1 < total) {
			int nextIndex = lastIndex + 1;
			return OnboardingStepResponse.next(
					nextIndex,
					total,
					questions.get(nextIndex)
			);
		}

		OnboardingAnswersResponse placement = applyPlacement(user, goalNodeId, placementAnswers);
		return OnboardingStepResponse.placed(lastIndex, total, placement);
	}

	@Transactional
	public OnboardingAnswersResponse submitAnswers(OnboardingAnswersRequest request) {
		AppUser user = currentUserService.requireActiveUser();
		UUID goalNodeId = request.goalNodeId();
		requireActiveGoalNode(goalNodeId);

		if (userNodeRepository.existsByUserId(user.getId())) {
			throw new ApiException(HttpStatus.CONFLICT, "Onboarding placement already completed");
		}

		return applyPlacement(user, goalNodeId, toPlacementAnswers(request.answers()));
	}

	private OnboardingAnswersResponse applyPlacement(
			AppUser user,
			UUID goalNodeId,
			List<PlacementAnswer> placementAnswers
	) {
		Node goalNode = requireActiveGoalNode(goalNodeId);
		workoutSessionService.assertNoOpenSession(user);

		PlacementResult placement = pathPlacementEngine.place(goalNodeId, placementAnswers);

		List<UserNode> placed = toUserNodes(user, placement);
		userNodeRepository.saveAll(placed);

		if (user.getCurrentGoalNode() == null
				|| !goalNodeId.equals(user.getCurrentGoalNode().getId())) {
			user.setCurrentGoalNode(goalNode);
			appUserRepository.save(user);
		}

		PlanDayAssignment assignment = planProgressionService.enrollAndAssignDay1(user, placement);
		WorkoutSession session = workoutSessionService.createPending(user, assignment);

		List<PlacedUserNodeDto> placedDtos = placement.placements().stream()
				.map(p -> new PlacedUserNodeDto(p.nodeId(), p.status()))
				.toList();

		return new OnboardingAnswersResponse(
				assignment.goalNodeId(),
				assignment.focusNodeId(),
				session.getId(),
				assignment.workoutId(),
				assignment.workoutTitle(),
				session.getStatus(),
				placedDtos
		);
	}

	private Node requireActiveGoalNode(UUID goalNodeId) {
		return nodeRepository
				.findByIdAndStatus(goalNodeId, Node.STATUS_ACTIVE)
				.orElseThrow(() -> new ApiException(
						HttpStatus.NOT_FOUND,
						"Active goal node not found: " + goalNodeId
				));
	}

	private List<PlacementAnswer> toPlacementAnswers(List<OnboardingAnswerDto> answers) {
		return answers.stream()
				.map(a -> new PlacementAnswer(a.nodeId(), toPlacementType(a.type()), a.value()))
				.toList();
	}

	private PlacementAnswerType toPlacementType(QuestionType type) {
		return switch (type) {
			case REPS -> PlacementAnswerType.REPS;
			case YES_NO -> PlacementAnswerType.YES_NO;
		};
	}

	private List<UserNode> toUserNodes(AppUser user, PlacementResult placement) {
		Map<UUID, Node> nodesById = nodeRepository.findAllById(
						placement.placements().stream().map(NodePlacement::nodeId).toList()
				).stream()
				.collect(Collectors.toMap(Node::getId, Function.identity()));

		Instant now = Instant.now();
		List<UserNode> result = new ArrayList<>(placement.placements().size());
		for (NodePlacement p : placement.placements()) {
			Node node = nodesById.get(p.nodeId());
			if (node == null) {
				throw new ApiException(HttpStatus.NOT_FOUND, "Path node not found: " + p.nodeId());
			}

			UserNode userNode = new UserNode();
			userNode.setUser(user);
			userNode.setNode(node);
			userNode.setStatus(p.status());
			userNode.setVerified(false);
			userNode.setProgressPercentage(
					p.status() == UserNodeStatus.COMPLETED ? BigDecimal.valueOf(100) : BigDecimal.ZERO
			);
			if (p.status() != UserNodeStatus.LOCKED) {
				userNode.setUnlockedAt(now);
			}
			result.add(userNode);
		}
		return result;
	}
}

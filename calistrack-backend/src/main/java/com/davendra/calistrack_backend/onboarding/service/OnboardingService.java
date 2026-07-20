package com.davendra.calistrack_backend.onboarding.service;

import com.davendra.calistrack_backend.catalog.entity.Node;
import com.davendra.calistrack_backend.catalog.entity.Workout;
import com.davendra.calistrack_backend.catalog.repo.NodeRepository;
import com.davendra.calistrack_backend.catalog.repo.WorkoutRepository;
import com.davendra.calistrack_backend.common.exception.ApiException;
import com.davendra.calistrack_backend.onboarding.dto.OnboardingAnswerDto;
import com.davendra.calistrack_backend.onboarding.dto.OnboardingAnswersRequest;
import com.davendra.calistrack_backend.onboarding.dto.OnboardingAnswersResponse;
import com.davendra.calistrack_backend.onboarding.dto.OnboardingQuestionsResponse;
import com.davendra.calistrack_backend.onboarding.dto.OnboardingStatusResponse;
import com.davendra.calistrack_backend.onboarding.dto.PlacedUserNodeDto;
import com.davendra.calistrack_backend.onboarding.enums.QuestionType;
import com.davendra.calistrack_backend.path.dto.NodePlacement;
import com.davendra.calistrack_backend.path.dto.PlacementAnswer;
import com.davendra.calistrack_backend.path.dto.PlacementResult;
import com.davendra.calistrack_backend.path.dto.WorkoutAssignment;
import com.davendra.calistrack_backend.path.enums.PlacementAnswerType;
import com.davendra.calistrack_backend.path.facade.NextWorkoutFacade;
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
 * HTTP use-case orchestrator: validate user state, place on path, assign next workout, persist.
 * Placement and workout selection live in {@link PathPlacementEngine} / {@link NextWorkoutFacade}.
 */
@Service
public class OnboardingService {

	private final NodeRepository nodeRepository;
	private final WorkoutRepository workoutRepository;
	private final UserNodeRepository userNodeRepository;
	private final AppUserRepository appUserRepository;
	private final OnboardingQuestionProvider questionProvider;
	private final PathPlacementEngine pathPlacementEngine;
	private final NextWorkoutFacade nextWorkoutFacade;
	private final CurrentUserService currentUserService;
	private final WorkoutSessionService workoutSessionService;

	public OnboardingService(
			NodeRepository nodeRepository,
			WorkoutRepository workoutRepository,
			UserNodeRepository userNodeRepository,
			AppUserRepository appUserRepository,
			OnboardingQuestionProvider questionProvider,
			PathPlacementEngine pathPlacementEngine,
			NextWorkoutFacade nextWorkoutFacade,
			CurrentUserService currentUserService,
			WorkoutSessionService workoutSessionService
	) {
		this.nodeRepository = nodeRepository;
		this.workoutRepository = workoutRepository;
		this.userNodeRepository = userNodeRepository;
		this.appUserRepository = appUserRepository;
		this.questionProvider = questionProvider;
		this.pathPlacementEngine = pathPlacementEngine;
		this.nextWorkoutFacade = nextWorkoutFacade;
		this.currentUserService = currentUserService;
		this.workoutSessionService = workoutSessionService;
	}

	@Transactional(readOnly = true)
	public OnboardingStatusResponse getStatus() {
		AppUser user = currentUserService.requireActiveUser();
		// A workout_session is created only after questionnaire answers succeed.
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

	@Transactional
	public OnboardingAnswersResponse submitAnswers(OnboardingAnswersRequest request) {
		AppUser user = currentUserService.requireActiveUser();
		UUID goalNodeId = request.goalNodeId();
		Node goalNode = requireActiveGoalNode(goalNodeId);

		if (userNodeRepository.existsByUserId(user.getId())) {
			throw new ApiException(HttpStatus.CONFLICT, "Onboarding placement already completed");
		}
		workoutSessionService.assertNoOpenSession(user);

		PlacementResult placement = pathPlacementEngine.place(
				goalNodeId,
				toPlacementAnswers(request.answers())
		);

		List<UserNode> placed = toUserNodes(user, placement);
		userNodeRepository.saveAll(placed);

		if (user.getCurrentGoalNode() == null
				|| !goalNodeId.equals(user.getCurrentGoalNode().getId())) {
			user.setCurrentGoalNode(goalNode);
			appUserRepository.save(user);
		}

		WorkoutAssignment assignment = nextWorkoutFacade.nextWorkout(placement);
		Workout workout = workoutRepository.findByIdWithGoalNode(assignment.workoutId())
				.orElseThrow(() -> new ApiException(
						HttpStatus.NOT_FOUND,
						"Workout not found: " + assignment.workoutId()
				));

		WorkoutSession session = workoutSessionService.createPending(user, workout);

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

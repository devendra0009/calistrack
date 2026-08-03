package com.davendra.calistrack_backend.path.service;

import com.davendra.calistrack_backend.catalog.entity.Node;
import com.davendra.calistrack_backend.catalog.repo.NodeRepository;
import com.davendra.calistrack_backend.common.exception.ApiException;
import com.davendra.calistrack_backend.path.catalog.GoalPathCatalog;
import com.davendra.calistrack_backend.path.dto.NodePlacement;
import com.davendra.calistrack_backend.path.dto.PathQuestion;
import com.davendra.calistrack_backend.path.dto.PlacementAnswer;
import com.davendra.calistrack_backend.path.dto.PlacementResult;
import com.davendra.calistrack_backend.progress.enums.UserNodeStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DefaultPathPlacementEngine implements PathPlacementEngine {

	private final GoalPathCatalog goalPathCatalog;
	private final NodeRepository nodeRepository;

	public DefaultPathPlacementEngine(GoalPathCatalog goalPathCatalog, NodeRepository nodeRepository) {
		this.goalPathCatalog = goalPathCatalog;
		this.nodeRepository = nodeRepository;
	}

	@Override
	public PlacementResult place(UUID goalNodeId, List<PlacementAnswer> answers) {
		requireActiveGoalNode(goalNodeId);

		List<PathQuestion> expected = goalPathCatalog.questionsFor(goalNodeId);
		validatePrefixAnswers(expected, answers);

		List<UUID> path = goalPathCatalog.pathNodeIds(goalNodeId);
		Map<UUID, Node> nodesById = loadNodes(path);

		UUID focusNodeId = resolveFocusNodeId(goalNodeId, expected, answers, nodesById);
		List<NodePlacement> placements = buildPlacements(path, focusNodeId);

		return new PlacementResult(goalNodeId, focusNodeId, List.copyOf(placements));
	}

	@Override
	public boolean isAnswerPassed(PlacementAnswer answer) {
		validateAnswerValue(answer);
		Node node = nodeRepository
				.findById(answer.nodeId())
				.orElseThrow(() -> new ApiException(
						HttpStatus.NOT_FOUND,
						"Path node not found: " + answer.nodeId()
				));
		return isPassed(answer, node);
	}

	private Node requireActiveGoalNode(UUID goalNodeId) {
		return nodeRepository
				.findByIdAndStatus(goalNodeId, Node.STATUS_ACTIVE)
				.orElseThrow(() -> new ApiException(
						HttpStatus.NOT_FOUND,
						"Active goal node not found: " + goalNodeId
				));
	}

	private Map<UUID, Node> loadNodes(List<UUID> pathNodeIds) {
		Map<UUID, Node> byId = nodeRepository.findAllById(pathNodeIds).stream()
				.collect(Collectors.toMap(Node::getId, Function.identity()));
		for (UUID id : pathNodeIds) {
			if (!byId.containsKey(id)) {
				throw new ApiException(HttpStatus.NOT_FOUND, "Path node not found: " + id);
			}
		}
		return byId;
	}

	/**
	 * Answers must be the ordered prefix expected[0..n). Placement is allowed when the
	 * prefix ends on a fail, or when n equals the full question list (all pass → goal).
	 */
	private void validatePrefixAnswers(List<PathQuestion> expected, List<PlacementAnswer> answers) {
		if (answers == null || answers.isEmpty()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "At least one answer is required");
		}
		if (answers.size() > expected.size()) {
			throw new ApiException(
					HttpStatus.BAD_REQUEST,
					"Expected at most " + expected.size() + " answers, got " + answers.size()
			);
		}

		for (int i = 0; i < answers.size(); i++) {
			PlacementAnswer answer = answers.get(i);
			PathQuestion question = expected.get(i);
			if (!question.nodeId().equals(answer.nodeId())) {
				throw new ApiException(
						HttpStatus.BAD_REQUEST,
						"Answer at index " + i + " must be for node " + question.nodeId()
								+ ", got " + answer.nodeId()
				);
			}
			if (question.type() != answer.type()) {
				throw new ApiException(
						HttpStatus.BAD_REQUEST,
						"Answer type mismatch for node " + answer.nodeId()
								+ ": expected " + question.type()
				);
			}
			validateAnswerValue(answer);
		}
	}

	private void validateAnswerValue(PlacementAnswer answer) {
		switch (answer.type()) {
			case REPS -> parseNumber(answer.value(), answer.nodeId());
			case YES_NO -> parseBoolean(answer.value(), answer.nodeId());
		}
	}

	private UUID resolveFocusNodeId(
			UUID goalNodeId,
			List<PathQuestion> expected,
			List<PlacementAnswer> answers,
			Map<UUID, Node> nodesById
	) {
		Map<UUID, PlacementAnswer> byNode = new HashMap<>();
		for (PlacementAnswer answer : answers) {
			byNode.put(answer.nodeId(), answer);
		}

		boolean sawFail = false;
		UUID focusFromFail = null;
		for (int i = 0; i < answers.size(); i++) {
			PathQuestion question = expected.get(i);
			PlacementAnswer answer = byNode.get(question.nodeId());
			Node node = nodesById.get(question.nodeId());
			if (!isPassed(answer, node)) {
				sawFail = true;
				focusFromFail = question.nodeId();
				break;
			}
		}

		if (sawFail) {
			return focusFromFail;
		}

		if (answers.size() < expected.size()) {
			throw new ApiException(
					HttpStatus.BAD_REQUEST,
					"Incomplete answers: all provided answers passed but more questions remain"
			);
		}

		return goalNodeId;
	}

	private boolean isPassed(PlacementAnswer answer, Node node) {
		return switch (answer.type()) {
			case YES_NO -> parseBoolean(answer.value(), answer.nodeId());
			case REPS -> {
				BigDecimal reps = parseNumber(answer.value(), answer.nodeId());
				yield reps.compareTo(node.getTargetValue()) >= 0;
			}
		};
	}

	private List<NodePlacement> buildPlacements(List<UUID> path, UUID focusNodeId) {
		int focusIndex = path.indexOf(focusNodeId);
		if (focusIndex < 0) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Focus node is not on goal path: " + focusNodeId);
		}

		List<NodePlacement> result = new ArrayList<>(path.size());
		for (int i = 0; i < path.size(); i++) {
			UUID nodeId = path.get(i);
			UserNodeStatus status;
			if (i < focusIndex) {
				status = UserNodeStatus.COMPLETED;
			} else if (i == focusIndex) {
				status = UserNodeStatus.AVAILABLE;
			} else {
				status = UserNodeStatus.LOCKED;
			}
			result.add(new NodePlacement(nodeId, status));
		}
		return result;
	}

	private BigDecimal parseNumber(Object value, UUID nodeId) {
		if (value instanceof Number number) {
			return BigDecimal.valueOf(number.doubleValue());
		}
		if (value instanceof String s) {
			try {
				return new BigDecimal(s.trim());
			} catch (NumberFormatException ignored) {
				// fall through
			}
		}
		throw new ApiException(HttpStatus.BAD_REQUEST, "REPS answer must be a number for node: " + nodeId);
	}

	private boolean parseBoolean(Object value, UUID nodeId) {
		if (value instanceof Boolean b) {
			return b;
		}
		if (value instanceof String s) {
			if ("true".equalsIgnoreCase(s.trim())) {
				return true;
			}
			if ("false".equalsIgnoreCase(s.trim())) {
				return false;
			}
		}
		throw new ApiException(HttpStatus.BAD_REQUEST, "YES_NO answer must be a boolean for node: " + nodeId);
	}
}

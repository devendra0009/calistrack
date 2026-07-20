package com.davendra.calistrack_backend.admin.service;

import com.davendra.calistrack_backend.admin.dto.AdminPathQuestionRequest;
import com.davendra.calistrack_backend.admin.dto.AdminPathQuestionResponse;
import com.davendra.calistrack_backend.admin.dto.NamedRef;
import com.davendra.calistrack_backend.catalog.entity.Node;
import com.davendra.calistrack_backend.catalog.repo.NodeRepository;
import com.davendra.calistrack_backend.common.exception.ApiException;
import com.davendra.calistrack_backend.path.catalog.GoalPathCatalog;
import com.davendra.calistrack_backend.path.entity.PathQuestionEntity;
import com.davendra.calistrack_backend.path.repo.PathQuestionRepository;
import com.davendra.calistrack_backend.user.service.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AdminPathQuestionService {

	private final CurrentUserService currentUserService;
	private final PathQuestionRepository pathQuestionRepository;
	private final NodeRepository nodeRepository;
	private final GoalPathCatalog goalPathCatalog;

	public AdminPathQuestionService(
			CurrentUserService currentUserService,
			PathQuestionRepository pathQuestionRepository,
			NodeRepository nodeRepository,
			GoalPathCatalog goalPathCatalog
	) {
		this.currentUserService = currentUserService;
		this.pathQuestionRepository = pathQuestionRepository;
		this.nodeRepository = nodeRepository;
		this.goalPathCatalog = goalPathCatalog;
	}

	@Transactional(readOnly = true)
	public List<AdminPathQuestionResponse> list(UUID goalNodeId) {
		currentUserService.requireAdmin();
		List<PathQuestionEntity> rows = goalNodeId != null
				? pathQuestionRepository.findByGoalNode_IdOrderBySortOrderAsc(goalNodeId)
				: pathQuestionRepository.findAllByOrderBySortOrderAsc();
		return rows.stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public AdminPathQuestionResponse get(UUID id) {
		currentUserService.requireAdmin();
		return toResponse(requireQuestion(id));
	}

	@Transactional
	public AdminPathQuestionResponse create(AdminPathQuestionRequest request) {
		currentUserService.requireAdmin();
		Node goal = requireNode(request.goalNodeId());
		Node node = requireNode(request.nodeId());
		assertNodeOnGoalPath(goal.getId(), node.getId());
		assertUnique(goal.getId(), node.getId(), request.sortOrder(), null);

		PathQuestionEntity entity = new PathQuestionEntity();
		entity.setGoalNode(goal);
		entity.setNode(node);
		entity.setPrompt(request.prompt().trim());
		entity.setAnswerType(request.answerType());
		entity.setSortOrder(request.sortOrder());
		return toResponse(pathQuestionRepository.save(entity));
	}

	@Transactional
	public AdminPathQuestionResponse update(UUID id, AdminPathQuestionRequest request) {
		currentUserService.requireAdmin();
		PathQuestionEntity entity = requireQuestion(id);
		Node goal = entity.getGoalNode();
		if (!goal.getId().equals(request.goalNodeId())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot change goalNodeId on an existing question");
		}
		Node node = requireNode(request.nodeId());
		assertNodeOnGoalPath(goal.getId(), node.getId());
		assertUnique(goal.getId(), node.getId(), request.sortOrder(), id);

		entity.setNode(node);
		entity.setPrompt(request.prompt().trim());
		entity.setAnswerType(request.answerType());
		entity.setSortOrder(request.sortOrder());
		return toResponse(pathQuestionRepository.save(entity));
	}

	@Transactional
	public void delete(UUID id) {
		currentUserService.requireAdmin();
		PathQuestionEntity entity = requireQuestion(id);
		pathQuestionRepository.delete(entity);
	}

	private void assertNodeOnGoalPath(UUID goalNodeId, UUID nodeId) {
		List<UUID> path = goalPathCatalog.pathNodeIds(goalNodeId);
		Set<UUID> pathSet = new HashSet<>(path);
		if (!pathSet.contains(nodeId)) {
			throw new ApiException(
					HttpStatus.BAD_REQUEST,
					"Question node must be on the goal path (via node_edge ancestors). "
							+ "Wire path edges first, then attach questions to nodes on that path."
			);
		}
	}

	private void assertUnique(UUID goalNodeId, UUID nodeId, int sortOrder, UUID excludeId) {
		boolean sortTaken = excludeId == null
				? pathQuestionRepository.existsByGoalNode_IdAndSortOrder(goalNodeId, sortOrder)
				: pathQuestionRepository.existsByGoalNode_IdAndSortOrderAndIdNot(goalNodeId, sortOrder, excludeId);
		if (sortTaken) {
			throw new ApiException(HttpStatus.CONFLICT, "sortOrder already used for this goal: " + sortOrder);
		}
		boolean nodeTaken = excludeId == null
				? pathQuestionRepository.existsByGoalNode_IdAndNode_Id(goalNodeId, nodeId)
				: pathQuestionRepository.existsByGoalNode_IdAndNode_IdAndIdNot(goalNodeId, nodeId, excludeId);
		if (nodeTaken) {
			throw new ApiException(HttpStatus.CONFLICT, "A question for this path node already exists on this goal");
		}
	}

	private PathQuestionEntity requireQuestion(UUID id) {
		return pathQuestionRepository.findDetailedById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Path question not found: " + id));
	}

	private Node requireNode(UUID id) {
		return nodeRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Node not found: " + id));
	}

	private AdminPathQuestionResponse toResponse(PathQuestionEntity q) {
		return toResponse(q, q.getGoalNode(), q.getNode());
	}

	private AdminPathQuestionResponse toResponse(PathQuestionEntity q, Node goal, Node node) {
		return new AdminPathQuestionResponse(
				q.getId(),
				new NamedRef(goal.getId(), goal.getName()),
				new NamedRef(node.getId(), node.getName()),
				q.getPrompt(),
				q.getAnswerType(),
				q.getSortOrder(),
				q.getCreatedAt()
		);
	}
}

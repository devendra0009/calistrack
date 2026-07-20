package com.davendra.calistrack_backend.admin.service;

import com.davendra.calistrack_backend.admin.dto.AdminNodeRequest;
import com.davendra.calistrack_backend.admin.dto.AdminNodeResponse;
import com.davendra.calistrack_backend.admin.dto.NamedRef;
import com.davendra.calistrack_backend.catalog.entity.Exercise;
import com.davendra.calistrack_backend.catalog.entity.Node;
import com.davendra.calistrack_backend.catalog.repo.ExerciseRepository;
import com.davendra.calistrack_backend.catalog.repo.NodeRepository;
import com.davendra.calistrack_backend.common.exception.ApiException;
import com.davendra.calistrack_backend.user.service.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminNodeService {

	private static final String STATUS_DEPRECATED = "DEPRECATED";

	private final CurrentUserService currentUserService;
	private final NodeRepository nodeRepository;
	private final ExerciseRepository exerciseRepository;

	public AdminNodeService(
			CurrentUserService currentUserService,
			NodeRepository nodeRepository,
			ExerciseRepository exerciseRepository
	) {
		this.currentUserService = currentUserService;
		this.nodeRepository = nodeRepository;
		this.exerciseRepository = exerciseRepository;
	}

	@Transactional(readOnly = true)
	public List<AdminNodeResponse> list(String status) {
		currentUserService.requireAdmin();
		List<Node> nodes = StringUtils.hasText(status)
				? nodeRepository.findByStatusOrderByNameAsc(status.trim())
				: nodeRepository.findAllByOrderByNameAsc();
		Map<UUID, Exercise> exercisesById = loadExercises(nodes);
		return nodes.stream().map(n -> toResponse(n, exercisesById.get(n.getExerciseId()))).toList();
	}

	@Transactional(readOnly = true)
	public AdminNodeResponse get(UUID id) {
		currentUserService.requireAdmin();
		Node node = requireNode(id);
		Exercise exercise = requireExercise(node.getExerciseId());
		return toResponse(node, exercise);
	}

	@Transactional
	public AdminNodeResponse create(AdminNodeRequest request) {
		currentUserService.requireAdmin();
		assertUniqueName(request.name(), null);
		Exercise exercise = requireExercise(request.exerciseId());

		Node node = new Node();
		apply(node, request);
		Node saved = nodeRepository.save(node);
		return toResponse(saved, exercise);
	}

	@Transactional
	public AdminNodeResponse update(UUID id, AdminNodeRequest request) {
		currentUserService.requireAdmin();
		Node node = requireNode(id);
		assertUniqueName(request.name(), id);
		Exercise exercise = requireExercise(request.exerciseId());
		apply(node, request);
		return toResponse(nodeRepository.save(node), exercise);
	}

	@Transactional
	public AdminNodeResponse deprecate(UUID id) {
		currentUserService.requireAdmin();
		Node node = requireNode(id);
		node.setStatus(STATUS_DEPRECATED);
		Exercise exercise = requireExercise(node.getExerciseId());
		return toResponse(nodeRepository.save(node), exercise);
	}

	private void apply(Node node, AdminNodeRequest request) {
		node.setName(request.name().trim());
		node.setDescription(request.description());
		node.setNodeType(request.nodeType().trim());
		node.setExerciseId(request.exerciseId());
		node.setTargetValue(request.targetValue());
		node.setOperator(request.operator().trim());
		node.setUnitLabel(request.unitLabel().trim());
		node.setDifficulty(request.difficulty().trim());
		node.setXpReward(request.xpReward());
		node.setEstimatedMinutes(request.estimatedMinutes());
		node.setStatus(StringUtils.hasText(request.status()) ? request.status().trim() : Node.STATUS_ACTIVE);
	}

	private void assertUniqueName(String name, UUID excludeId) {
		boolean taken = excludeId == null
				? nodeRepository.existsByNameIgnoreCase(name.trim())
				: nodeRepository.existsByNameIgnoreCaseAndIdNot(name.trim(), excludeId);
		if (taken) {
			throw new ApiException(HttpStatus.CONFLICT, "Node name already exists: " + name);
		}
	}

	private Map<UUID, Exercise> loadExercises(List<Node> nodes) {
		List<UUID> ids = nodes.stream().map(Node::getExerciseId).distinct().toList();
		return exerciseRepository.findAllById(ids).stream()
				.collect(Collectors.toMap(Exercise::getId, Function.identity()));
	}

	private Node requireNode(UUID id) {
		return nodeRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Node not found: " + id));
	}

	private Exercise requireExercise(UUID id) {
		return exerciseRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Exercise not found: " + id));
	}

	private AdminNodeResponse toResponse(Node node, Exercise exercise) {
		NamedRef exerciseRef = exercise == null
				? new NamedRef(node.getExerciseId(), "(missing exercise)")
				: new NamedRef(exercise.getId(), exercise.getName());
		return new AdminNodeResponse(
				node.getId(),
				node.getName(),
				node.getDescription(),
				node.getNodeType(),
				exerciseRef,
				node.getTargetValue(),
				node.getOperator(),
				node.getUnitLabel(),
				node.getDifficulty(),
				node.getXpReward(),
				node.getEstimatedMinutes(),
				node.getStatus(),
				node.getCreatedAt(),
				node.getUpdatedAt()
		);
	}
}

package com.davendra.calistrack_backend.admin.service;

import com.davendra.calistrack_backend.admin.dto.AdminNodeEdgeRequest;
import com.davendra.calistrack_backend.admin.dto.AdminNodeEdgeResponse;
import com.davendra.calistrack_backend.admin.dto.NamedRef;
import com.davendra.calistrack_backend.catalog.entity.Node;
import com.davendra.calistrack_backend.catalog.entity.NodeEdge;
import com.davendra.calistrack_backend.catalog.repo.NodeEdgeRepository;
import com.davendra.calistrack_backend.catalog.repo.NodeRepository;
import com.davendra.calistrack_backend.common.exception.ApiException;
import com.davendra.calistrack_backend.user.service.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class AdminNodeEdgeService {

	private final CurrentUserService currentUserService;
	private final NodeEdgeRepository nodeEdgeRepository;
	private final NodeRepository nodeRepository;

	public AdminNodeEdgeService(
			CurrentUserService currentUserService,
			NodeEdgeRepository nodeEdgeRepository,
			NodeRepository nodeRepository
	) {
		this.currentUserService = currentUserService;
		this.nodeEdgeRepository = nodeEdgeRepository;
		this.nodeRepository = nodeRepository;
	}

	@Transactional(readOnly = true)
	public List<AdminNodeEdgeResponse> list(UUID nodeId) {
		currentUserService.requireAdmin();
		List<NodeEdge> edges;
		if (nodeId != null) {
			edges = nodeEdgeRepository.findByFromNode_IdInOrToNode_IdIn(List.of(nodeId), List.of(nodeId));
		} else {
			edges = nodeEdgeRepository.findAllByOrderByCreatedAtAsc();
		}
		return edges.stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public AdminNodeEdgeResponse get(UUID id) {
		currentUserService.requireAdmin();
		return toResponse(requireEdge(id));
	}

	@Transactional
	public AdminNodeEdgeResponse create(AdminNodeEdgeRequest request) {
		currentUserService.requireAdmin();
		if (request.fromNodeId().equals(request.toNodeId())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "fromNodeId and toNodeId must differ");
		}
		Node from = requireNode(request.fromNodeId());
		Node to = requireNode(request.toNodeId());
		if (nodeEdgeRepository.existsByFromNode_IdAndToNode_Id(from.getId(), to.getId())) {
			throw new ApiException(
					HttpStatus.CONFLICT,
					"Edge already exists: " + from.getName() + " → " + to.getName()
			);
		}

		NodeEdge edge = new NodeEdge();
		edge.setFromNode(from);
		edge.setToNode(to);
		edge.setRelationType(StringUtils.hasText(request.relationType())
				? request.relationType().trim()
				: "PREREQUISITE");
		return toResponse(nodeEdgeRepository.save(edge));
	}

	@Transactional
	public void delete(UUID id) {
		currentUserService.requireAdmin();
		NodeEdge edge = requireEdge(id);
		nodeEdgeRepository.delete(edge);
	}

	private NodeEdge requireEdge(UUID id) {
		return nodeEdgeRepository.findDetailedById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Node edge not found: " + id));
	}

	private Node requireNode(UUID id) {
		return nodeRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Node not found: " + id));
	}

	private AdminNodeEdgeResponse toResponse(NodeEdge edge) {
		return new AdminNodeEdgeResponse(
				edge.getId(),
				new NamedRef(edge.getFromNode().getId(), edge.getFromNode().getName()),
				new NamedRef(edge.getToNode().getId(), edge.getToNode().getName()),
				edge.getRelationType(),
				edge.getCreatedAt()
		);
	}
}

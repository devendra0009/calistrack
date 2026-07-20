package com.davendra.calistrack_backend.catalog.service;

import com.davendra.calistrack_backend.catalog.dto.CatalogNodeResponse;
import com.davendra.calistrack_backend.catalog.entity.Node;
import com.davendra.calistrack_backend.catalog.repo.NodeRepository;
import com.davendra.calistrack_backend.common.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CatalogNodeService {

	private final NodeRepository nodeRepository;

	public CatalogNodeService(NodeRepository nodeRepository) {
		this.nodeRepository = nodeRepository;
	}

	@Transactional(readOnly = true)
	public List<CatalogNodeResponse> listActive() {
		return nodeRepository.findByStatusOrderByNameAsc(Node.STATUS_ACTIVE).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public CatalogNodeResponse getActive(UUID id) {
		Node node = nodeRepository
				.findByIdAndStatus(id, Node.STATUS_ACTIVE)
				.orElseThrow(() -> new ApiException(
						HttpStatus.NOT_FOUND,
						"Active node not found: " + id
				));
		return toResponse(node);
	}

	private CatalogNodeResponse toResponse(Node node) {
		return new CatalogNodeResponse(
				node.getId(),
				node.getName(),
				node.getDescription(),
				node.getNodeType(),
				node.getDifficulty()
		);
	}
}

package com.davendra.calistrack_backend.admin.controller;

import com.davendra.calistrack_backend.admin.dto.AdminNodeEdgeRequest;
import com.davendra.calistrack_backend.admin.dto.AdminNodeEdgeResponse;
import com.davendra.calistrack_backend.admin.service.AdminNodeEdgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/node-edges")
@Tag(name = "Admin — Node edges", description = "Prerequisite graph edges between nodes (ADMIN role)")
public class AdminNodeEdgeController {

	private final AdminNodeEdgeService adminNodeEdgeService;

	public AdminNodeEdgeController(AdminNodeEdgeService adminNodeEdgeService) {
		this.adminNodeEdgeService = adminNodeEdgeService;
	}

	@GetMapping
	@Operation(
			summary = "List edges",
			description = "Includes fromNode/toNode {id, name}. Optional ?nodeId= to filter edges touching a node"
	)
	public List<AdminNodeEdgeResponse> list(@RequestParam(required = false) UUID nodeId) {
		return adminNodeEdgeService.list(nodeId);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get edge by id")
	public AdminNodeEdgeResponse get(@PathVariable UUID id) {
		return adminNodeEdgeService.get(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
			summary = "Create edge",
			description = "fromNode is a prerequisite of toNode; relationType defaults to PREREQUISITE"
	)
	public AdminNodeEdgeResponse create(@Valid @RequestBody AdminNodeEdgeRequest request) {
		return adminNodeEdgeService.create(request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete edge")
	public void delete(@PathVariable UUID id) {
		adminNodeEdgeService.delete(id);
	}
}

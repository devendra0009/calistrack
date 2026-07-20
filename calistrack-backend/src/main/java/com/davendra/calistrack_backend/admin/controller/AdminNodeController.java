package com.davendra.calistrack_backend.admin.controller;

import com.davendra.calistrack_backend.admin.dto.AdminNodeRequest;
import com.davendra.calistrack_backend.admin.dto.AdminNodeResponse;
import com.davendra.calistrack_backend.admin.service.AdminNodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/nodes")
@Tag(name = "Admin — Nodes", description = "Catalog CRUD for skill/goal nodes (ADMIN role)")
public class AdminNodeController {

	private final AdminNodeService adminNodeService;

	public AdminNodeController(AdminNodeService adminNodeService) {
		this.adminNodeService = adminNodeService;
	}

	@GetMapping
	@Operation(
			summary = "List nodes",
			description = "Includes nested exercise {id, name}. Optional ?status= filter"
	)
	public List<AdminNodeResponse> list(@RequestParam(required = false) String status) {
		return adminNodeService.list(status);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get node by id", description = "Includes nested exercise {id, name}")
	public AdminNodeResponse get(@PathVariable UUID id) {
		return adminNodeService.get(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create node")
	public AdminNodeResponse create(@Valid @RequestBody AdminNodeRequest request) {
		return adminNodeService.create(request);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update node")
	public AdminNodeResponse update(@PathVariable UUID id, @Valid @RequestBody AdminNodeRequest request) {
		return adminNodeService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Deprecate node", description = "Sets status=DEPRECATED (soft delete)")
	public AdminNodeResponse deprecate(@PathVariable UUID id) {
		return adminNodeService.deprecate(id);
	}
}

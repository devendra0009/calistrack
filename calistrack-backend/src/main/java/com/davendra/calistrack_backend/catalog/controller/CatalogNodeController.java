package com.davendra.calistrack_backend.catalog.controller;

import com.davendra.calistrack_backend.catalog.dto.CatalogNodeResponse;
import com.davendra.calistrack_backend.catalog.service.CatalogNodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/nodes")
@Tag(name = "Catalog — Nodes", description = "Read active skill/goal nodes for goal selection and skill explorer")
public class CatalogNodeController {

	private final CatalogNodeService catalogNodeService;

	public CatalogNodeController(CatalogNodeService catalogNodeService) {
		this.catalogNodeService = catalogNodeService;
	}

	@GetMapping
	@Operation(
			summary = "List active nodes",
			description = "Returns ACTIVE rows from the node table (for goal pickers and skill explorer)"
	)
	public List<CatalogNodeResponse> list() {
		return catalogNodeService.listActive();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get active node by id")
	public CatalogNodeResponse get(@PathVariable UUID id) {
		return catalogNodeService.getActive(id);
	}
}

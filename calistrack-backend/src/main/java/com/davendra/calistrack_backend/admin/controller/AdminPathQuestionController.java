package com.davendra.calistrack_backend.admin.controller;

import com.davendra.calistrack_backend.admin.dto.AdminPathQuestionRequest;
import com.davendra.calistrack_backend.admin.dto.AdminPathQuestionResponse;
import com.davendra.calistrack_backend.admin.service.AdminPathQuestionService;
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
@RequestMapping("/api/v1/admin/path-questions")
@Tag(
		name = "Admin — Path questions",
		description = "Placement questionnaire per goal node (ADMIN role). Answers decide where to place the user on the goal path."
)
public class AdminPathQuestionController {

	private final AdminPathQuestionService adminPathQuestionService;

	public AdminPathQuestionController(AdminPathQuestionService adminPathQuestionService) {
		this.adminPathQuestionService = adminPathQuestionService;
	}

	@GetMapping
	@Operation(
			summary = "List path questions",
			description = "Includes goalNode/node {id, name}. Optional ?goalNodeId= filter (recommended)"
	)
	public List<AdminPathQuestionResponse> list(@RequestParam(required = false) UUID goalNodeId) {
		return adminPathQuestionService.list(goalNodeId);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get path question by id")
	public AdminPathQuestionResponse get(@PathVariable UUID id) {
		return adminPathQuestionService.get(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
			summary = "Create path question",
			description = "nodeId must be on the goal's node_edge path. sortOrder is unique per goal; one question per path node."
	)
	public AdminPathQuestionResponse create(@Valid @RequestBody AdminPathQuestionRequest request) {
		return adminPathQuestionService.create(request);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update path question", description = "goalNodeId cannot change")
	public AdminPathQuestionResponse update(
			@PathVariable UUID id,
			@Valid @RequestBody AdminPathQuestionRequest request
	) {
		return adminPathQuestionService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete path question")
	public void delete(@PathVariable UUID id) {
		adminPathQuestionService.delete(id);
	}
}

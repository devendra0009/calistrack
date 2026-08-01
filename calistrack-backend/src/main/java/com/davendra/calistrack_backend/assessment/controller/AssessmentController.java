package com.davendra.calistrack_backend.assessment.controller;

import com.davendra.calistrack_backend.assessment.dto.AssessmentResponse;
import com.davendra.calistrack_backend.assessment.dto.GoalPathAssessmentResponse;
import com.davendra.calistrack_backend.assessment.dto.SubmitAssessmentRequest;
import com.davendra.calistrack_backend.assessment.service.AssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assessments")
@Tag(name = "Assessments", description = "Path skill video verification")
public class AssessmentController {

	private final AssessmentService assessmentService;

	public AssessmentController(AssessmentService assessmentService) {
		this.assessmentService = assessmentService;
	}

	@GetMapping("/path")
	@Operation(
			summary = "Goal path for assessment",
			description = "Returns all prerequisite nodes for the user's current goal, with verified status"
	)
	public GoalPathAssessmentResponse getPath() {
		return assessmentService.getPathForCurrentUser();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
			summary = "Submit skill verification video",
			description = "Uploads proof for a path node and immediately marks user_node.verified=true (MVP self-verify)"
	)
	public AssessmentResponse submit(@Valid @RequestBody SubmitAssessmentRequest request) {
		return assessmentService.submitProof(request);
	}
}

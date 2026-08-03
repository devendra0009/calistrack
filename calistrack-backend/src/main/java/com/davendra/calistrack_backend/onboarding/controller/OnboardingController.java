package com.davendra.calistrack_backend.onboarding.controller;

import com.davendra.calistrack_backend.onboarding.dto.OnboardingAnswersRequest;
import com.davendra.calistrack_backend.onboarding.dto.OnboardingAnswersResponse;
import com.davendra.calistrack_backend.onboarding.dto.OnboardingNextQuestionResponse;
import com.davendra.calistrack_backend.onboarding.dto.OnboardingQuestionsResponse;
import com.davendra.calistrack_backend.onboarding.dto.OnboardingStatusResponse;
import com.davendra.calistrack_backend.onboarding.dto.OnboardingStepRequest;
import com.davendra.calistrack_backend.onboarding.dto.OnboardingStepResponse;
import com.davendra.calistrack_backend.onboarding.service.OnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/onboarding")
@Tag(name = "Onboarding", description = "Goal-path placement questions and answers")
public class OnboardingController {

	private final OnboardingService onboardingService;

	public OnboardingController(OnboardingService onboardingService) {
		this.onboardingService = onboardingService;
	}

	@GetMapping("/status")
	@Operation(
			summary = "Check questionnaire completion",
			description = "True when the user already has a workout_session (created by submitting answers)"
	)
	public OnboardingStatusResponse getStatus() {
		return onboardingService.getStatus();
	}

	@GetMapping("/questions")
	@Operation(
			summary = "Get all placement questions",
			description = "Returns the full DB-backed path question list for the goal (legacy batch UI)"
	)
	public OnboardingQuestionsResponse getQuestions(
			@RequestParam @Schema(defaultValue = "22222222-2222-2222-2222-222222220010") UUID goalNodeId
	) {
		return onboardingService.getQuestions(goalNodeId);
	}

	@GetMapping("/questions/next")
	@Operation(
			summary = "Get one placement question",
			description = "Returns a single question by index for sequential setup (primary UI path)"
	)
	public OnboardingNextQuestionResponse getNextQuestion(
			@RequestParam @Schema(defaultValue = "22222222-2222-2222-2222-222222220010") UUID goalNodeId,
			@RequestParam(defaultValue = "0") int index
	) {
		return onboardingService.getNextQuestion(goalNodeId, index);
	}

	@PostMapping("/step")
	@ResponseStatus(HttpStatus.OK)
	@Operation(
			summary = "Submit one placement answer step",
			description = "Evaluates the latest answer: NEXT returns the following question; "
					+ "PLACED places the user (first fail or all pass) and creates the PENDING session"
	)
	public OnboardingStepResponse submitStep(@Valid @RequestBody OnboardingStepRequest request) {
		return onboardingService.submitStep(request);
	}

	@PostMapping("/answers")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
			summary = "Submit placement answers (batch)",
			description = "Places user_nodes from answers and creates the first PENDING workout session. "
					+ "Accepts a full list or an ordered prefix ending on a fail."
	)
	public OnboardingAnswersResponse submitAnswers(@Valid @RequestBody OnboardingAnswersRequest request) {
		return onboardingService.submitAnswers(request);
	}
}

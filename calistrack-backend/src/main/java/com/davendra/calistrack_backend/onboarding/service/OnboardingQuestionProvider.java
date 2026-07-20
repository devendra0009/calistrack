package com.davendra.calistrack_backend.onboarding.service;

import com.davendra.calistrack_backend.onboarding.dto.OnboardingQuestionDto;

import java.util.List;
import java.util.UUID;

/**
 * Onboarding-facing questions for a goal. Backed by {@code GoalPathCatalog}.
 */
public interface OnboardingQuestionProvider {

	List<OnboardingQuestionDto> getQuestions(UUID goalNodeId);

	List<UUID> getPathNodeIds(UUID goalNodeId);
}

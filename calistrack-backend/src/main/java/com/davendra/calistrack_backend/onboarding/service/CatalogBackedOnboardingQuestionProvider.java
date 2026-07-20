package com.davendra.calistrack_backend.onboarding.service;

import com.davendra.calistrack_backend.onboarding.dto.OnboardingQuestionDto;
import com.davendra.calistrack_backend.onboarding.enums.QuestionType;
import com.davendra.calistrack_backend.path.catalog.GoalPathCatalog;
import com.davendra.calistrack_backend.path.dto.PathQuestion;
import com.davendra.calistrack_backend.path.enums.PlacementAnswerType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Adapts {@link GoalPathCatalog} to onboarding API DTOs.
 */
@Component
public class CatalogBackedOnboardingQuestionProvider implements OnboardingQuestionProvider {

	private final GoalPathCatalog goalPathCatalog;

	public CatalogBackedOnboardingQuestionProvider(GoalPathCatalog goalPathCatalog) {
		this.goalPathCatalog = goalPathCatalog;
	}

	@Override
	public List<OnboardingQuestionDto> getQuestions(UUID goalNodeId) {
		return goalPathCatalog.questionsFor(goalNodeId).stream()
				.map(this::toDto)
				.toList();
	}

	@Override
	public List<UUID> getPathNodeIds(UUID goalNodeId) {
		return goalPathCatalog.pathNodeIds(goalNodeId);
	}

	private OnboardingQuestionDto toDto(PathQuestion question) {
		return new OnboardingQuestionDto(
				question.nodeId(),
				question.prompt(),
				toQuestionType(question.type())
		);
	}

	private QuestionType toQuestionType(PlacementAnswerType type) {
		return switch (type) {
			case REPS -> QuestionType.REPS;
			case YES_NO -> QuestionType.YES_NO;
		};
	}
}

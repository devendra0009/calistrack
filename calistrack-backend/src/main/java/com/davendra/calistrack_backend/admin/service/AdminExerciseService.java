package com.davendra.calistrack_backend.admin.service;

import com.davendra.calistrack_backend.admin.dto.AdminExerciseRequest;
import com.davendra.calistrack_backend.admin.dto.AdminExerciseResponse;
import com.davendra.calistrack_backend.catalog.entity.Exercise;
import com.davendra.calistrack_backend.catalog.repo.ExerciseRepository;
import com.davendra.calistrack_backend.common.exception.ApiException;
import com.davendra.calistrack_backend.user.service.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class AdminExerciseService {

	private static final String STATUS_DEPRECATED = "DEPRECATED";

	private final CurrentUserService currentUserService;
	private final ExerciseRepository exerciseRepository;

	public AdminExerciseService(CurrentUserService currentUserService, ExerciseRepository exerciseRepository) {
		this.currentUserService = currentUserService;
		this.exerciseRepository = exerciseRepository;
	}

	@Transactional(readOnly = true)
	public List<AdminExerciseResponse> list(String status) {
		currentUserService.requireAdmin();
		List<Exercise> exercises = StringUtils.hasText(status)
				? exerciseRepository.findByStatusOrderByNameAsc(status.trim())
				: exerciseRepository.findAllByOrderByNameAsc();
		return exercises.stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public AdminExerciseResponse get(UUID id) {
		currentUserService.requireAdmin();
		return toResponse(requireExercise(id));
	}

	@Transactional
	public AdminExerciseResponse create(AdminExerciseRequest request) {
		currentUserService.requireAdmin();
		assertUniqueName(request.name(), null);

		Exercise exercise = new Exercise();
		apply(exercise, request);
		return toResponse(exerciseRepository.save(exercise));
	}

	@Transactional
	public AdminExerciseResponse update(UUID id, AdminExerciseRequest request) {
		currentUserService.requireAdmin();
		Exercise exercise = requireExercise(id);
		assertUniqueName(request.name(), id);
		apply(exercise, request);
		return toResponse(exerciseRepository.save(exercise));
	}

	@Transactional
	public AdminExerciseResponse deprecate(UUID id) {
		currentUserService.requireAdmin();
		Exercise exercise = requireExercise(id);
		exercise.setStatus(STATUS_DEPRECATED);
		return toResponse(exerciseRepository.save(exercise));
	}

	private void apply(Exercise exercise, AdminExerciseRequest request) {
		exercise.setName(request.name().trim());
		exercise.setDescription(request.description());
		exercise.setCategory(request.category().trim());
		exercise.setMetricType(request.metricType().trim());
		exercise.setDifficulty(request.difficulty().trim());
		exercise.setThumbnailUrl(request.thumbnailUrl());
		exercise.setDemoVideoUrl(request.demoVideoUrl());
		exercise.setStatus(StringUtils.hasText(request.status()) ? request.status().trim() : Exercise.STATUS_ACTIVE);
	}

	private void assertUniqueName(String name, UUID excludeId) {
		boolean taken = excludeId == null
				? exerciseRepository.existsByNameIgnoreCase(name.trim())
				: exerciseRepository.existsByNameIgnoreCaseAndIdNot(name.trim(), excludeId);
		if (taken) {
			throw new ApiException(HttpStatus.CONFLICT, "Exercise name already exists: " + name);
		}
	}

	private Exercise requireExercise(UUID id) {
		return exerciseRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Exercise not found: " + id));
	}

	private AdminExerciseResponse toResponse(Exercise exercise) {
		return new AdminExerciseResponse(
				exercise.getId(),
				exercise.getName(),
				exercise.getDescription(),
				exercise.getCategory(),
				exercise.getMetricType(),
				exercise.getDifficulty(),
				exercise.getThumbnailUrl(),
				exercise.getDemoVideoUrl(),
				exercise.getStatus(),
				exercise.getCreatedAt(),
				exercise.getUpdatedAt()
		);
	}
}

package com.davendra.calistrack_backend.admin.service;

import com.davendra.calistrack_backend.admin.dto.AdminWorkoutExerciseRequest;
import com.davendra.calistrack_backend.admin.dto.AdminWorkoutExerciseResponse;
import com.davendra.calistrack_backend.admin.dto.AdminWorkoutRequest;
import com.davendra.calistrack_backend.admin.dto.AdminWorkoutResponse;
import com.davendra.calistrack_backend.admin.dto.AdminWorkoutSummaryResponse;
import com.davendra.calistrack_backend.admin.dto.NamedRef;
import com.davendra.calistrack_backend.catalog.entity.Exercise;
import com.davendra.calistrack_backend.catalog.entity.Node;
import com.davendra.calistrack_backend.catalog.entity.Workout;
import com.davendra.calistrack_backend.catalog.entity.WorkoutExercise;
import com.davendra.calistrack_backend.catalog.repo.ExerciseRepository;
import com.davendra.calistrack_backend.catalog.repo.NodeRepository;
import com.davendra.calistrack_backend.catalog.repo.WorkoutExerciseRepository;
import com.davendra.calistrack_backend.catalog.repo.WorkoutRepository;
import com.davendra.calistrack_backend.common.exception.ApiException;
import com.davendra.calistrack_backend.user.entity.AppUser;
import com.davendra.calistrack_backend.user.service.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AdminWorkoutService {

	private static final String STATUS_DEPRECATED = "DEPRECATED";

	private final CurrentUserService currentUserService;
	private final WorkoutRepository workoutRepository;
	private final WorkoutExerciseRepository workoutExerciseRepository;
	private final NodeRepository nodeRepository;
	private final ExerciseRepository exerciseRepository;

	public AdminWorkoutService(
			CurrentUserService currentUserService,
			WorkoutRepository workoutRepository,
			WorkoutExerciseRepository workoutExerciseRepository,
			NodeRepository nodeRepository,
			ExerciseRepository exerciseRepository
	) {
		this.currentUserService = currentUserService;
		this.workoutRepository = workoutRepository;
		this.workoutExerciseRepository = workoutExerciseRepository;
		this.nodeRepository = nodeRepository;
		this.exerciseRepository = exerciseRepository;
	}

	@Transactional(readOnly = true)
	public List<AdminWorkoutSummaryResponse> list(String status, UUID goalNodeId) {
		currentUserService.requireAdmin();
		List<Workout> workouts;
		if (goalNodeId != null) {
			workouts = workoutRepository.findByGoalNode_IdOrderByTitleAsc(goalNodeId);
			if (StringUtils.hasText(status)) {
				String s = status.trim();
				workouts = workouts.stream().filter(w -> s.equals(w.getStatus())).toList();
			}
		} else if (StringUtils.hasText(status)) {
			workouts = workoutRepository.findByStatusOrderByTitleAsc(status.trim());
		} else {
			workouts = workoutRepository.findAllByOrderByTitleAsc();
		}
		return workouts.stream().map(this::toSummary).toList();
	}

	@Transactional(readOnly = true)
	public AdminWorkoutResponse get(UUID id) {
		currentUserService.requireAdmin();
		Workout workout = requireWorkout(id);
		List<WorkoutExercise> lines = workoutExerciseRepository.findByWorkout_IdOrderBySequenceAsc(id);
		return toDetail(workout, lines);
	}

	@Transactional
	public AdminWorkoutResponse create(AdminWorkoutRequest request) {
		AppUser admin = currentUserService.requireAdmin();
		Node goalNode = requireNode(request.goalNodeId());

		Workout workout = new Workout();
		workout.setTitle(request.title().trim());
		workout.setDescription(request.description());
		workout.setGoalNode(goalNode);
		workout.setDifficulty(request.difficulty().trim());
		workout.setStatus(StringUtils.hasText(request.status()) ? request.status().trim() : Workout.STATUS_ACTIVE);
		workout.setCreatedByUserId(admin.getId());
		Workout saved = workoutRepository.save(workout);

		List<WorkoutExercise> lines = replaceExercises(saved, request.exercises());
		return toDetail(saved, lines);
	}

	@Transactional
	public AdminWorkoutResponse update(UUID id, AdminWorkoutRequest request) {
		currentUserService.requireAdmin();
		Workout workout = requireWorkout(id);
		Node goalNode = requireNode(request.goalNodeId());

		workout.setTitle(request.title().trim());
		workout.setDescription(request.description());
		workout.setGoalNode(goalNode);
		workout.setDifficulty(request.difficulty().trim());
		workout.setStatus(StringUtils.hasText(request.status()) ? request.status().trim() : workout.getStatus());
		Workout saved = workoutRepository.save(workout);

		List<WorkoutExercise> lines;
		if (request.exercises() != null) {
			lines = replaceExercises(saved, request.exercises());
		} else {
			lines = workoutExerciseRepository.findByWorkout_IdOrderBySequenceAsc(saved.getId());
		}
		return toDetail(saved, lines);
	}

	@Transactional
	public AdminWorkoutResponse deprecate(UUID id) {
		currentUserService.requireAdmin();
		Workout workout = requireWorkout(id);
		workout.setStatus(STATUS_DEPRECATED);
		Workout saved = workoutRepository.save(workout);
		List<WorkoutExercise> lines = workoutExerciseRepository.findByWorkout_IdOrderBySequenceAsc(id);
		return toDetail(saved, lines);
	}

	@Transactional
	public AdminWorkoutExerciseResponse addExercise(UUID workoutId, AdminWorkoutExerciseRequest request) {
		currentUserService.requireAdmin();
		Workout workout = requireWorkout(workoutId);
		assertSequenceFree(workoutId, request.sequence(), null);
		Exercise exercise = requireExercise(request.exerciseId());

		WorkoutExercise line = new WorkoutExercise();
		line.setWorkout(workout);
		applyLine(line, request, exercise);
		return toExerciseResponse(workoutExerciseRepository.save(line));
	}

	@Transactional
	public AdminWorkoutExerciseResponse updateExercise(
			UUID workoutId,
			UUID workoutExerciseId,
			AdminWorkoutExerciseRequest request
	) {
		currentUserService.requireAdmin();
		WorkoutExercise line = requireWorkoutExercise(workoutId, workoutExerciseId);
		assertSequenceFree(workoutId, request.sequence(), workoutExerciseId);
		Exercise exercise = requireExercise(request.exerciseId());
		applyLine(line, request, exercise);
		return toExerciseResponse(workoutExerciseRepository.save(line));
	}

	@Transactional
	public void deleteExercise(UUID workoutId, UUID workoutExerciseId) {
		currentUserService.requireAdmin();
		WorkoutExercise line = requireWorkoutExercise(workoutId, workoutExerciseId);
		workoutExerciseRepository.delete(line);
	}

	private List<WorkoutExercise> replaceExercises(Workout workout, List<AdminWorkoutExerciseRequest> requests) {
		List<WorkoutExercise> existing = workoutExerciseRepository.findByWorkout_IdOrderBySequenceAsc(workout.getId());
		workoutExerciseRepository.deleteAll(existing);
		workoutExerciseRepository.flush();

		if (requests == null || requests.isEmpty()) {
			return List.of();
		}
		assertUniqueSequences(requests);

		return requests.stream().map(req -> {
			Exercise exercise = requireExercise(req.exerciseId());
			WorkoutExercise line = new WorkoutExercise();
			line.setWorkout(workout);
			applyLine(line, req, exercise);
			return workoutExerciseRepository.save(line);
		}).toList();
	}

	private void applyLine(WorkoutExercise line, AdminWorkoutExerciseRequest request, Exercise exercise) {
		line.setExercise(exercise);
		line.setSequence(request.sequence());
		line.setTargetSets(request.targetSets());
		line.setTargetReps(request.targetReps());
		line.setTargetHoldSeconds(request.targetHoldSeconds());
		line.setTargetRestSeconds(request.targetRestSeconds());
		line.setNotes(request.notes());
		line.setDemoVideoUrl(request.demoVideoUrl());
	}

	private void assertUniqueSequences(List<AdminWorkoutExerciseRequest> requests) {
		Set<Integer> seen = new HashSet<>();
		for (AdminWorkoutExerciseRequest req : requests) {
			if (!seen.add(req.sequence())) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "Duplicate sequence in exercises: " + req.sequence());
			}
		}
	}

	private void assertSequenceFree(UUID workoutId, int sequence, UUID excludeId) {
		List<WorkoutExercise> lines = workoutExerciseRepository.findByWorkout_IdOrderBySequenceAsc(workoutId);
		boolean taken = lines.stream()
				.anyMatch(l -> l.getSequence() == sequence && (excludeId == null || !l.getId().equals(excludeId)));
		if (taken) {
			throw new ApiException(HttpStatus.CONFLICT, "Sequence already used in this workout: " + sequence);
		}
	}

	private Workout requireWorkout(UUID id) {
		return workoutRepository.findByIdWithGoalNode(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Workout not found: " + id));
	}

	private WorkoutExercise requireWorkoutExercise(UUID workoutId, UUID workoutExerciseId) {
		return workoutExerciseRepository.findByIdAndWorkout_Id(workoutExerciseId, workoutId)
				.orElseThrow(() -> new ApiException(
						HttpStatus.NOT_FOUND,
						"Workout exercise not found: " + workoutExerciseId
				));
	}

	private Node requireNode(UUID id) {
		return nodeRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Node not found: " + id));
	}

	private Exercise requireExercise(UUID id) {
		return exerciseRepository.findById(id)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Exercise not found: " + id));
	}

	private AdminWorkoutSummaryResponse toSummary(Workout workout) {
		long count = workoutExerciseRepository.countByWorkout_Id(workout.getId());
		Node goal = workout.getGoalNode();
		return new AdminWorkoutSummaryResponse(
				workout.getId(),
				workout.getTitle(),
				workout.getDescription(),
				new NamedRef(goal.getId(), goal.getName()),
				workout.getDifficulty(),
				workout.getCreatedByUserId(),
				workout.getStatus(),
				(int) count,
				workout.getCreatedAt(),
				workout.getUpdatedAt()
		);
	}

	private AdminWorkoutResponse toDetail(Workout workout, List<WorkoutExercise> lines) {
		Node goal = workout.getGoalNode();
		return new AdminWorkoutResponse(
				workout.getId(),
				workout.getTitle(),
				workout.getDescription(),
				new NamedRef(goal.getId(), goal.getName()),
				workout.getDifficulty(),
				workout.getCreatedByUserId(),
				workout.getStatus(),
				workout.getCreatedAt(),
				workout.getUpdatedAt(),
				lines.stream().map(this::toExerciseResponse).toList()
		);
	}

	private AdminWorkoutExerciseResponse toExerciseResponse(WorkoutExercise line) {
		Exercise exercise = line.getExercise();
		return new AdminWorkoutExerciseResponse(
				line.getId(),
				new NamedRef(exercise.getId(), exercise.getName()),
				line.getSequence(),
				line.getTargetSets(),
				line.getTargetReps(),
				line.getTargetHoldSeconds(),
				line.getTargetRestSeconds(),
				line.getNotes(),
				line.getDemoVideoUrl()
		);
	}
}

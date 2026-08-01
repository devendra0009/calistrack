package com.davendra.calistrack_backend.workout.repo;

import com.davendra.calistrack_backend.workout.entity.ExerciseAttempt;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExerciseAttemptRepository extends JpaRepository<ExerciseAttempt, UUID> {

	@EntityGraph(attributePaths = {
			"workoutExercise",
			"workoutExercise.exercise",
			"workoutSession",
			"workoutSession.user"
	})
	Optional<ExerciseAttempt> findDetailedById(UUID id);

	@EntityGraph(attributePaths = {"workoutExercise", "workoutExercise.exercise"})
	List<ExerciseAttempt> findByWorkoutSession_Id(UUID workoutSessionId);

	@EntityGraph(attributePaths = {
			"workoutExercise",
			"workoutSession"
	})
	Optional<ExerciseAttempt> findByWorkoutSession_IdAndWorkoutExercise_Id(
			UUID workoutSessionId,
			UUID workoutExerciseId
	);

	boolean existsByWorkoutExercise_Id(UUID workoutExerciseId);
}

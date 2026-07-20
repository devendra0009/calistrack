package com.davendra.calistrack_backend.catalog.repo;

import com.davendra.calistrack_backend.catalog.entity.WorkoutExercise;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, UUID> {

	@EntityGraph(attributePaths = "exercise")
	List<WorkoutExercise> findByWorkout_IdOrderBySequenceAsc(UUID workoutId);

	@EntityGraph(attributePaths = {"exercise", "workout"})
	Optional<WorkoutExercise> findByIdAndWorkout_Id(UUID id, UUID workoutId);

	long countByWorkout_Id(UUID workoutId);
}

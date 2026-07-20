package com.davendra.calistrack_backend.catalog.repo;

import com.davendra.calistrack_backend.catalog.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {

	List<Exercise> findAllByOrderByNameAsc();

	List<Exercise> findByStatusOrderByNameAsc(String status);

	Optional<Exercise> findByIdAndStatus(UUID id, String status);

	boolean existsByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}

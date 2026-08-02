package com.davendra.calistrack_backend.assessment.repo;

import com.davendra.calistrack_backend.assessment.entity.Assessment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {

	@EntityGraph(attributePaths = {"node"})
	List<Assessment> findByUser_IdAndNode_IdInOrderByCreatedAtDesc(
			UUID userId,
			Collection<UUID> nodeIds
	);
}

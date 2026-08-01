package com.davendra.calistrack_backend.progress.repo;

import com.davendra.calistrack_backend.progress.entity.UserPlanEnrollment;
import com.davendra.calistrack_backend.progress.enums.UserPlanEnrollmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserPlanEnrollmentRepository extends JpaRepository<UserPlanEnrollment, UUID> {

	@EntityGraph(attributePaths = {"plan", "plan.node", "node"})
	Optional<UserPlanEnrollment> findById(UUID id);

	@EntityGraph(attributePaths = {"plan", "plan.node", "node"})
	Optional<UserPlanEnrollment> findFirstByUser_IdAndStatusInOrderByUpdatedAtDesc(
			UUID userId,
			Collection<UserPlanEnrollmentStatus> statuses
	);

	@EntityGraph(attributePaths = {"plan", "plan.node", "node"})
	Optional<UserPlanEnrollment> findFirstByUser_IdAndNode_IdAndStatusInOrderByCreatedAtDesc(
			UUID userId,
			UUID nodeId,
			Collection<UserPlanEnrollmentStatus> statuses
	);

	@EntityGraph(attributePaths = {"plan", "plan.node", "node"})
	List<UserPlanEnrollment> findByUser_IdOrderByCreatedAtDesc(UUID userId);

	boolean existsByUser_IdAndNode_IdAndStatusIn(
			UUID userId,
			UUID nodeId,
			Collection<UserPlanEnrollmentStatus> statuses
	);
}

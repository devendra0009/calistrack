package com.davendra.calistrack_backend.progress.repo;

import com.davendra.calistrack_backend.progress.entity.UserNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserNodeRepository extends JpaRepository<UserNode, UUID> {

	boolean existsByUserId(UUID userId);

	Optional<UserNode> findByUser_IdAndNode_Id(UUID userId, UUID nodeId);
}

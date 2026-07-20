package com.davendra.calistrack_backend.catalog.repo;

import com.davendra.calistrack_backend.catalog.entity.Node;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NodeRepository extends JpaRepository<Node, UUID> {

	Optional<Node> findByIdAndStatus(UUID id, String status);

	List<Node> findAllByOrderByNameAsc();

	List<Node> findByStatusOrderByNameAsc(String status);

	boolean existsByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}

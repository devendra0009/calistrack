package com.davendra.calistrack_backend.catalog.repo;

import com.davendra.calistrack_backend.catalog.entity.NodeEdge;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NodeEdgeRepository extends JpaRepository<NodeEdge, UUID> {

	@EntityGraph(attributePaths = {"fromNode", "toNode"})
	List<NodeEdge> findByToNode_IdIn(Collection<UUID> toNodeIds);

	@EntityGraph(attributePaths = {"fromNode", "toNode"})
	List<NodeEdge> findByFromNode_IdInOrToNode_IdIn(Collection<UUID> fromNodeIds, Collection<UUID> toNodeIds);

	@EntityGraph(attributePaths = {"fromNode", "toNode"})
	List<NodeEdge> findAllByOrderByCreatedAtAsc();

	@EntityGraph(attributePaths = {"fromNode", "toNode"})
	@Query("select e from NodeEdge e where e.id = :id")
	Optional<NodeEdge> findDetailedById(@Param("id") UUID id);

	boolean existsByFromNode_IdAndToNode_Id(UUID fromNodeId, UUID toNodeId);
}

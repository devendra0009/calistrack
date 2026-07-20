package com.davendra.calistrack_backend.path.repo;

import com.davendra.calistrack_backend.path.entity.PathQuestionEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PathQuestionRepository extends JpaRepository<PathQuestionEntity, UUID> {

	@EntityGraph(attributePaths = {"goalNode", "node"})
	List<PathQuestionEntity> findByGoalNode_IdOrderBySortOrderAsc(UUID goalNodeId);

	@EntityGraph(attributePaths = {"goalNode", "node"})
	List<PathQuestionEntity> findAllByOrderBySortOrderAsc();

	@EntityGraph(attributePaths = {"goalNode", "node"})
	@Query("select q from PathQuestionEntity q where q.id = :id")
	Optional<PathQuestionEntity> findDetailedById(@Param("id") UUID id);

	boolean existsByGoalNode_Id(UUID goalNodeId);

	boolean existsByGoalNode_IdAndSortOrder(UUID goalNodeId, int sortOrder);

	boolean existsByGoalNode_IdAndSortOrderAndIdNot(UUID goalNodeId, int sortOrder, UUID id);

	boolean existsByGoalNode_IdAndNode_Id(UUID goalNodeId, UUID nodeId);

	boolean existsByGoalNode_IdAndNode_IdAndIdNot(UUID goalNodeId, UUID nodeId, UUID id);
}

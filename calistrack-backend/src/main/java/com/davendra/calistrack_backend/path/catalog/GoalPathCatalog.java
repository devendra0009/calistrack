package com.davendra.calistrack_backend.path.catalog;

import com.davendra.calistrack_backend.path.dto.PathQuestion;

import java.util.List;
import java.util.UUID;

/**
 * Catalog of goal paths and placement questions.
 * Swap static MVP impl for graph-based generation later.
 */
public interface GoalPathCatalog {

	List<PathQuestion> questionsFor(UUID goalNodeId);

	/**
	 * Ordered prerequisite path ending at the goal node.
	 */
	List<UUID> pathNodeIds(UUID goalNodeId);
}

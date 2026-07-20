package com.davendra.calistrack_backend.path.catalog;

import com.davendra.calistrack_backend.catalog.entity.NodeEdge;
import com.davendra.calistrack_backend.catalog.repo.NodeEdgeRepository;
import com.davendra.calistrack_backend.common.exception.ApiException;
import com.davendra.calistrack_backend.path.dto.PathQuestion;
import com.davendra.calistrack_backend.path.entity.PathQuestionEntity;
import com.davendra.calistrack_backend.path.repo.PathQuestionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

/**
 * Questions from {@code path_question}; path order derived by walking {@code node_edge}
 * backward from the goal, then topologically sorting the ancestor subgraph.
 */
@Component
public class DbGoalPathCatalog implements GoalPathCatalog {

	private final PathQuestionRepository pathQuestionRepository;
	private final NodeEdgeRepository nodeEdgeRepository;

	public DbGoalPathCatalog(
			PathQuestionRepository pathQuestionRepository,
			NodeEdgeRepository nodeEdgeRepository
	) {
		this.pathQuestionRepository = pathQuestionRepository;
		this.nodeEdgeRepository = nodeEdgeRepository;
	}

	@Override
	public List<PathQuestion> questionsFor(UUID goalNodeId) {
		List<PathQuestionEntity> rows = pathQuestionRepository.findByGoalNode_IdOrderBySortOrderAsc(goalNodeId);
		if (rows.isEmpty()) {
			throw new ApiException(
					HttpStatus.NOT_FOUND,
					"No path questions configured for goal node: " + goalNodeId
			);
		}
		return rows.stream()
				.map(q -> new PathQuestion(q.getNode().getId(), q.getPrompt(), q.getAnswerType()))
				.toList();
	}

	@Override
	public List<UUID> pathNodeIds(UUID goalNodeId) {
		Set<UUID> ancestors = collectAncestorsInclusive(goalNodeId);
		if (ancestors.size() == 1) {
			// Goal with no incoming edges — alone is still a valid (trivial) path.
			return List.of(goalNodeId);
		}

		List<NodeEdge> subgraphEdges = nodeEdgeRepository
				.findByFromNode_IdInOrToNode_IdIn(ancestors, ancestors)
				.stream()
				.filter(e -> ancestors.contains(e.getFromNode().getId())
						&& ancestors.contains(e.getToNode().getId()))
				.toList();

		return topologicalOrder(ancestors, subgraphEdges, goalNodeId);
	}

	/**
	 * Walk {@code node_edge} backward: for each node, find edges where it is {@code to_node},
	 * then continue from those {@code from_node} prerequisites.
	 */
	private Set<UUID> collectAncestorsInclusive(UUID goalNodeId) {
		Set<UUID> visited = new HashSet<>();
		Queue<UUID> queue = new ArrayDeque<>();
		queue.add(goalNodeId);
		visited.add(goalNodeId);

		while (!queue.isEmpty()) {
			Set<UUID> batch = new HashSet<>();
			while (!queue.isEmpty()) {
				batch.add(queue.poll());
			}
			for (NodeEdge edge : nodeEdgeRepository.findByToNode_IdIn(batch)) {
				UUID prereq = edge.getFromNode().getId();
				if (visited.add(prereq)) {
					queue.add(prereq);
				}
			}
		}
		return visited;
	}

	/**
	 * Kahn topological sort so prerequisites appear before dependents. Goal is always last
	 * among equals when multiple roots exist (e.g. 15 Dips branch into Muscle Up).
	 */
	private List<UUID> topologicalOrder(Set<UUID> nodes, List<NodeEdge> edges, UUID goalNodeId) {
		Map<UUID, Integer> indegree = new HashMap<>();
		Map<UUID, List<UUID>> outgoing = new HashMap<>();
		for (UUID id : nodes) {
			indegree.put(id, 0);
			outgoing.put(id, new ArrayList<>());
		}
		for (NodeEdge edge : edges) {
			UUID from = edge.getFromNode().getId();
			UUID to = edge.getToNode().getId();
			outgoing.get(from).add(to);
			indegree.merge(to, 1, Integer::sum);
		}

		Queue<UUID> ready = new ArrayDeque<>();
		for (UUID id : nodes) {
			if (indegree.get(id) == 0) {
				ready.add(id);
			}
		}

		List<UUID> ordered = new ArrayList<>(nodes.size());
		while (!ready.isEmpty()) {
			UUID current = ready.poll();
			ordered.add(current);
			for (UUID next : outgoing.get(current)) {
				int remaining = indegree.merge(next, -1, Integer::sum);
				if (remaining == 0) {
					ready.add(next);
				}
			}
		}

		if (ordered.size() != nodes.size()) {
			throw new ApiException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					"Goal path graph for " + goalNodeId + " has a cycle"
			);
		}
		if (!ordered.get(ordered.size() - 1).equals(goalNodeId)) {
			// Ensure goal is last for placement semantics (COMPLETED before focus / goal).
			ordered.remove(goalNodeId);
			ordered.add(goalNodeId);
		}
		return List.copyOf(ordered);
	}
}

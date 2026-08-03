package com.davendra.calistrack_backend.path.service;

import com.davendra.calistrack_backend.path.dto.PlacementAnswer;
import com.davendra.calistrack_backend.path.dto.PlacementResult;

import java.util.List;
import java.util.UUID;

/**
 * Pure placement: goal + answers → focus node and COMPLETED / AVAILABLE / LOCKED statuses.
 * Answers may be an ordered prefix of the goal's questions; placement is allowed when the
 * prefix ends on a fail, or covers every question (all pass → focus = goal).
 */
public interface PathPlacementEngine {

	PlacementResult place(UUID goalNodeId, List<PlacementAnswer> answers);

	/**
	 * Whether the given answer meets the node's pass criteria (YES_NO true / REPS >= target).
	 */
	boolean isAnswerPassed(PlacementAnswer answer);
}

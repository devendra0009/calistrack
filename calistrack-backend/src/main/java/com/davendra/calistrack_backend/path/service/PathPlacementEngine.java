package com.davendra.calistrack_backend.path.service;

import com.davendra.calistrack_backend.path.dto.PlacementAnswer;
import com.davendra.calistrack_backend.path.dto.PlacementResult;

import java.util.List;
import java.util.UUID;

/**
 * Pure placement: goal + answers → focus node and COMPLETED / AVAILABLE / LOCKED statuses.
 */
public interface PathPlacementEngine {

	PlacementResult place(UUID goalNodeId, List<PlacementAnswer> answers);
}

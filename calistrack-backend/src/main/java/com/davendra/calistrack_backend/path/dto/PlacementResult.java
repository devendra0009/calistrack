package com.davendra.calistrack_backend.path.dto;

import java.util.List;
import java.util.UUID;

/**
 * Result of placing a user on a goal path from answers (or derived progress).
 */
public record PlacementResult(
		UUID goalNodeId,
		UUID focusNodeId,
		List<NodePlacement> placements
) {
}

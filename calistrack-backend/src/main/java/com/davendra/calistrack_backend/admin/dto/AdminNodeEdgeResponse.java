package com.davendra.calistrack_backend.admin.dto;

import java.time.Instant;
import java.util.UUID;

public record AdminNodeEdgeResponse(
		UUID id,
		NamedRef fromNode,
		NamedRef toNode,
		String relationType,
		Instant createdAt
) {
}

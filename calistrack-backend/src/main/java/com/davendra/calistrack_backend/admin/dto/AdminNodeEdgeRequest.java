package com.davendra.calistrack_backend.admin.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AdminNodeEdgeRequest(
		@NotNull UUID fromNodeId,
		@NotNull UUID toNodeId,
		@Size(max = 20) String relationType
) {
}

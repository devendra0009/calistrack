package com.davendra.calistrack_backend.path.dto;

import com.davendra.calistrack_backend.progress.enums.UserNodeStatus;

import java.util.UUID;

public record NodePlacement(
		UUID nodeId,
		UserNodeStatus status
) {
}

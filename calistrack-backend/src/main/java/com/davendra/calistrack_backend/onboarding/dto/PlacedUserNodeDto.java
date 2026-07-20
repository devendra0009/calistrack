package com.davendra.calistrack_backend.onboarding.dto;

import com.davendra.calistrack_backend.progress.enums.UserNodeStatus;

import java.util.UUID;

public record PlacedUserNodeDto(
		UUID nodeId,
		UserNodeStatus status
) {
}

package com.davendra.calistrack_backend.catalog.dto;

import java.util.UUID;

public record CatalogNodeResponse(
		UUID id,
		String name,
		String description,
		String nodeType,
		String difficulty
) {
}

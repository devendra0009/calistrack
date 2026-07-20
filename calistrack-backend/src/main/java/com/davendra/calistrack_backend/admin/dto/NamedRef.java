package com.davendra.calistrack_backend.admin.dto;

import java.util.UUID;

/** Lightweight id + display name used across admin catalog responses. */
public record NamedRef(
		UUID id,
		String name
) {
}

package com.davendra.calistrack_backend.common.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Lightweight liveness probe used by free-tier keep-alive pings and load balancers.
 * Intentionally avoids DB / external checks so cold starts stay cheap.
 */
@RestController
public class HealthController {

	@GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, String> health() {
		return Map.of("status", "UP");
	}
}

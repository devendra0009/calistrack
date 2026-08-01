package com.davendra.calistrack_backend.chat.exception;

import org.springframework.http.HttpStatus;

/**
 * Domain failure from the AI coach provider (timeouts, rate limits, empty replies, etc.).
 */
public class AiCoachException extends RuntimeException {

	private final HttpStatus status;

	public AiCoachException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	public AiCoachException(HttpStatus status, String message, Throwable cause) {
		super(message, cause);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}

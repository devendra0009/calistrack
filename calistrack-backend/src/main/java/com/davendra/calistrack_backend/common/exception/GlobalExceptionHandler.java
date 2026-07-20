package com.davendra.calistrack_backend.common.exception;

import com.davendra.calistrack_backend.auth.exception.AuthException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

/**
 * RFC 7807 Problem Details ({@code application/problem+json}).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AuthException.class)
	public ProblemDetail handleAuth(AuthException ex) {
		return problem(HttpStatus.UNAUTHORIZED, ex.getMessage(), "authentication-failed");
	}

	@ExceptionHandler(ApiException.class)
	public ProblemDetail handleApi(ApiException ex) {
		return problem(ex.getStatus(), ex.getMessage(), "api-error");
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(err -> err.getField() + ": " + err.getDefaultMessage())
				.orElse("Validation failed");
		ProblemDetail detail = problem(HttpStatus.BAD_REQUEST, message, "validation-error");
		detail.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
				.map(err -> err.getField() + ": " + err.getDefaultMessage())
				.toList());
		return detail;
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ProblemDetail handleUnreadable(HttpMessageNotReadableException ex) {
		return problem(HttpStatus.BAD_REQUEST, "Malformed request body or invalid enum value", "malformed-request");
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleGeneric(Exception ex) {
		return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", "internal-error");
	}

	private static ProblemDetail problem(HttpStatus status, String detail, String typeSlug) {
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
		pd.setTitle(status.getReasonPhrase());
		pd.setType(URI.create("https://calistrack.app/problems/" + typeSlug));
		pd.setProperty("timestamp", Instant.now().toString());
		return pd;
	}
}

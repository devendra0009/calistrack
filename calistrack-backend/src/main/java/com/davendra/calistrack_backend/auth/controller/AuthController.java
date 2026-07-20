package com.davendra.calistrack_backend.auth.controller;

import com.davendra.calistrack_backend.auth.dto.AuthResponse;
import com.davendra.calistrack_backend.auth.dto.LoginRequest;
import com.davendra.calistrack_backend.auth.dto.LogoutRequest;
import com.davendra.calistrack_backend.auth.dto.RefreshRequest;
import com.davendra.calistrack_backend.auth.dto.RegisterRequest;
import com.davendra.calistrack_backend.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Register, login, refresh, and logout (public)")
@SecurityRequirements // no bearer required for these endpoints
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Register", description = "Create account via Firebase and return idToken + refreshToken")
	public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
		return authService.register(request);
	}

	@PostMapping("/login")
	@Operation(summary = "Login", description = "Sign in and return idToken + refreshToken")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@PostMapping("/refresh")
	@Operation(summary = "Refresh", description = "Rotate refresh token and return a new idToken")
	public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
		return authService.refresh(request);
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Logout", description = "Revoke the given refresh token")
	public void logout(@Valid @RequestBody LogoutRequest request) {
		authService.logout(request);
	}
}

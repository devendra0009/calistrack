package com.davendra.calistrack_backend.user.controller;

import com.davendra.calistrack_backend.user.dto.MeResponse;
import com.davendra.calistrack_backend.user.dto.PatchMeRequest;
import com.davendra.calistrack_backend.user.dto.PutGoalRequest;
import com.davendra.calistrack_backend.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "Profile", description = "Current user profile, goal, and account lifecycle")
public class MeController {

	private final UserProfileService userProfileService;

	public MeController(UserProfileService userProfileService) {
		this.userProfileService = userProfileService;
	}

	@GetMapping
	@Operation(summary = "Get profile", description = "Returns the authenticated user's profile and current goal node")
	public MeResponse getMe() {
		return userProfileService.getProfile();
	}

	@PatchMapping
	@Operation(summary = "Update profile", description = "Partial update of display name and onboarding body stats")
	public MeResponse patchMe(@Valid @RequestBody PatchMeRequest request) {
		return userProfileService.updateProfile(request);
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete account", description = "Soft-deletes the account, revokes sessions, and disables Firebase Auth")
	public void deleteMe() {
		userProfileService.softDeleteAccount();
	}

	@PutMapping("/goal")
	@Operation(summary = "Set goal", description = "Pick or change the current goal skill node")
	public MeResponse putGoal(@Valid @RequestBody PutGoalRequest request) {
		return userProfileService.updateGoal(request);
	}
}

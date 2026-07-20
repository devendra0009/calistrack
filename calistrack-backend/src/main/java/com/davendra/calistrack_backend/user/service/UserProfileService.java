package com.davendra.calistrack_backend.user.service;

import com.davendra.calistrack_backend.auth.repo.RefreshTokenRepository;
import com.davendra.calistrack_backend.catalog.entity.Node;
import com.davendra.calistrack_backend.catalog.repo.NodeRepository;
import com.davendra.calistrack_backend.common.exception.ApiException;
import com.davendra.calistrack_backend.user.dto.MeResponse;
import com.davendra.calistrack_backend.user.dto.PatchMeRequest;
import com.davendra.calistrack_backend.user.dto.PutGoalRequest;
import com.davendra.calistrack_backend.user.entity.AppUser;
import com.davendra.calistrack_backend.user.mapper.UserProfileMapper;
import com.davendra.calistrack_backend.user.repo.AppUserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class UserProfileService {

	private static final Logger log = LoggerFactory.getLogger(UserProfileService.class);

	private final CurrentUserService currentUserService;
	private final AppUserRepository appUserRepository;
	private final NodeRepository nodeRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final UserProfileMapper userProfileMapper;

	public UserProfileService(
			CurrentUserService currentUserService,
			AppUserRepository appUserRepository,
			NodeRepository nodeRepository,
			RefreshTokenRepository refreshTokenRepository,
			UserProfileMapper userProfileMapper
	) {
		this.currentUserService = currentUserService;
		this.appUserRepository = appUserRepository;
		this.nodeRepository = nodeRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.userProfileMapper = userProfileMapper;
	}

	@Transactional(readOnly = true)
	public MeResponse getProfile() {
		return userProfileMapper.toMeResponse(currentUserService.requireActiveUserWithGoal());
	}

	@Transactional
	public MeResponse updateProfile(PatchMeRequest request) {
		validatePatchHasAtLeastOneField(request);

		AppUser user = currentUserService.requireActiveUserWithGoal();
		applyPatch(user, request);
		return userProfileMapper.toMeResponse(appUserRepository.save(user));
	}

	@Transactional
	public MeResponse updateGoal(PutGoalRequest request) {
		AppUser user = currentUserService.requireActiveUserWithGoal();

		Node goalNode = nodeRepository
				.findByIdAndStatus(request.goalNodeId(), Node.STATUS_ACTIVE)
				.orElseThrow(() -> new ApiException(
						HttpStatus.NOT_FOUND,
						"Active goal node not found: " + request.goalNodeId()
				));

		user.setCurrentGoalNode(goalNode);
		return userProfileMapper.toMeResponse(appUserRepository.save(user));
	}

	@Transactional
	public void softDeleteAccount() {
		String firebaseUid = currentUserService.requireFirebaseUid();
		AppUser user = currentUserService.requireActiveUser();

		user.softDelete();
		appUserRepository.save(user);
		refreshTokenRepository.revokeAllActiveForUser(user, Instant.now());

		// Runs before commit; failure rolls back the soft-delete + revoke.
		disableFirebaseAccount(firebaseUid);
	}

	private void applyPatch(AppUser user, PatchMeRequest request) {
		if (request.displayName() != null) {
			String name = request.displayName().trim();
			if (name.isEmpty()) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "displayName: must not be blank");
			}
			user.setDisplayName(name);
		}
		if (request.heightCm() != null) {
			user.setHeightCm(request.heightCm());
		}
		if (request.weightKg() != null) {
			user.setWeightKg(request.weightKg());
		}
		if (request.age() != null) {
			user.setAge(request.age());
		}
		if (request.gender() != null) {
			user.setGender(request.gender().name());
		}
		if (request.experience() != null) {
			user.setExperience(request.experience().name());
		}
		if (request.avatarUrl() != null) {
			String avatar = request.avatarUrl().isBlank() ? null : request.avatarUrl().trim();
			user.setAvatarUrl(avatar);
		}
	}

	private void validatePatchHasAtLeastOneField(PatchMeRequest request) {
		boolean empty = request.displayName() == null
				&& request.heightCm() == null
				&& request.weightKg() == null
				&& request.age() == null
				&& request.gender() == null
				&& request.experience() == null
				&& request.avatarUrl() == null;
		if (empty) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "At least one field must be provided");
		}
	}

	private void disableFirebaseAccount(String firebaseUid) {
		try {
			FirebaseAuth.getInstance().updateUser(
					new UserRecord.UpdateRequest(firebaseUid).setDisabled(true)
			);
			FirebaseAuth.getInstance().revokeRefreshTokens(firebaseUid);
		} catch (FirebaseAuthException e) {
			log.error("Failed to disable Firebase Auth user {}: {}", firebaseUid, e.getMessage());
			throw new ApiException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to disable authentication for deleted account"
			);
		}
	}
}

package com.davendra.calistrack_backend.user.service;

import com.davendra.calistrack_backend.auth.entity.UserAuthIdentity;
import com.davendra.calistrack_backend.auth.exception.AuthException;
import com.davendra.calistrack_backend.auth.repo.UserAuthIdentityRepository;
import com.davendra.calistrack_backend.common.exception.ApiException;
import com.davendra.calistrack_backend.common.security.SecurityUtils;
import com.davendra.calistrack_backend.user.entity.AppUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resolves the authenticated Firebase principal to the local {@link AppUser}.
 */
@Service
public class CurrentUserService {

	private final UserAuthIdentityRepository userAuthIdentityRepository;

	public CurrentUserService(UserAuthIdentityRepository userAuthIdentityRepository) {
		this.userAuthIdentityRepository = userAuthIdentityRepository;
	}

	@Transactional(readOnly = true)
	public AppUser requireActiveUser() {
		String firebaseUid = SecurityUtils.requireFirebaseUid();

		UserAuthIdentity identity = userAuthIdentityRepository
				.findActiveByProviderAndProviderSubject(UserAuthIdentity.PROVIDER_FIREBASE, firebaseUid)
				.orElseThrow(() -> new AuthException("User not found locally — register first"));

		AppUser user = identity.getUser();
		if (user.isDeleted()) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Account has been deleted");
		}
		return user;
	}

	@Transactional(readOnly = true)
	public AppUser requireActiveUserWithGoal() {
		String firebaseUid = SecurityUtils.requireFirebaseUid();

		UserAuthIdentity identity = userAuthIdentityRepository
				.findActiveByProviderAndProviderSubjectWithGoal(UserAuthIdentity.PROVIDER_FIREBASE, firebaseUid)
				.orElseThrow(() -> new AuthException("User not found locally — register first"));

		AppUser user = identity.getUser();
		if (user.isDeleted()) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Account has been deleted");
		}
		return user;
	}

	public String requireFirebaseUid() {
		return SecurityUtils.requireFirebaseUid();
	}

	/**
	 * Same as {@link #requireActiveUser()} but requires {@code app_user.role = ADMIN}.
	 */
	@Transactional(readOnly = true)
	public AppUser requireAdmin() {
		AppUser user = requireActiveUser();
		if (!"ADMIN".equals(user.getRole())) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Admin access required");
		}
		return user;
	}
}

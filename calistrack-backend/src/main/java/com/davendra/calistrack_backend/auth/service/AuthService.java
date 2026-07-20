package com.davendra.calistrack_backend.auth.service;

import com.davendra.calistrack_backend.auth.client.FirebaseAuthClient;
import com.davendra.calistrack_backend.auth.client.FirebaseAuthClient.FirebaseAuthTokens;
import com.davendra.calistrack_backend.auth.dto.AuthResponse;
import com.davendra.calistrack_backend.auth.dto.LoginRequest;
import com.davendra.calistrack_backend.auth.dto.LogoutRequest;
import com.davendra.calistrack_backend.auth.dto.RefreshRequest;
import com.davendra.calistrack_backend.auth.dto.RegisterRequest;
import com.davendra.calistrack_backend.auth.entity.RefreshToken;
import com.davendra.calistrack_backend.auth.entity.UserAuthIdentity;
import com.davendra.calistrack_backend.auth.exception.AuthException;
import com.davendra.calistrack_backend.auth.repo.RefreshTokenRepository;
import com.davendra.calistrack_backend.auth.repo.UserAuthIdentityRepository;
import com.davendra.calistrack_backend.auth.utils.TokenHashUtils;
import com.davendra.calistrack_backend.user.entity.AppUser;
import com.davendra.calistrack_backend.user.repo.AppUserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthService {

	private static final Logger log = LoggerFactory.getLogger(AuthService.class);
	private static final long REFRESH_TOKEN_TTL_DAYS = 365;

	private final FirebaseAuthClient firebaseAuthClient;
	private final AppUserRepository appUserRepository;
	private final UserAuthIdentityRepository userAuthIdentityRepository;
	private final RefreshTokenRepository refreshTokenRepository;

	public AuthService(
			FirebaseAuthClient firebaseAuthClient,
			AppUserRepository appUserRepository,
			UserAuthIdentityRepository userAuthIdentityRepository,
			RefreshTokenRepository refreshTokenRepository
	) {
		this.firebaseAuthClient = firebaseAuthClient;
		this.appUserRepository = appUserRepository;
		this.userAuthIdentityRepository = userAuthIdentityRepository;
		this.refreshTokenRepository = refreshTokenRepository;
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		if (userAuthIdentityRepository
				.findByProviderAndEmail(UserAuthIdentity.PROVIDER_FIREBASE, request.email())
				.isPresent()) {
			throw new AuthException("Email already registered");
		}

		FirebaseAuthTokens tokens = firebaseAuthClient.signUp(request.email(), request.password());
		// Firebase Auth is outside the DB txn — compensate by deleting the Auth user on rollback.
		boolean syncRegistered = registerFirebaseSignupCompensation(tokens.localId());

		try {
			AppUser user = new AppUser();
			user.setDisplayName(request.displayName());
			user = appUserRepository.save(user);

			UserAuthIdentity identity = new UserAuthIdentity();
			identity.setUser(user);
			identity.setProvider(UserAuthIdentity.PROVIDER_FIREBASE);
			identity.setEmail(request.email());
			identity.setProviderSubject(tokens.localId());
			userAuthIdentityRepository.save(identity);

			persistRefreshToken(user, tokens.refreshToken());

			return toResponse(tokens, user);
		} catch (RuntimeException ex) {
			// Fallback when no Spring txn sync is active (afterCompletion would not run).
			if (!syncRegistered) {
				deleteFirebaseUser(tokens.localId());
			}
			throw ex;
		}
	}

	@Transactional
	public AuthResponse login(LoginRequest request) {
		FirebaseAuthTokens tokens = firebaseAuthClient.signIn(request.email(), request.password());

		UserAuthIdentity identity = userAuthIdentityRepository
				.findByProviderAndProviderSubject(UserAuthIdentity.PROVIDER_FIREBASE, tokens.localId())
				.or(() -> userAuthIdentityRepository.findByProviderAndEmail(
						UserAuthIdentity.PROVIDER_FIREBASE, request.email()))
				.orElseThrow(() -> new AuthException("User not found locally — register first"));

		AppUser user = identity.getUser();
		rejectIfDeleted(user);

		// End other app sessions in DB only. Do NOT call Firebase revokeRefreshTokens here:
		// that sets tokensValidAfterTime and makes verifyIdToken(..., checkRevoked=true)
		// reject the brand-new ID token (login 200, then /me 401).
		refreshTokenRepository.revokeAllActiveForUser(user, Instant.now());
		persistRefreshToken(user, tokens.refreshToken());

		return toResponse(tokens, user);
	}

	@Transactional
	public AuthResponse refresh(RefreshRequest request) {
		RefreshToken stored = requireActiveRefresh(request.refreshToken());
		rejectIfDeleted(stored.getUser());

		FirebaseAuthTokens tokens = firebaseAuthClient.refresh(request.refreshToken());

		stored.setRevokedAt(Instant.now());
		persistRefreshToken(stored.getUser(), tokens.refreshToken());

		return toResponse(tokens, stored.getUser());
	}

	@Transactional
	public void logout(LogoutRequest request) {
		refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(TokenHashUtils.sha256(request.refreshToken()))
				.ifPresent(token -> token.setRevokedAt(Instant.now()));
		// ID token stays valid until Firebase expiry (~1h). Instant kill of all devices:
		// call FirebaseAuth.revokeRefreshTokens(uid) instead (logout-all).
	}

	private RefreshToken requireActiveRefresh(String rawRefreshToken) {
		return refreshTokenRepository
				.findByTokenHashAndRevokedAtIsNull(TokenHashUtils.sha256(rawRefreshToken))
				.filter(RefreshToken::isActive)
				.orElseThrow(() -> new AuthException("Invalid or revoked refresh token"));
	}

	private void rejectIfDeleted(AppUser user) {
		if (user.isDeleted()) {
			throw new AuthException("Account has been deleted");
		}
	}

	private void persistRefreshToken(AppUser user, String rawRefreshToken) {
		RefreshToken row = new RefreshToken();
		row.setUser(user);
		row.setTokenHash(TokenHashUtils.sha256(rawRefreshToken));
		row.setExpiresAt(Instant.now().plus(REFRESH_TOKEN_TTL_DAYS, ChronoUnit.DAYS));
		refreshTokenRepository.save(row);
	}

	private boolean registerFirebaseSignupCompensation(String firebaseUid) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			return false;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCompletion(int status) {
				if (status == STATUS_ROLLED_BACK) {
					deleteFirebaseUser(firebaseUid);
				}
			}
		});
		return true;
	}

	private void deleteFirebaseUser(String firebaseUid) {
		try {
			FirebaseAuth.getInstance().deleteUser(firebaseUid);
			log.info("Deleted orphaned Firebase Auth user after register rollback: {}", firebaseUid);
		} catch (FirebaseAuthException e) {
			// Original DB failure still surfaces to the client; log so ops can clean up manually.
			log.error("Failed to delete Firebase Auth user {} after register rollback: {}",
					firebaseUid, e.getMessage());
		}
	}

	private AuthResponse toResponse(FirebaseAuthTokens tokens, AppUser user) {
//		String email = tokens.email();
//		if (email == null) {
//			email = userAuthIdentityRepository
//					.findByProviderAndProviderSubject(UserAuthIdentity.PROVIDER_FIREBASE, tokens.localId())
//					.map(UserAuthIdentity::getEmail)
//					.orElse(null);
//		}
		return new AuthResponse(
				tokens.idToken(),
				tokens.refreshToken(),
				tokens.expiresIn(),
				user.getId(),
				tokens.email(),
				user.getDisplayName()
		);
	}
}

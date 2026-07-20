package com.davendra.calistrack_backend.auth.repo;

import com.davendra.calistrack_backend.auth.entity.RefreshToken;
import com.davendra.calistrack_backend.user.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

	Optional<RefreshToken> findByTokenHashAndRevokedAtIsNull(String tokenHash);

	@Modifying(clearAutomatically = true)
	@Query("update RefreshToken r set r.revokedAt = :now where r.user = :user and r.revokedAt is null")
	int revokeAllActiveForUser(@Param("user") AppUser user, @Param("now") Instant now);
}

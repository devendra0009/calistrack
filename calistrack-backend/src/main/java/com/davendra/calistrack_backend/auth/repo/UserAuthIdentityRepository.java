package com.davendra.calistrack_backend.auth.repo;

import com.davendra.calistrack_backend.auth.entity.UserAuthIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserAuthIdentityRepository extends JpaRepository<UserAuthIdentity, UUID> {

	@Query("""
			select i from UserAuthIdentity i
			join fetch i.user
			where i.provider = :provider and i.email = :email
			""")
	Optional<UserAuthIdentity> findByProviderAndEmail(
			@Param("provider") String provider,
			@Param("email") String email
	);

	@Query("""
			select i from UserAuthIdentity i
			join fetch i.user
			where i.provider = :provider and i.providerSubject = :providerSubject
			""")
	Optional<UserAuthIdentity> findByProviderAndProviderSubject(
			@Param("provider") String provider,
			@Param("providerSubject") String providerSubject
	);

	@Query("""
			select i from UserAuthIdentity i
			join fetch i.user u
			where i.provider = :provider
			  and i.providerSubject = :providerSubject
			  and u.deletedAt is null
			""")
	Optional<UserAuthIdentity> findActiveByProviderAndProviderSubject(
			@Param("provider") String provider,
			@Param("providerSubject") String providerSubject
	);

	@Query("""
			select i from UserAuthIdentity i
			join fetch i.user u
			left join fetch u.currentGoalNode
			where i.provider = :provider
			  and i.providerSubject = :providerSubject
			  and u.deletedAt is null
			""")
	Optional<UserAuthIdentity> findActiveByProviderAndProviderSubjectWithGoal(
			@Param("provider") String provider,
			@Param("providerSubject") String providerSubject
	);
}

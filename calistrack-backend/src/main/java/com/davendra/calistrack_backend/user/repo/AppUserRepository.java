package com.davendra.calistrack_backend.user.repo;

import com.davendra.calistrack_backend.user.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
}

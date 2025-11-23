package com.bookfair.auth_service.repository;

import com.bookfair.auth_service.entity.User;
import com.bookfair.auth_service.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByMobileNo(String mobileNo);

    boolean existsByMobileNoAndIdNot(String mobileNo, UUID id);

    long countByIdAndRole(UUID id, UserRole role);
}


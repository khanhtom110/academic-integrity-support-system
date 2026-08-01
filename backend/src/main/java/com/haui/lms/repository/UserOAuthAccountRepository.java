package com.haui.lms.repository;

import com.haui.lms.entity.AuthProvider;
import com.haui.lms.entity.UserOAuthAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserOAuthAccountRepository extends JpaRepository<UserOAuthAccount, UUID> {

    boolean existsByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    Optional<UserOAuthAccount> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
}
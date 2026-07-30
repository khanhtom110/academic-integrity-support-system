package com.haui.lms.repository;

import com.haui.lms.entity.UserOAuthAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserOAuthAccountRepository extends JpaRepository<UserOAuthAccount, UUID> {
}

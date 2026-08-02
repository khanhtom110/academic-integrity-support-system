package com.haui.lms.mock.repository;

import com.haui.lms.dto.AuthResponse;
import org.springframework.data.repository.CrudRepository;

public interface RedisTokenRepository extends CrudRepository<AuthResponse, String> {
}

package com.haui.lms.user.controller;

import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

/**
 * TAM THOI - mo phong lay current user cho toi khi JwtAuthenticationFilter cua Khanh (feature/be-core-security-oauth2)
 * dua user vao SecurityContext. Test tam bang cach gui header "X-User-Id: <uuid>" trong Postman. XOA class nay khi tich
 * hop that, thay bang lay tu SecurityContextHolder.
 */
public final class CurrentUserHolder {

    private CurrentUserHolder() {
    }

    public static UUID getCurrentUserId(HttpServletRequest request) {
        String header = request.getHeader("X-User-Id");
        if (header == null || header.isBlank()) {
            throw new IllegalStateException("Thieu header X-User-Id (tam thoi) - can JWT that de lay userId.");
        }
        return UUID.fromString(header);
    }
}

package com.haui.lms.security;

import com.haui.lms.constant.ApiPath;
import com.haui.lms.constant.UrlConstant;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, String> redisTemplate;
    private static final int MAX_REQUESTS_PER_MINUTE = 5;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        if (requestURI.startsWith(ApiPath.API_V1 + UrlConstant.Auth.LOGIN)
                || requestURI.startsWith(ApiPath.API_V1 + UrlConstant.Auth.REGISTER)) {

            // Lay IP cua User
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null) {
                // Lay dia chi cua nguoi dang conect voi Spring boot
                ip = request.getRemoteAddr();
            }

            String redisKey = "RATE_LIMIT:" + ip;

            // request = 0+1=1
            Long requests = redisTemplate.opsForValue().increment(redisKey);

            // Neu la request dau tien + them 1 phut
            if (requests != null && requests == 1L) {
                redisTemplate.expire(redisKey, Duration.ofMinutes(1));
            }

            if (requests != null && requests > MAX_REQUESTS_PER_MINUTE) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write(
                        "{\"statusCode\": 429, \"message\": \"Too many request. Please try again after 1 minute.\", \"data\": null}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}

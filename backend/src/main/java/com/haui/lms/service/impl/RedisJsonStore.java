package com.haui.lms.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;

/**
 * Doc ghi JSON tren Redis dung chung cho cac module tra cuu tap chi.
 * <p>
 * Nguyen tac: cache hong thi coi nhu chua co, khong bao gio lam hong request cua nguoi dung. Vi vay moi phuong thuc deu
 * nuot loi va chi ghi log, ben goi cu xu ly nhu cache miss binh thuong.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RedisJsonStore {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * @return null neu chua co trong cache hoac doc that bai
     */
    public <T> T get(String key, Class<T> type) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            return json == null ? null : objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.warn("Failed to read cache. Key: {}", key, e);
            return null;
        }
    }

    /**
     * Doc mot danh sach. Truyen kieu mang chu khong phai kieu phan tu, vi du JournalSearchResponse[].class, de khong
     * phai dung den TypeFactory.
     *
     * @return null neu chua co trong cache hoac doc that bai
     */
    public <T> List<T> getList(String key, Class<T[]> arrayType) {
        T[] array = get(key, arrayType);
        return array == null ? null : List.of(array);
    }

    public void put(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (Exception e) {
            log.warn("Failed to write cache. Key: {}", key, e);
        }
    }
}

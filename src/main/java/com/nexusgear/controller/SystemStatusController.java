package com.nexusgear.controller;

import com.nexusgear.repository.mongo.ProductRepository;
import com.nexusgear.repository.postgres.AdminUserRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/status")
public class SystemStatusController {

    private final AdminUserRepository adminUserRepository;
    private final ProductRepository productRepository;
    private final StringRedisTemplate redisTemplate;

    public SystemStatusController(AdminUserRepository adminUserRepository,
                                  ProductRepository productRepository,
                                  StringRedisTemplate redisTemplate) {
        this.adminUserRepository = adminUserRepository;
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping
    public Map<String, Object> checkDatabasesStatus() {
        Map<String, Object> status = new LinkedHashMap<>();

        try {
            long adminsCount = adminUserRepository.count();
            status.put("supabasePostgres", Map.of("online", true, "adminsCount", adminsCount));
        } catch (Exception e) {
            status.put("supabasePostgres", Map.of("online", false, "error", e.getMessage()));
        }

        try {
            long productsCount = productRepository.count();
            status.put("mongoDb", Map.of("online", true, "productsCount", productsCount));
        } catch (Exception e) {
            status.put("mongoDb", Map.of("online", false, "error", e.getMessage()));
        }

        try {
            String pong = redisTemplate.getConnectionFactory() != null
                    ? redisTemplate.getConnectionFactory().getConnection().ping()
                    : null;
            status.put("redis", Map.of("online", "PONG".equalsIgnoreCase(pong), "ping", String.valueOf(pong)));
        } catch (Exception e) {
            status.put("redis", Map.of("online", false, "error", e.getMessage()));
        }

        return status;
    }
}

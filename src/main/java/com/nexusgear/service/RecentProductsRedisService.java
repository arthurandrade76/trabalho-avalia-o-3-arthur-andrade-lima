package com.nexusgear.service;

import com.nexusgear.model.mongo.Product;
import com.nexusgear.repository.mongo.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class RecentProductsRedisService {

    private static final Logger log = LoggerFactory.getLogger(RecentProductsRedisService.class);
    private static final String KEY_PREFIX = "nexus:recent_products:";
    private static final int MAX_RECENT_ITEMS = 6;
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;
    private final ProductRepository productRepository;
    private final Map<String, Deque<String>> fallbackCache = new ConcurrentHashMap<>();

    public RecentProductsRedisService(StringRedisTemplate redisTemplate, ProductRepository productRepository) {
        this.redisTemplate = redisTemplate;
        this.productRepository = productRepository;
    }

    public void recordProductView(String visitorId, String productId) {
        if (visitorId == null || visitorId.isBlank() || productId == null || productId.isBlank()) {
            return;
        }
        String key = KEY_PREFIX + visitorId.trim();

        try {
            redisTemplate.opsForList().remove(key, 0, productId);
            redisTemplate.opsForList().leftPush(key, productId);
            redisTemplate.opsForList().trim(key, 0, MAX_RECENT_ITEMS - 1);
            redisTemplate.expire(key, CACHE_TTL);
            log.info("[REDIS] Produto id={} gravado na chave '{}' (LPUSH + LTRIM)", productId, key);
        } catch (Exception e) {
            Deque<String> list = fallbackCache.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
            list.remove(productId);
            list.addFirst(productId);
            while (list.size() > MAX_RECENT_ITEMS) {
                list.removeLast();
            }
        }
    }

    public Map<String, Object> getRecentProducts(String visitorId) {
        if (visitorId == null || visitorId.isBlank()) {
            return Map.of("source", "NONE", "items", Collections.emptyList());
        }
        String key = KEY_PREFIX + visitorId.trim();
        List<String> productIds;
        String source = "REDIS";

        try {
            productIds = redisTemplate.opsForList().range(key, 0, MAX_RECENT_ITEMS - 1);
            if (productIds == null) {
                productIds = Collections.emptyList();
            }
        } catch (Exception e) {
            source = "REDIS_FALLBACK_MEMORY";
            Deque<String> fallback = fallbackCache.getOrDefault(key, new ArrayDeque<>());
            productIds = new ArrayList<>(fallback);
        }

        if (productIds.isEmpty()) {
            return Map.of("source", source, "redisKey", key, "items", Collections.emptyList());
        }

        List<Product> orderedProducts = new ArrayList<>();
        for (String id : productIds) {
            productRepository.findById(id).ifPresent(orderedProducts::add);
        }

        return Map.of(
                "source", source,
                "redisKey", key,
                "items", orderedProducts
        );
    }

    public void clearRecentProducts(String visitorId) {
        if (visitorId == null || visitorId.isBlank()) {
            return;
        }
        String key = KEY_PREFIX + visitorId.trim();
        try {
            redisTemplate.delete(key);
        } catch (Exception ignored) {
        }
        fallbackCache.remove(key);
    }
}

package com.nexusgear.controller;

import com.nexusgear.service.RecentProductsRedisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/recent")
public class RecentProductsController {

    private final RecentProductsRedisService recentProductsRedisService;

    public RecentProductsController(RecentProductsRedisService recentProductsRedisService) {
        this.recentProductsRedisService = recentProductsRedisService;
    }

    @GetMapping("/{visitorId}")
    public ResponseEntity<Map<String, Object>> getRecentProducts(@PathVariable String visitorId) {
        return ResponseEntity.ok(recentProductsRedisService.getRecentProducts(visitorId));
    }

    @PostMapping("/{visitorId}/{productId}")
    public ResponseEntity<Map<String, Object>> recordProductSelection(
            @PathVariable String visitorId,
            @PathVariable String productId) {
        recentProductsRedisService.recordProductView(visitorId, productId);
        return ResponseEntity.ok(recentProductsRedisService.getRecentProducts(visitorId));
    }

    @DeleteMapping("/{visitorId}")
    public ResponseEntity<Map<String, String>> clearRecentProducts(@PathVariable String visitorId) {
        recentProductsRedisService.clearRecentProducts(visitorId);
        return ResponseEntity.ok(Map.of("message", "Cache de produtos recentes limpo no Redis."));
    }
}

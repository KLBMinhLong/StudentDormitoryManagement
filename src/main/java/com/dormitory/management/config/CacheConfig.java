package com.dormitory.management.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CacheConfig {

    public static final String BUILDINGS_LIST_CACHE = "buildingsList";
    public static final String BUILDING_BY_ID_CACHE = "buildingById";
    public static final String ROOM_TYPES_LIST_CACHE = "roomTypesList";
    public static final String PRICING_LATEST_CACHE = "pricingLatest";

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                new CaffeineCache(BUILDINGS_LIST_CACHE, Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofSeconds(60))
                        .maximumSize(200)
                        .build()),
                new CaffeineCache(BUILDING_BY_ID_CACHE, Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofSeconds(120))
                        .maximumSize(500)
                        .build()),
                new CaffeineCache(ROOM_TYPES_LIST_CACHE, Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofSeconds(90))
                        .maximumSize(200)
                        .build()),
                new CaffeineCache(PRICING_LATEST_CACHE, Caffeine.newBuilder()
                        .expireAfterWrite(Duration.ofSeconds(60))
                        .maximumSize(20)
                        .build())
        ));
        return manager;
    }
}

package com.davendra.calistrack_backend.path.config;

import com.davendra.calistrack_backend.path.catalog.DbGoalPathCatalog;
import com.davendra.calistrack_backend.stretching.service.StretchCatalogService;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class PathCacheConfig {

	@Bean
	CacheManager cacheManager() {
		CaffeineCacheManager manager = new CaffeineCacheManager(
				DbGoalPathCatalog.PATH_CACHE,
				StretchCatalogService.PLAN_CACHE,
				StretchCatalogService.PLAN_DAY_CACHE,
				StretchCatalogService.EXERCISES_CACHE
		);
		manager.setCaffeine(Caffeine.newBuilder()
				.maximumSize(512)
				.expireAfterWrite(24, TimeUnit.HOURS));
		return manager;
	}
}

package com.bytemarket.bytemarket_api.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.jcache.spi.CaffeineCachingProvider;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager jCacheCacheManager() {
        // Obtém o CachingProvider do Caffeine
        CaffeineCachingProvider cachingProvider = (CaffeineCachingProvider) Caching.getCachingProvider(
                "com.github.benmanes.caffeine.jcache.spi.CaffeineCachingProvider"
        );

        // Cria o CacheManager
        CacheManager cacheManager = cachingProvider.getCacheManager();

        // Configuração base para todos os caches
        MutableConfiguration<Object, Object> configuration = new MutableConfiguration<>()
                .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ONE_HOUR))
                .setStatisticsEnabled(true)
                .setStoreByValue(false);

        // Cria os caches necessários para o Bucket4j
        String[] cacheNames = {
                "login-bucket",
                "register-bucket",
                "webhook-bucket",
                "admin-bucket",
                "store-bucket"
        };

        for (String cacheName : cacheNames) {
            if (cacheManager.getCache(cacheName) == null) {
                cacheManager.createCache(cacheName, configuration);
            }
        }

        return cacheManager;
    }
}
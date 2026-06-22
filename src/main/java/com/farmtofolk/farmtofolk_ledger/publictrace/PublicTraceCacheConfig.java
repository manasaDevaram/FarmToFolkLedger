package com.farmtofolk.farmtofolk_ledger.publictrace;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
public class PublicTraceCacheConfig {

    @Bean
    RedisCacheManagerBuilderCustomizer publicTraceStableCacheCustomizer() {
        return builder -> builder.withCacheConfiguration(
                "publicTraceStable",
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(10))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()
                        ))
        );
    }

    @Bean
    CacheErrorHandler cacheErrorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                // Cache failures should fall back to PostgreSQL reads.
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                // Cache write failures should not block API responses.
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                // Cache eviction failures can be retried by future write flows.
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                // Cache clear failures should not stop application behavior.
            }
        };
    }
}

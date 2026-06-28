package com.farmtofolk.farmtofolk_ledger.publictrace;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import java.time.Duration;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
public class PublicTraceCacheConfig {

  @Bean
  RedisCacheManagerBuilderCustomizer publicTraceStableCacheCustomizer(ObjectMapper objectMapper) {
    ObjectMapper redisObjectMapper = objectMapper.copy();
    redisObjectMapper.activateDefaultTyping(
        LaissezFaireSubTypeValidator.instance,
        ObjectMapper.DefaultTyping.EVERYTHING,
        JsonTypeInfo.As.PROPERTY);
    GenericJackson2JsonRedisSerializer.registerNullValueSerializer(redisObjectMapper, null);
    GenericJackson2JsonRedisSerializer serializer =
        new GenericJackson2JsonRedisSerializer(redisObjectMapper);

    return builder ->
        builder.withCacheConfiguration(
            "publicTraceStable",
            RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(serializer)));
  }

  @Bean
  CacheErrorHandler cacheErrorHandler() {
    return new CacheErrorHandler() {
      @Override
      public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        // Cache failures should fall back to PostgreSQL reads.
      }

      @Override
      public void handleCachePutError(
          RuntimeException exception, Cache cache, Object key, Object value) {
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

package com.filestore.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.filestore.dto.FileMetadataDTO;
import com.filestore.dto.PageResponse;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public ObjectMapper redisObjectMapper() {

        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.registerModule(new JavaTimeModule());

        objectMapper.disable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
        );

        return objectMapper;
    }


    // =========================================================
    // RedisTemplate
    // =========================================================

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper redisObjectMapper) {

        RedisTemplate<String, Object> template =
                new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer keySerializer =
                new StringRedisSerializer();

        GenericJackson2JsonRedisSerializer valueSerializer =
                new GenericJackson2JsonRedisSerializer(
                        redisObjectMapper
                );

        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);

        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();

        return template;
    }


    // =========================================================
    // CacheManager
    // =========================================================

    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            ObjectMapper redisObjectMapper) {

        StringRedisSerializer keySerializer =
                new StringRedisSerializer();


        // -----------------------------------------------------
        // Generic serializer
        // Used as default
        // -----------------------------------------------------

        GenericJackson2JsonRedisSerializer genericSerializer =
                new GenericJackson2JsonRedisSerializer(
                        redisObjectMapper
                );


        // -----------------------------------------------------
        // Typed serializer for:
        //
        // PageResponse<FileMetadataDTO>
        // -----------------------------------------------------

        JavaType filesJavaType =
                redisObjectMapper.getTypeFactory()
                        .constructParametricType(
                                PageResponse.class,
                                FileMetadataDTO.class
                        );

        Jackson2JsonRedisSerializer<Object> filesSerializer =
                new Jackson2JsonRedisSerializer<>(
                        redisObjectMapper,
                        filesJavaType
                );


        // -----------------------------------------------------
        // Key serializer
        // -----------------------------------------------------

        RedisSerializationContext.SerializationPair<String>
                keyPair =
                RedisSerializationContext.SerializationPair
                        .fromSerializer(keySerializer);


        // -----------------------------------------------------
        // Default value serializer
        // -----------------------------------------------------

        RedisSerializationContext.SerializationPair<Object>
                genericValuePair =
                RedisSerializationContext.SerializationPair
                        .fromSerializer(genericSerializer);


        // -----------------------------------------------------
        // Files value serializer
        // -----------------------------------------------------

        RedisSerializationContext.SerializationPair<Object>
                filesValuePair =
                RedisSerializationContext.SerializationPair
                        .fromSerializer(filesSerializer);


        // -----------------------------------------------------
        // Default cache configuration
        // -----------------------------------------------------

        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofHours(1))
                        .serializeKeysWith(keyPair)
                        .serializeValuesWith(genericValuePair)
                        .disableCachingNullValues();


        // -----------------------------------------------------
        // Files cache
        // -----------------------------------------------------

        RedisCacheConfiguration filesConfig =
                defaultConfig
                        .entryTtl(Duration.ofMinutes(30))
                        .serializeValuesWith(filesValuePair);


        // -----------------------------------------------------
        // Users cache
        // -----------------------------------------------------

        RedisCacheConfiguration usersConfig =
                defaultConfig
                        .entryTtl(Duration.ofHours(2));


        // -----------------------------------------------------
        // Redis Cache Manager
        // -----------------------------------------------------

        return RedisCacheManager.builder(connectionFactory)

                .cacheDefaults(defaultConfig)

                .withCacheConfiguration(
                        "files",
                        filesConfig
                )

                .withCacheConfiguration(
                        "users",
                        usersConfig
                )

                .build();
    }
}
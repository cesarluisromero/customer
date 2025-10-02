package com.bootcamp.customer.config;

import com.bootcamp.customer.domain.model.Customer;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Configuration
public class CacheConfig {

    public static final String BY_ID      = "customer.byId";
    public static final String BY_DOC     = "customer.byDoc";
    public static final String BY_ID_MONO = "customer.byIdMono"; // para el coalescing

    @Bean(BY_ID)
    public Cache<String, Customer> customerByIdCache() {
        return Caffeine.newBuilder()
                .maximumSize(1_000)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats()
                .build();
    }

    @Bean(BY_DOC)
    public Cache<String, Customer> customerByDocCache() {
        return Caffeine.newBuilder()
                .maximumSize(1_000)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats()
                .build();
    }

    @Bean(BY_ID_MONO)
    public Cache<String, Mono<Customer>> byIdMonoCache() {
        return Caffeine.newBuilder()
                .maximumSize(1_000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .build();
    }
}


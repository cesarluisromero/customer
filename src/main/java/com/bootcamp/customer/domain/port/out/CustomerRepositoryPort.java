package com.bootcamp.customer.domain.port.out;

import com.bootcamp.customer.domain.model.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerRepositoryPort {
    Flux<Customer> findAll();
    Flux<Customer> findByType(CustomerType type);
    Mono<Customer> findById(String id);
    Mono<Customer> findByDocumentNumber(String doc);
    Mono<Customer> save(Customer customer);
    Mono<Void> deleteById(String id);
}
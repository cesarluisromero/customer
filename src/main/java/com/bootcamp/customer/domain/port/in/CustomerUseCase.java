package com.bootcamp.customer.domain.port.in;

import com.bootcamp.customer.domain.model.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerUseCase {
    Flux<Customer> findAll(CustomerType type);     // null -> todos
    Mono<Customer> findById(String id);
    Mono<Customer> create(Customer customer);
    Mono<Customer> update(String id, Customer customer);
    Mono<Void> delete(String id);
}

package com.bootcamp.customer.infrastructure.out.persistence.repository;

import com.bootcamp.customer.infrastructure.out.persistence.document.CustomerDocument;
import com.bootcamp.customer.domain.model.CustomerType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveCustomerMongoRepository
        extends ReactiveMongoRepository<CustomerDocument, String> {

    Mono<CustomerDocument> findByDocumentNumber(String documentNumber);
    Flux<CustomerDocument> findByType(CustomerType type);
}

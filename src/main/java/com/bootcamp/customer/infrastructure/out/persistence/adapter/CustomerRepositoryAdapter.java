package com.bootcamp.customer.infrastructure.out.persistence.adapter;

import com.bootcamp.customer.domain.model.*;
import com.bootcamp.customer.domain.port.out.CustomerRepositoryPort;
import com.bootcamp.customer.infrastructure.out.persistence.document.CustomerDocument;
import com.bootcamp.customer.infrastructure.out.persistence.repository.ReactiveCustomerMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CustomerRepositoryAdapter implements CustomerRepositoryPort {

    private final ReactiveCustomerMongoRepository repo;

    private Customer toDomain(CustomerDocument d) {
        return Customer.builder()
                .id(d.getId()).type(d.getType()).documentNumber(d.getDocumentNumber())
                .fullName(d.getFullName()).businessName(d.getBusinessName())
                .email(d.getEmail()).phones(d.getPhones()).build();
    }
    private CustomerDocument toDoc(Customer c) {
        return CustomerDocument.builder()
                .id(c.getId()).type(c.getType()).documentNumber(c.getDocumentNumber())
                .fullName(c.getFullName()).businessName(c.getBusinessName())
                .email(c.getEmail()).phones(c.getPhones()).build();
    }

    @Override public Flux<Customer> findAll() {
        return repo.findAll().map(this::toDomain);
    }


    @Override public Mono<Customer> findById(String id){
        return repo.findById(id).map(this::toDomain);
    }

    @Override public Mono<Customer> findByDocumentNumber(String doc){
        return repo.findByDocumentNumber(doc).map(this::toDomain);
    }
    @Override public Flux<Customer> findByType(CustomerType t){
        return repo.findByType(t).map(this::toDomain);
    }

    @Override public Mono<Customer> save(Customer c){
        return repo.save(toDoc(c)).map(this::toDomain);
    }
    @Override public Mono<Void> deleteById(String id){
        return repo.deleteById(id);
    }
}
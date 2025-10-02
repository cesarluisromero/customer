package com.bootcamp.customer.infrastructure.in.web.controller;

import com.bootcamp.customer.domain.model.CustomerType;
import com.bootcamp.customer.domain.port.in.CustomerUseCase;
import com.bootcamp.customer.infrastructure.in.web.dto.CustomerDto;
import com.bootcamp.customer.infrastructure.in.web.mapper.CustomerMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers")
@Tag(name = "Customer")
public class CustomerController {

    private final CustomerUseCase useCase;

    @GetMapping
    public Flux<CustomerDto> findAll(@RequestParam(required=false) CustomerType type){
        return useCase.findAll(type).map(CustomerMapper::toDto);
    }

    @GetMapping("/{id}")
    public Mono<CustomerDto> findById(@PathVariable String id){
        return useCase.findById(id).map(CustomerMapper::toDto);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CustomerDto> create(@Validated @RequestBody CustomerDto dto){
        return useCase.create(CustomerMapper.toDomain(dto)).map(CustomerMapper::toDto);
    }

    @PutMapping("/{id}")
    public Mono<CustomerDto> update(@PathVariable String id, @Validated @RequestBody CustomerDto dto){
        return useCase.update(id, CustomerMapper.toDomain(dto)).map(CustomerMapper::toDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable String id){
        return useCase.delete(id);
    }
}
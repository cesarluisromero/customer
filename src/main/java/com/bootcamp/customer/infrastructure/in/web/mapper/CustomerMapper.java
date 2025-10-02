package com.bootcamp.customer.infrastructure.in.web.mapper;

import com.bootcamp.customer.domain.model.Customer;
import com.bootcamp.customer.infrastructure.in.web.dto.CustomerDto;

public class CustomerMapper {
    public static Customer toDomain(CustomerDto d){
        return Customer.builder()
                .id(d.getId()).type(d.getType()).documentNumber(d.getDocumentNumber())
                .fullName(d.getFullName()).businessName(d.getBusinessName())
                .email(d.getEmail()).phones(d.getPhones()).build();
    }
    public static CustomerDto toDto(Customer c){
        CustomerDto d = new CustomerDto();
        d.setId(c.getId()); d.setType(c.getType()); d.setDocumentNumber(c.getDocumentNumber());
        d.setFullName(c.getFullName()); d.setBusinessName(c.getBusinessName());
        d.setEmail(c.getEmail()); d.setPhones(c.getPhones());
        return d;
    }
}

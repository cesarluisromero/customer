package com.bootcamp.customer.domain.model;

import java.util.List;
import lombok.*;

@Data @Builder(toBuilder = true) @AllArgsConstructor @NoArgsConstructor
public class Customer {
    private String id;
    private CustomerType type;
    private String documentNumber; // único
    private String fullName;       // si PERSONAL
    private String businessName;   // si ENTERPRISE
    private String email;
    private List<String> phones;


}

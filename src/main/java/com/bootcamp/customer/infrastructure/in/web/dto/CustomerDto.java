package com.bootcamp.customer.infrastructure.in.web.dto;

import com.bootcamp.customer.domain.model.CustomerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
public class CustomerDto {
    private String id;

    @NotNull
    private CustomerType type;

    @NotBlank
    private String documentNumber;

    private String fullName;     // si PERSONAL
    private String businessName; // si ENTERPRISE
    private String email;
    private List<String> phones;
}

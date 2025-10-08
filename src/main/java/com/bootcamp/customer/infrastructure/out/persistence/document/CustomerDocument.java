package com.bootcamp.customer.infrastructure.out.persistence.document;

import com.bootcamp.customer.domain.model.CustomerType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document("customers")
public class CustomerDocument {
    @Id private String id;
    private CustomerType type;
    @Indexed(unique = true)
    private String documentNumber;
    private String fullName;
    private String businessName;
    private String email;
    private List<String> phones;
}

package com.bootcamp.customer.application;

import com.bootcamp.customer.domain.model.Customer;
import com.bootcamp.customer.domain.model.CustomerType;
import com.bootcamp.customer.domain.port.out.CustomerRepositoryPort;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class CustomerServiceCacheTest {

    @Mock
    CustomerRepositoryPort repo;

    private Cache<String, Customer> idCache;
    private Cache<String, Customer> docCache;
    private Cache<String, Mono<Customer>> byIdMonoCache;

    private CustomerServiceImpl service;

    @BeforeEach
    void setUp() {
        idCache = Caffeine.newBuilder().maximumSize(10_000).expireAfterWrite(Duration.ofMinutes(10)).build();
        docCache = Caffeine.newBuilder().maximumSize(10_000).expireAfterWrite(Duration.ofMinutes(10)).build();
        byIdMonoCache = Caffeine.newBuilder().maximumSize(10_000).expireAfterWrite(Duration.ofMinutes(5)).build();
        service = new CustomerServiceImpl(repo, idCache, docCache, byIdMonoCache);
    }

    // ---------- helpers ----------
    private static Customer customer(String id, String doc, CustomerType type, String full, String biz, String email, List<String> phones) {
        Customer c = new Customer();
        c.setId(id);
        c.setDocumentNumber(doc);
        c.setType(type);
        c.setFullName(full);
        c.setBusinessName(biz);
        c.setEmail(email);
        c.setPhones(phones);
        return c;
    }

    @ParameterizedTest(name = "[{index}] startType={0} -> patchType={1}, patchFullName={2}, existingBiz={3}, expectFull={4}, expectBizNull={5}")
    @CsvFileSource(
            resources = "/data/switch_type_cases.csv",
            numLinesToSkip = 1,
            nullValues = { "null", "NULL" }
    )
    void update_cambiaType_parametrizado_limpiaCamposDependientes(
            String startType,
            String patchType,
            String patchFullName,
            String existingBizName,
            String expectedFullName,
            boolean expectBusinessNameNull
    ) {
        // -------- Arrange --------
        final CustomerType startT = CustomerType.valueOf(startType.trim());
        final CustomerType patchT = CustomerType.valueOf(patchType.trim());
        final String id = "C1-" + startType + "-" + patchType;

        final Customer existing = customer(
                id, "DOC-1", startT,
                null, existingBizName, "sales@company.com", List.of("111","222")
        );

        final Customer patch = new Customer();
        patch.setType(patchT);
        patch.setFullName(patchFullName); // puede ser null

        final Customer expected = customer(
                id, "DOC-1", patchT,
                expectedFullName,                              // puede ser null
                expectBusinessNameNull ? null : existingBizName,
                "sales@company.com",
                List.of("111","222")
        );

        when(repo.findById(id)).thenReturn(Mono.just(existing));
        when(repo.save(any(Customer.class))).thenReturn(Mono.just(expected));

        // -------- Act --------
        final Mono<Customer> result = service.update(id, patch);

        // -------- Assert --------
        final String expectedFull = expectedFullName; // final para usar en lambda
        StepVerifier.create(result)
                .expectNextMatches(c ->
                        c.getType() == patchT &&
                                java.util.Objects.equals(expectedFull, c.getFullName()) &&
                                (expectBusinessNameNull ? c.getBusinessName() == null : c.getBusinessName() != null)
                )
                .verifyComplete();

        assertThat(idCache.getIfPresent(id)).isEqualTo(expected);
        verify(repo).findById(id);
        verify(repo).save(any(Customer.class));
        verifyNoMoreInteractions(repo);
    }




}

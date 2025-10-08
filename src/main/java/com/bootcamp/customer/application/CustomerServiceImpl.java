package com.bootcamp.customer.application;

import com.bootcamp.customer.config.CacheConfig;
import com.bootcamp.customer.domain.model.Customer;
import com.bootcamp.customer.domain.model.CustomerType;
import com.bootcamp.customer.domain.port.in.CustomerUseCase;
import com.bootcamp.customer.domain.port.out.CustomerRepositoryPort;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import static java.util.Objects.nonNull;

@Slf4j
@Service
public class CustomerServiceImpl implements CustomerUseCase {

    private final CustomerRepositoryPort repo;
    private final Cache<String, Customer> customerByIdCache;
    private final Cache<String, Customer> customerByDocCache;
    private final Cache<String, Mono<Customer>> byIdMonoCache;

    //inyecto dependencias desde CacheConfig
    public CustomerServiceImpl(
            CustomerRepositoryPort repo,
            @Qualifier(CacheConfig.BY_ID)
            Cache<String, Customer> customerByIdCache,
            @Qualifier(CacheConfig.BY_DOC)
            Cache<String, Customer> customerByDocCache,
            @Qualifier(CacheConfig.BY_ID_MONO)
            Cache<String, Mono<Customer>> byIdMonoCache
    ) {
        this.repo = repo;
        this.customerByIdCache = customerByIdCache;
        this.customerByDocCache = customerByDocCache;
        this.byIdMonoCache = byIdMonoCache;
    }
    @Override
    public Flux<Customer> findAll(CustomerType type) {
        return type == null ? repo.findAll() : repo.findByType(type);
    }
    // En tu servicio
    @Override
    public Mono<Customer> findById(String id) {
        return Mono.defer(() -> {
            Customer cached = customerByIdCache.getIfPresent(id);
            if (cached != null) return Mono.just(cached);

            // coalescing opcional: evita múltiples llamadas concurrentes al backend
            Mono<Customer> cachedMono = byIdMonoCache.getIfPresent(id);
            if (cachedMono != null) return cachedMono;

            Mono<Customer> loader = repo.findById(id)
                    .doOnNext(c -> {
                        customerByIdCache.put(id, c);
                        if (nonNull(c.getDocumentNumber())) {
                            customerByDocCache.put(c.getDocumentNumber(), c);
                        }
                    })
                    .cache(); // comparte el resultado entre suscriptores

            byIdMonoCache.put(id, loader);
            return loader;
        });
    }

    @Override
    public Mono<Customer> findByDocumentNumber(String documentNumber) {
        String key = normalizeDoc(documentNumber);
        if (key == null) return Mono.empty();

        Customer cached = customerByDocCache.getIfPresent(key);
        if (cached != null) return Mono.just(cached);

        return repo.findByDocumentNumber(key)
                .doOnNext(c -> {
                    customerByDocCache.put(key, c);
                    if (nonNull(c.getId())) {
                        customerByIdCache.put(c.getId(), c);
                        byIdMonoCache.invalidate(c.getId()); // evita stale del cache Mono por id
                    }
                });
    }

    private String normalizeDoc(String doc) {
        if (doc == null) return null;
        String s = doc.trim();
        return s.isEmpty() ? null : s;
    }



    @Override
    public Mono<Customer> create(Customer customer) {
        String key = normalizeDoc(customer.getDocumentNumber());
        if (key == null) return Mono.error(new IllegalArgumentException("documentNumber required"));

        return repo.findByDocumentNumber(key).hasElement() // Mono<Boolean>
                .flatMap(exists -> exists
                        ? Mono.error(new IllegalStateException("documentNumber already exists"))
                        : repo.save(customer)
                        .doOnNext(saved -> {
                            if (saved.getId() != null) {
                                customerByIdCache.put(saved.getId(), saved);
                                byIdMonoCache.invalidate(saved.getId());
                            }
                            if (saved.getDocumentNumber() != null) {
                                customerByDocCache.put(saved.getDocumentNumber(), saved);
                            }
                        })
                );
    }




    @Override
    public Mono<Customer> update(String id, Customer patch) {
        return repo.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Customer no existe: " + id)))
                .flatMap(existing -> {
                    String oldDoc = existing.getDocumentNumber();
                    Customer toSave = applyPatch(existing, patch);
                    return repo.save(toSave)
                            .doOnNext(updated -> {
                                customerByIdCache.put(updated.getId(), updated);
                                byIdMonoCache.invalidate(updated.getId());

                                String newDoc = updated.getDocumentNumber();
                                if (oldDoc != null && !oldDoc.equals(newDoc)) {
                                    customerByDocCache.invalidate(oldDoc);
                                }
                                if (newDoc != null) {
                                    customerByDocCache.put(newDoc, updated);
                                }
                            });
                });
    }

    private Customer applyPatch(Customer base, Customer patch) {
        if (patch.getFullName() != null) base.setFullName(patch.getFullName());
        if (patch.getDocumentNumber() != null) base.setDocumentNumber(patch.getDocumentNumber());
        if (patch.getType() != null) base.setType(patch.getType());
        if (patch.getEmail() != null) {
            base.setEmail(normalizeEmail(patch.getEmail()));
        }
        if (patch.getBusinessName() != null) base.setBusinessName(patch.getBusinessName());
        if (patch.getPhones() != null) {
            base.setPhones(cleanPhones(patch.getPhones()));
        }
        return base;
    }

    private List<String> cleanPhones(List<String> phones) {
        if (phones == null) return null;
        return phones.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
    }

    private String normalizeEmail(String email) {
        String e = email.trim();
        return e.isEmpty() ? null : e.toLowerCase();
    }

    @Override
    public Mono<Void> delete(String id) {
        // obtenemos el customer para poder invalidar también por documento
        return repo.deleteById(id)
                .doOnSuccess(v -> {
                    customerByIdCache.invalidate(id);
                    byIdMonoCache.invalidate(id);
                    // si conoces el docNumber podrías invalidarlo también
                })
                .then();
    }
}

package com.example.loanapi.repository;

import com.example.loanapi.model.LoanRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory implementation of LoanRequestRepository
 * Thread-safe using ConcurrentHashMap
 */
@Repository
public class LoanRequestRepositoryImpl implements LoanRequestRepository {

    private final ConcurrentHashMap<Long, LoanRequest> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public LoanRequest save(LoanRequest loanRequest) {
        if (loanRequest.getId() == null) {
            // New entity - generate ID
            Long newId = idGenerator.getAndIncrement();
            loanRequest.setId(newId);
        }
        storage.put(loanRequest.getId(), loanRequest);
        return loanRequest;
    }

    @Override
    public Optional<LoanRequest> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<LoanRequest> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }
}

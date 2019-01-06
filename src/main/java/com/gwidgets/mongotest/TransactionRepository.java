package com.gwidgets.mongotest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
    Page<Transaction> findBySuccessIsTrueAndCreatedLessThanEqualAndUserIdOrderByCreatedDesc(long created, String userId, Pageable pageRequest);
}

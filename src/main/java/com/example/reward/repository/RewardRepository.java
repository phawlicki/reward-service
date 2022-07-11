package com.example.reward.repository;

import com.example.reward.model.Transaction;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RewardRepository extends MongoRepository<Transaction, ObjectId> {
    List<Transaction> findAllByCustomerIdAndCreatedDateTimeAfter(String customerId, LocalDateTime localDateTime);
}

package com.example.reward.model;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document
@Getter
@Setter
public class Transaction {
    @Id
    private ObjectId id;
    private BigDecimal amount;
    private final String customerId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private final LocalDateTime createdDateTime;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDateTime lastUpdateDateTime;

    public Transaction(BigDecimal amount, String customerId,
            LocalDateTime createdDateTime, LocalDateTime lastUpdateDateTime) {
        this.id = ObjectId.get();
        this.amount = amount;
        this.customerId = customerId;
        this.createdDateTime = createdDateTime;
        this.lastUpdateDateTime = lastUpdateDateTime;
    }

    @PersistenceCreator
    Transaction(ObjectId id, BigDecimal amount, String customerId,
            LocalDateTime createdDateTime, LocalDateTime lastUpdateDateTime) {
        this.id = id;
        this.amount = amount;
        this.customerId = customerId;
        this.createdDateTime = createdDateTime;
        this.lastUpdateDateTime = lastUpdateDateTime;
    }
}

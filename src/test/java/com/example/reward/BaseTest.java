package com.example.reward;

import com.example.reward.configuration.TestApplicationConfiguration;
import com.example.reward.model.Transaction;
import com.example.reward.repository.RewardRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "integration-test")
@TestPropertySource(properties = "spring.mongodb.embedded.version=3.5.5")
@ContextConfiguration(classes = {TestApplicationConfiguration.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public abstract class BaseTest {
    @Autowired
    protected RewardRepository rewardRepository;
    protected List<Transaction> transactions = new ArrayList<>();

    @BeforeEach
    protected void init() {
        transactions = List.of(buildTransaction(new BigDecimal("150"), "123"),
                buildTransaction(new BigDecimal("200"), "123"),
                buildTransaction(new BigDecimal("50"), "123"),
                buildTransaction(new BigDecimal("200"), "555"),
                buildTransaction(new BigDecimal("50"), "555"),
                buildTransaction(new BigDecimal("1000"), "123", LocalDateTime.of(2022, 1, 1, 10, 00)),
                buildTransaction(new BigDecimal("1000"), "123", LocalDateTime.now().minusMonths(2)));
        rewardRepository.saveAll(transactions);
    }

    protected Transaction buildTransaction(BigDecimal amount, String customerId) {
        return new Transaction(amount,
                customerId,
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    protected Transaction buildTransaction(BigDecimal amount, String customerId, LocalDateTime localDateTime) {
        return new Transaction(amount,
                customerId,
                localDateTime,
                localDateTime);
    }

    @AfterEach
    protected void cleanUp() {
        rewardRepository.deleteAll();
    }
}

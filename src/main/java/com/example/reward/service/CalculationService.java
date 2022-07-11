package com.example.reward.service;

import com.example.reward.model.Transaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
class CalculationService {
    private static final int FIRST_THRESHOLD = 50;
    private static final int SECOND_THRESHOLD = 100;

    Integer calculateTotalReward(List<Transaction> transactions) {
        return transactions.stream().map(Transaction::getAmount).map(this::calculateSingleReward)
                .reduce(0, Integer::sum);
    }

    int calculateSingleReward(BigDecimal amount) {
        int roundedValue = amount.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        if (roundedValue > SECOND_THRESHOLD) {
            return ((roundedValue - SECOND_THRESHOLD) * 2) + FIRST_THRESHOLD;
        }
        if (roundedValue > FIRST_THRESHOLD) {
            return roundedValue - FIRST_THRESHOLD;
        }
        return 0;
    }
}

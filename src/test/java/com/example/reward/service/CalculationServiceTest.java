package com.example.reward.service;

import com.example.reward.model.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CalculationServiceTest {
    private static final String CUSTOMER_ID = "123";
    CalculationService calculationService = new CalculationService();

    @Test
    void calculateSingleRewardWhenAmountHasNoDecimalAndIsAboveSecondThreshold() {
        //given
        BigDecimal transactionAmount = new BigDecimal("155");
        //when
        int calculatedPoints = calculationService.calculateSingleReward(transactionAmount);
        //then
        assertThat(calculatedPoints).isEqualTo(160);
    }

    @Test
    void calculateSingleRewardWhenAmountHasDecimalAndIsAboveSecondThreshold() {
        //given
        BigDecimal transactionAmount = new BigDecimal("155.5");
        //when
        int calculatedPoints = calculationService.calculateSingleReward(transactionAmount);
        //then
        assertThat(calculatedPoints).isEqualTo(162);
    }

    @Test
    void calculateSingleRewardWhenAmountHasDecimalAndIsAboveFirstThreshold() {
        //given
        BigDecimal transactionAmount = new BigDecimal("56.45");
        //when
        int calculatedPoints = calculationService.calculateSingleReward(transactionAmount);
        //then
        assertThat(calculatedPoints).isEqualTo(6);
    }

    @Test
    void calculateSingleRewardWhenAmountHasNoDecimalAndIsAboveFirstThreshold() {
        //given
        BigDecimal transactionAmount = new BigDecimal("90");
        //when
        int calculatedPoints = calculationService.calculateSingleReward(transactionAmount);
        //then
        assertThat(calculatedPoints).isEqualTo(40);
    }

    @Test
    void calculateSingleRewardWhenAmountHasNoDecimalAndIsBelowFirstThreshold() {
        //given
        BigDecimal transactionAmount = new BigDecimal("30");
        //when
        int calculatedPoints = calculationService.calculateSingleReward(transactionAmount);
        //then
        assertThat(calculatedPoints).isZero();
    }

    @Test
    void calculateSingleRewardWhenAmountIsExactlyFirstThreshold() {
        //given
        BigDecimal transactionAmount = new BigDecimal("50");
        //when
        int calculatedPoints = calculationService.calculateSingleReward(transactionAmount);
        //then
        assertThat(calculatedPoints).isZero();
    }

    @Test
    void calculateSingleRewardWhenAmountIsExactlySecondThreshold() {
        //given
        BigDecimal transactionAmount = new BigDecimal("100");
        //when
        int calculatedPoints = calculationService.calculateSingleReward(transactionAmount);
        //then
        assertThat(calculatedPoints).isEqualTo(50);
    }

    @Test
    void calculateTotalReward() {
        //given
        List<Transaction> transactions = List.of(
                buildTransaction(new BigDecimal("50.40")),
                buildTransaction(new BigDecimal("100.35")),
                buildTransaction(new BigDecimal("200")),
                buildTransaction(new BigDecimal("52")),
                buildTransaction(new BigDecimal("99.50")));
        //when//then
        assertThat(calculationService.calculateTotalReward(transactions)).isEqualTo(352);
    }

    private Transaction buildTransaction(BigDecimal amount) {
        return new Transaction(amount, CUSTOMER_ID, LocalDateTime.now(), LocalDateTime.now());
    }
}
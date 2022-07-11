package com.example.reward.service;

import com.example.reward.BaseTest;
import com.example.reward.model.TotalRewardView;
import com.example.reward.model.TransactionRequest;
import com.example.reward.model.TransactionUpdateRequest;
import com.example.reward.model.TransactionView;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RewardServiceTestIT extends BaseTest {
    @Autowired
    private RewardService rewardService;

    @Test
    void shouldSaveTransactionAndReturnView() {
        //given
        TransactionRequest request = new TransactionRequest("123", new BigDecimal("122.5"));
        //when
        TransactionView savedTransaction = rewardService.saveTransaction(request);
        //then
        assertThat(savedTransaction.getAmount()).isEqualTo(new BigDecimal("122.5"));
        assertThat(savedTransaction.getCustomerId()).isEqualTo("123");
        assertThat(savedTransaction.getId()).isNotNull();
    }

    @Test
    void shouldGetPointsWhenCorrectFromDate() {
        //given//when
        TotalRewardView totalReward = rewardService.getRewardPoints("123",
                LocalDateTime.now().minusMonths(1));
        //then
        assertThat(totalReward.getTotalPoints()).isEqualTo(400);
        assertThat(totalReward.getCustomerId()).isEqualTo("123");
    }

    @Test
    void shouldUpdateTransaction() {
        //given
        TransactionUpdateRequest request =
                new TransactionUpdateRequest(transactions.get(0).getId().toHexString(), new BigDecimal("5000"));
        //when
        TransactionView updatedTransaction = rewardService.updateTransaction(request);
        //then
        assertThat(updatedTransaction.getAmount()).isEqualTo(new BigDecimal("5000"));
        assertThat(updatedTransaction.getCustomerId()).isEqualTo("123");
        assertThat(updatedTransaction.getId()).isEqualTo(transactions.get(0).getId().toHexString());
    }

    @Test
    void shouldDeleteTransaction() {
        //given
        ObjectId id = transactions.get(0).getId();
        //when
        rewardService.delete(id.toHexString());
        //then
        assertThat(rewardRepository.findById(id)).isNotPresent();
    }
}

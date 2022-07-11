package com.example.reward.service;

import com.example.reward.converter.RewardTransactionToTotalRewardViewConverter;
import com.example.reward.converter.RewardTransactionToViewConverter;
import com.example.reward.exception.DomainException;
import com.example.reward.exception.ExceptionCode;
import com.example.reward.exception.NotFoundException;
import com.example.reward.exception.ValidationException;
import com.example.reward.model.TotalRewardView;
import com.example.reward.model.Transaction;
import com.example.reward.model.TransactionRequest;
import com.example.reward.model.TransactionUpdateRequest;
import com.example.reward.model.TransactionView;
import com.example.reward.repository.RewardRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.util.Pair;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RewardServiceTest {
    private static final LocalDateTime DEFAULT_LOCAL_DATE_TIME = LocalDateTime.of(2022, 7, 7, 15, 0);
    private final RewardRepository rewardRepository = mock(RewardRepository.class);
    private final RewardTransactionToTotalRewardViewConverter rewardTransactionToTotalRewardViewConverter =
            mock(RewardTransactionToTotalRewardViewConverter.class);
    private final RewardTransactionToViewConverter rewardTransactionToViewConverter =
            mock(RewardTransactionToViewConverter.class);
    private final CalculationService calculationService = mock(CalculationService.class);
    private final RewardService rewardService = new RewardService(rewardRepository,
            rewardTransactionToTotalRewardViewConverter,
            rewardTransactionToViewConverter,
            calculationService);

    @Test
    void shouldSaveTransactionAndReturnView() {
        //given
        TransactionRequest request = new TransactionRequest("123", new BigDecimal("122.5"));
        Transaction transaction = buildTransaction(new BigDecimal("122.5"),
                "123");
        TransactionView transactionView = new TransactionView(transaction.getId().toHexString(),
                new BigDecimal("122.5"),
                "123",
                DEFAULT_LOCAL_DATE_TIME,
                DEFAULT_LOCAL_DATE_TIME);
        //when
        when(rewardRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(rewardTransactionToViewConverter.convert(transaction)).thenReturn(transactionView);
        TransactionView savedTransaction = rewardService.saveTransaction(request);
        //then
        assertThat(savedTransaction).isEqualTo(transactionView);
    }

    @Test
    void shouldThrowValidationExceptionWhenSaveTransactionWithBlankCustomerId() {
        //given//when//then
        assertThrows(ValidationException.class,
                () -> rewardService.saveTransaction(new TransactionRequest(" ", new BigDecimal("122.5"))),
                ExceptionCode.INCORRECT_CUSTOMER_ID.getMessage());
    }

    @Test
    void shouldGetTransactionAndCalculatePointsWhenCorrectFromDate() {
        //given
        List<Transaction>
                transactions = List.of(buildTransaction(new BigDecimal("150"), "123"),
                buildTransaction(new BigDecimal("200"), "123"),
                buildTransaction(new BigDecimal("50"), "123"));
        TotalRewardView totalRewardView = new TotalRewardView("123", 250);
        //when
        when(rewardRepository.findAllByCustomerIdAndCreatedDateTimeAfter("123",
                LocalDateTime.of(2022, 6, 7, 15, 0)))
                .thenReturn(transactions);
        when(calculationService.calculateTotalReward(transactions)).thenReturn(250);
        when(rewardTransactionToTotalRewardViewConverter.convert(Pair.of("123", 250))).thenReturn(totalRewardView);
        TotalRewardView totalReward = rewardService.getRewardPoints("123",
                LocalDateTime.of(2022, 6, 7, 15, 0));
        //then
        assertThat(totalReward).isEqualTo(totalRewardView);
    }

    @Test
    void shouldThrowDomainExceptionWhenGetTransactionWithIncorrectFromDate() {
        //given//when//then
        assertThrows(DomainException.class,
                () -> rewardService.getRewardPoints("123", LocalDateTime.of(2022, 3, 7, 15, 0)),
                ExceptionCode.MAX_THREE_MONTH_LIMIT.getMessage());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdateTransactionAndNotExist() { //TODO think about better name
        //given
        TransactionUpdateRequest request =
                new TransactionUpdateRequest(new ObjectId().toHexString(), new BigDecimal("20"));
        // when
        when(rewardRepository.findById(new ObjectId(request.getId()))).thenReturn(Optional.empty());
        // then
        assertThrows(NotFoundException.class,
                () -> rewardService.updateTransaction(request),
                ExceptionCode.NOT_FOUND.getMessage());
    }

    @Test
    void shouldUpdateTransactionWhenTransactionExist() {
        //given
        TransactionUpdateRequest request =
                new TransactionUpdateRequest(new ObjectId().toHexString(), new BigDecimal("20"));
        Transaction transaction = buildTransaction(new BigDecimal("500"), "123");
        TransactionView transactionView = new TransactionView(transaction.getId().toHexString(),
                new BigDecimal("20"),
                "123",
                DEFAULT_LOCAL_DATE_TIME,
                DEFAULT_LOCAL_DATE_TIME);
        Transaction savedTransaction = new Transaction(new BigDecimal("20"), "123",
                DEFAULT_LOCAL_DATE_TIME,
                LocalDateTime.of(2022, 8, 7, 15, 0));
        // when
        when(rewardRepository.findById(new ObjectId(request.getId()))).thenReturn(Optional.of(transaction));
        when(rewardRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        when(rewardTransactionToViewConverter.convert(savedTransaction)).thenReturn(transactionView);
        TransactionView updatedTransaction = rewardService.updateTransaction(request);
        // then
        assertThat(updatedTransaction).isEqualTo(transactionView);
    }

    @Test
    void shouldDeleteTransactionWhenTransactionExist() {
        //given
        ArgumentCaptor<ObjectId> objectIdArgumentCaptor = ArgumentCaptor.forClass(ObjectId.class);
        Transaction transaction = buildTransaction(new BigDecimal("500"), "123");
        ObjectId objectId = new ObjectId();
        // when
        when(rewardRepository.findById(objectId)).thenReturn(Optional.of(transaction));
        rewardService.delete(objectId.toHexString());
        // then
        verify(rewardRepository).deleteById(objectIdArgumentCaptor.capture());
        assertThat(objectIdArgumentCaptor.getValue()).isEqualTo(objectId);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDeleteNonExistTransaction() {
        //given
        ObjectId objectId = new ObjectId();
        // when
        when(rewardRepository.findById(objectId)).thenReturn(Optional.empty());
        // then
        assertThrows(NotFoundException.class,
                () -> rewardService.delete(objectId.toHexString()),
                ExceptionCode.NOT_FOUND.getMessage());
    }

    private Transaction buildTransaction(BigDecimal amount, String customerId) {
        return new Transaction(amount,
                customerId,
                LocalDateTime.of(2022, 7, 7, 15, 0),
                LocalDateTime.of(2022, 7, 7, 15, 0));
    }
}
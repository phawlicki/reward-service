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
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RewardService {
    private static final Logger LOG = LoggerFactory.getLogger(RewardService.class);
    private final RewardRepository rewardRepository;
    private final RewardTransactionToTotalRewardViewConverter rewardTransactionToTotalRewardViewConverter;
    private final RewardTransactionToViewConverter rewardTransactionToViewConverter;
    private final CalculationService calculationService;

    public RewardService(RewardRepository rewardRepository,
            RewardTransactionToTotalRewardViewConverter rewardTransactionToTotalRewardViewConverter,
            RewardTransactionToViewConverter rewardTransactionToViewConverter,
            CalculationService calculationService) {
        this.rewardRepository = rewardRepository;
        this.rewardTransactionToTotalRewardViewConverter = rewardTransactionToTotalRewardViewConverter;
        this.rewardTransactionToViewConverter = rewardTransactionToViewConverter;
        this.calculationService = calculationService;
    }

    public TransactionView saveTransaction(TransactionRequest transactionRequest) {
        if (StringUtils.isNoneBlank(transactionRequest.getCustomerId())) {
            Transaction transaction =
                    new Transaction(transactionRequest.getAmount(),
                            transactionRequest.getCustomerId(),
                            LocalDateTime.now(),
                            LocalDateTime.now());
            LOG.debug("Saving transaction to database");
            return rewardTransactionToViewConverter.convert(rewardRepository.save(transaction));
        }
        LOG.error("Customer ID cannot be blank {}", transactionRequest.getCustomerId());
        throw new ValidationException(ExceptionCode.INCORRECT_CUSTOMER_ID);
    }

    public TotalRewardView getRewardPoints(String customerId, LocalDateTime from) {
        if (from.isBefore(LocalDateTime.now().minusMonths(3))) {
            LOG.error("You cannot get total points for more than 3 months {}", from);
            throw new DomainException(ExceptionCode.MAX_THREE_MONTH_LIMIT);
        }
        LOG.debug("Retrieving transaction from database");
        List<Transaction>
                transactions = rewardRepository.findAllByCustomerIdAndCreatedDateTimeAfter(customerId, from);
        Integer totalReward = calculationService.calculateTotalReward(transactions);
        return rewardTransactionToTotalRewardViewConverter.convert(Pair.of(customerId, totalReward));
    }

    public TransactionView updateTransaction(TransactionUpdateRequest transactionUpdateRequest) {
        Optional<Transaction> rewardTransaction =
                rewardRepository.findById(new ObjectId(transactionUpdateRequest.getId()));
        if (!rewardTransaction.isPresent()) {
            LOG.error("No data found for given id {}", transactionUpdateRequest.getId());
            throw new NotFoundException(ExceptionCode.NOT_FOUND);
        }
        LOG.debug("Saving transaction to database");
        return rewardTransactionToViewConverter.convert(rewardRepository
                .save(updateRewardTransaction(rewardTransaction.get(), transactionUpdateRequest.getAmount())));
    }

    public void delete(String id) {
        Optional<Transaction> rewardTransaction = rewardRepository.findById(new ObjectId(id));
        if (!rewardTransaction.isPresent()) {
            LOG.error("No data found for given id {}", id);
            throw new NotFoundException(ExceptionCode.NOT_FOUND);
        }
        LOG.debug("Deleting transaction from database");
        rewardRepository.deleteById(new ObjectId(id));
    }

    private Transaction updateRewardTransaction(Transaction transactionToUpdate,
            BigDecimal transactionAmount) {
        transactionToUpdate.setAmount(transactionAmount);
        transactionToUpdate.setLastUpdateDateTime(LocalDateTime.now());
        return transactionToUpdate;
    }
}

package com.example.reward.api;

import com.example.reward.model.TotalRewardView;
import com.example.reward.model.TransactionRequest;
import com.example.reward.model.TransactionUpdateRequest;
import com.example.reward.model.TransactionView;
import com.example.reward.service.RewardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@RestController
@Validated
public class RewardController {
    private static final Logger LOG = LoggerFactory.getLogger(RewardController.class);
    private final RewardService rewardService;

    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @GetMapping("/reward/points")
    public TotalRewardView getPoints(@RequestParam @NotBlank String customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @NotNull LocalDateTime from) {
        LOG.debug("Getting total points for customerId {} and fromDate {}", customerId, from);
        return rewardService.getRewardPoints(customerId, from);
    }

    @PostMapping("/reward/save")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionView saveTransaction(@RequestBody TransactionRequest transactionRequest) {
        LOG.debug("Saving transaction with request {}", transactionRequest);
        return rewardService.saveTransaction(transactionRequest);
    }

    @PutMapping("/reward/update")
    public TransactionView updateTransaction(@RequestBody TransactionUpdateRequest transactionUpdateRequest) {
        LOG.debug("Updating transaction with request {}", transactionUpdateRequest);
        return rewardService.updateTransaction(transactionUpdateRequest);
    }

    @DeleteMapping("/reward/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTransaction(@RequestParam @NotBlank String id) {
        LOG.debug("Deleting transaction with id {}", id);
        rewardService.delete(id);
    }
}

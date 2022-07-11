package com.example.reward.converter;


import com.example.reward.model.Transaction;
import com.example.reward.model.TransactionView;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RewardTransactionToViewConverter implements Converter<Transaction, TransactionView> {
    @Override
    public TransactionView convert(Transaction source) {
        return new TransactionView(source.getId().toHexString(),
                source.getAmount(),
                source.getCustomerId(),
                source.getCreatedDateTime(),
                source.getLastUpdateDateTime());
    }
}

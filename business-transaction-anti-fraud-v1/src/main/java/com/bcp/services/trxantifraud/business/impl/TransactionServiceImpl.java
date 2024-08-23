package com.bcp.services.trxantifraud.business.impl;

import com.bcp.services.trxantifraud.business.TransactionService;
import com.bcp.services.trxantifraud.config.ApplicationProperties;
import com.bcp.services.trxantifraud.model.AnalysisResult;
import com.bcp.services.trxantifraud.model.RawTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Transaction Service Implementation.<br/>
 */
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    public static final String REASON = "Transaction amount exceeds the maximum allowed amount";
    private final ApplicationProperties applicationProperties;

    @Override
    public Mono<AnalysisResult> analyzeTrx(final Mono<RawTransaction> transaction) {

        return transaction.filter(trx -> trx.getAmount()
                        .compareTo(applicationProperties.getMaximumTransactionAmount()) <= 0)
                .map(this::createLegitimateResult)
                .switchIfEmpty(transaction.map(this::createFraudulentResult));
    }

    private AnalysisResult createLegitimateResult(final RawTransaction trx) {
        return AnalysisResult.builder()
                .isFraudulent(false)
                .transactionId(trx.getId())
                .build();
    }

    private AnalysisResult createFraudulentResult(final RawTransaction trx) {
        return AnalysisResult.builder()
                .isFraudulent(true)
                .transactionId(trx.getId())
                .reason(REASON)
                .build();
    }

}

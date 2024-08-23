package com.bcp.services.transaction.business;

import com.bcp.services.transaction.model.AnalysisResult;
import com.bcp.services.transaction.trx.model.GetTransactionResponse;
import com.bcp.services.transaction.trx.model.TransactionRequest;
import com.bcp.services.transaction.trx.model.TransactionResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Transaction Service.<br/>
 */
public interface TransactionService {

    Mono<TransactionResponse> createTrx(Mono<TransactionRequest> transaction);
    Mono<GetTransactionResponse> getTrx(UUID transactionExternalId);
    Mono<Void> updateTrx(AnalysisResult transaction);
}

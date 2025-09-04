package com.bcp.services.trxantifraud.business;

import com.bcp.services.trxantifraud.model.AnalysisResult;
import com.bcp.services.trxantifraud.model.RawTransaction;
import reactor.core.publisher.Mono;

/**
 * Transaction Service.
 * This interface is used to represent a TransactionService.
 */
public interface TransactionService {

    Mono<AnalysisResult> analyzeTrx(Mono<RawTransaction> transaction);

}

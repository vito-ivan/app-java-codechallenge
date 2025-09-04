package com.bcp.services.transaction.expose.web;

import com.bcp.services.transaction.business.TransactionService;
import com.bcp.services.transaction.trx.api.TransactionApiDelegate;
import com.bcp.services.transaction.trx.model.GetTransactionResponse;
import com.bcp.services.transaction.trx.model.TransactionRequest;
import com.bcp.services.transaction.trx.model.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * TransactionApiImpl.
 * This class implements the TransactionApiDelegate interface.
 */
@RequiredArgsConstructor
@Component
public class TransactionApiImpl implements TransactionApiDelegate {

    private final TransactionService transactionService;

    @Override
    public Mono<ResponseEntity<TransactionResponse>> createTransaction(final UUID requestID,
                                                                       final java.time.LocalDateTime requestDate,
                                                                       final String callerName,
                                                                       final Mono<TransactionRequest> transactionRequest,
                                                                       final ServerWebExchange exchange) {

        return transactionService.createTrx(transactionRequest)
                .map(response -> new ResponseEntity<>(response, org.springframework.http.HttpStatus.CREATED));

    }

    @Override
    public Mono<ResponseEntity<GetTransactionResponse>> getTransaction(final UUID requestID,
                                                                       final java.time.LocalDateTime requestDate,
                                                                       final String callerName,
                                                                       final UUID transactionExternalId,
                                                                       final ServerWebExchange exchange) {

        return transactionService.getTrx(transactionExternalId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }

}




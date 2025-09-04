package com.bcp.services.transaction.expose.web;

import com.bcp.services.transaction.business.TransactionService;
import com.bcp.services.transaction.trx.model.GetTransactionResponse;
import com.bcp.services.transaction.trx.model.TransactionRequest;
import com.bcp.services.transaction.trx.model.TransactionResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionApiImplTest {

    public static final UUID REQUEST_ID = UUID.randomUUID();
    public static final LocalDateTime REQUEST_DATE = LocalDateTime.now();
    public static final String CALLER_NAME = "testCaller";
    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionApiImpl transactionApiImpl;

    @Test
    @DisplayName("Return http status 201 when transaction is saved")
    void returnHttpStatus201WhenTransactionIsSaved() {
        Mono<TransactionRequest> transactionRequest = Mono.just(new TransactionRequest());
        TransactionResponse transactionResponse = new TransactionResponse();

        when(transactionService.createTrx(any(Mono.class))).thenReturn(Mono.just(transactionResponse));

        Mono<ResponseEntity<TransactionResponse>> result = transactionApiImpl
                .createTransaction(REQUEST_ID, REQUEST_DATE, CALLER_NAME, transactionRequest, null);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    Assertions.assertNotNull(responseEntity);
                    Assertions.assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
                    Assertions.assertEquals(transactionResponse, responseEntity.getBody());
                })
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Return http status 200 when transaction is found")
    void returnHttpStatus200WhenTransactionIsFound() {
        UUID transactionExternalId = UUID.randomUUID();
        GetTransactionResponse getTransactionResponse = new GetTransactionResponse();

        when(transactionService.getTrx(any(UUID.class))).thenReturn(Mono.just(getTransactionResponse));

        Mono<ResponseEntity<GetTransactionResponse>> result = transactionApiImpl
                .getTransaction(REQUEST_ID, REQUEST_DATE, CALLER_NAME, transactionExternalId, null);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    Assertions.assertNotNull(responseEntity);
                    Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
                    Assertions.assertEquals(getTransactionResponse, responseEntity.getBody());
                })
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Return http status 204 when transaction is not found")
    void returnHttpStatus204WhenTransactionIsNotFound() {
        UUID transactionExternalId = UUID.randomUUID();

        when(transactionService.getTrx(any(UUID.class))).thenReturn(Mono.empty());

        Mono<ResponseEntity<GetTransactionResponse>> result = transactionApiImpl
                .getTransaction(REQUEST_ID, REQUEST_DATE, CALLER_NAME, transactionExternalId, null);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    Assertions.assertNotNull(responseEntity);
                    Assertions.assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
                })
                .expectComplete()
                .verify();
    }

}
package com.bcp.services.transaction.business.impl;

import com.bcp.services.transaction.business.ReactiveKafkaProducer;
import com.bcp.services.transaction.config.ApplicationProperties;
import com.bcp.services.transaction.config.core.utils.exception.ApiException;
import com.bcp.services.transaction.config.core.utils.utils.PropertyUtils;
import com.bcp.services.transaction.model.TransactionEntity;
import com.bcp.services.transaction.trx.model.TransactionRequest;
import com.bcp.services.transaction.trx.model.TransactionResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.core.env.Environment;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.core.ReactiveInsertOperation;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaveTransactionServiceImplTest {

    public static final ApplicationProperties.TrxStatuses TRX_STATUSES = ApplicationProperties.TrxStatuses.builder()
            .pending("PENDING")
            .approved("APPROVED")
            .rejected("REJECTED")
            .build();
    @Mock
    private Environment environment;

    @Mock
    private R2dbcEntityTemplate entityTemplate;

    @Mock
    private ReactiveKafkaProducer reactiveKafkaProducer;

    @Mock
    private ApplicationProperties applicationProperties;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private TransactionRequest transactionRequest;
    private TransactionEntity transactionEntity;

    @Mock
    private ReactiveInsertOperation.ReactiveInsert<TransactionEntity> insertMock;

    @Captor
    private ArgumentCaptor<String> topicCaptor;

    @Captor
    private ArgumentCaptor<String> keyCaptor;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        transactionRequest = new TransactionRequest()
                .accountExternalIdDebit(UUID.randomUUID())
                .accountExternalIdCredit(UUID.randomUUID())
                .transactionTypeCode("220")
                .amount(new BigDecimal("100.00"));

        transactionEntity = TransactionEntity.builder()
                .id(UUID.randomUUID().toString())
                .accountExternalIdDebit(transactionRequest.getAccountExternalIdDebit().toString())
                .accountExternalIdCredit(transactionRequest.getAccountExternalIdCredit().toString())
                .transactionTypeCode(transactionRequest.getTransactionTypeCode())
                .amount(transactionRequest.getAmount())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        ReflectionTestUtils.setField(transactionService, "topic", "transaction-anti-fraud-validation");

        when(applicationProperties.getTrxStatuses()).thenReturn(TRX_STATUSES);


    }

    @Test
    @DisplayName("Return transaction when it is saved and sent for anti-fraud validation")
    void returnTransactionWhenItIsSavedAndSentForAntiFraudValidation() throws JSONException, JsonProcessingException {

        when(entityTemplate.insert(TransactionEntity.class)).thenReturn(insertMock);
        when(insertMock.using(any(TransactionEntity.class))).thenReturn(Mono.just(transactionEntity));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"id\":null,\"amount\":100.00,\"currency\":null}");
        when(reactiveKafkaProducer.sendMessage(any(), any(), any())).thenReturn(Mono.empty());

        Mono<TransactionResponse> result = transactionService.createTrx(Mono.just(transactionRequest));

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getTransactionExternalId()
                        .equals(UUID.fromString(transactionEntity.getId())))
                .verifyComplete();

        verify(reactiveKafkaProducer).sendMessage(topicCaptor.capture(), keyCaptor.capture(), messageCaptor.capture());

        assertEquals("transaction-anti-fraud-validation", topicCaptor.getValue());
        assertEquals(36, keyCaptor.getValue().length());

        JSONObject expectedJson = new JSONObject("{\"id\":null,\"amount\":100.00,\"currency\":null}");
        JSONObject actualJson = new JSONObject(messageCaptor.getValue());

        expectedJson.remove("id");
        actualJson.remove("id");

        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.LENIENT);

        verify(reactiveKafkaProducer, times(1)).sendMessage(any(), any(), any());
    }

    @Test
    @DisplayName("Return transaction when it is saved and an error occurs while sending it for anti-fraud validation")
    void returnTransactionWhenItIsSavedAndAnErrorOccursWhileSendingItForAntiFraudValidation() throws JsonProcessingException {

        when(entityTemplate.insert(TransactionEntity.class)).thenReturn(insertMock);
        when(insertMock.using(any(TransactionEntity.class))).thenReturn(Mono.just(transactionEntity));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"id\":null,\"amount\":100.00,\"currency\":null}");
        doReturn(Mono.error(new RuntimeException("Kafka error")))
                .when(reactiveKafkaProducer).sendMessage(any(), any(), any());

        Mono<TransactionResponse> result = transactionService.createTrx(Mono.just(transactionRequest));

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getTransactionExternalId()
                        .equals(UUID.fromString(transactionEntity.getId())))
                .verifyComplete();

        verify(reactiveKafkaProducer).sendMessage(topicCaptor.capture(), keyCaptor.capture(), messageCaptor.capture());

        assertEquals("transaction-anti-fraud-validation", topicCaptor.getValue());
        assertEquals(36, keyCaptor.getValue().length());

        verify(reactiveKafkaProducer, times(1)).sendMessage(any(), any(), any());

    }

    @Test
    @DisplayName("Return transaction when it is saved and a JsonProcessingException occurs")
    void returnTransactionWhenItIsSavedAndAJsonProcessingExceptionOccurs() throws JsonProcessingException {

        PropertyUtils.setResolver(environment);

        //given(environment.getProperty("spring.application.name")).willReturn("business-transaction");
        given(environment.getProperty("application.api.error-code.unexpected.code")).willReturn("T0099");
        given(environment.getProperty("application.api.error-code.unexpected.description")).willReturn(
                "Internal server error");
        given(environment.getProperty("application.api.error-code.unexpected.error-type")).willReturn("Technical");

        when(applicationProperties.getTrxStatuses()).thenReturn(ApplicationProperties.TrxStatuses.builder()
                .pending("PENDING")
                .approved("APPROVED")
                .rejected("REJECTED")
                .build());

        when(entityTemplate.insert(TransactionEntity.class)).thenReturn(insertMock);
        when(insertMock.using(any(TransactionEntity.class))).thenReturn(Mono.just(transactionEntity));
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Error processing JSON") {
        });

        Mono<TransactionResponse> result = transactionService.createTrx(Mono.just(transactionRequest));

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getTransactionExternalId()
                        .equals(UUID.fromString(transactionEntity.getId())))
                .verifyComplete();

        verify(entityTemplate, times(1)).insert(TransactionEntity.class);
        verify(insertMock, times(1)).using(any(TransactionEntity.class));
        verify(objectMapper, times(1)).writeValueAsString(any());
        verify(reactiveKafkaProducer, times(0)).sendMessage(any(), any(), any());
    }

    @Test
    @DisplayName("Throw ApiException when error occurs while saving transaction")
    void throwApiExceptionWhenErrorOccursWhileSavingTransaction() throws JsonProcessingException {

        PropertyUtils.setResolver(environment);

        //given(environment.getProperty("spring.application.name")).willReturn("business-transaction");
        given(environment.getProperty("application.api.error-code.external-error.code")).willReturn("T0099");
        given(environment.getProperty("application.api.error-code.external-error.description")).willReturn(
                "Internal server error");
        given(environment.getProperty("application.api.error-code.external-error.error-type")).willReturn("Technical");

        when(entityTemplate.insert(TransactionEntity.class)).thenReturn(insertMock);
        when(insertMock.using(any(TransactionEntity.class)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        Mono<TransactionResponse> result = transactionService.createTrx(Mono.just(transactionRequest));

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> {
                    if (throwable instanceof ApiException apiException) {
                        return apiException.getExceptionDetails().get(0).getDescription().contains("Database error");
                    }
                    return false;
                })
                .verify();

        verify(entityTemplate, times(1)).insert(TransactionEntity.class);
        verify(insertMock, times(1)).using(any(TransactionEntity.class));
        verify(objectMapper, times(0)).writeValueAsString(any());
        verify(reactiveKafkaProducer, times(0)).sendMessage(any(), any(), any());
    }

}

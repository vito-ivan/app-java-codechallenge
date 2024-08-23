package com.bcp.services.transaction.business.impl;

import com.bcp.services.transaction.config.ApplicationProperties;
import com.bcp.services.transaction.config.core.utils.utils.PropertyUtils;
import com.bcp.services.transaction.model.TransactionEntity;
import com.bcp.services.transaction.repository.TransactionRepository;
import com.bcp.services.transaction.trx.model.GetTransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.test.annotation.DirtiesContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.sql.SQLDataException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class GetTransactionServiceImplTest {

    @Mock
    private Environment environment;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ApplicationProperties applicationProperties;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private UUID transactionExternalId;
    private TransactionEntity transactionEntity;

    @BeforeEach
    void setUp() {
        transactionExternalId = UUID.randomUUID();
        transactionEntity = TransactionEntity.builder()
                .id(transactionExternalId.toString())
                .status("APPROVED")
                .transactionTypeCode("220")
                .amount(new BigDecimal("100.00"))
                .createdAt(LocalDateTime.parse("2021-08-01T00:00:00"))
                .updatedAt(LocalDateTime.parse("2021-08-01T00:00:00"))
                .build();
    }

    @Test
    @DisplayName("Return transaction when transaction is found")
    void returnTransactionWhenTransactionIsFound() {

        when(applicationProperties.getTrxTypes()).thenReturn(Map.of(
                "220", "Transferencias ordinaria",
                "225", "Pago a cta tarjeta",
                "320", "ORDINARIAS - INMEDIATAS",
                "325", "PAGO DE TARJETA DE CREDITO - INMEDIATAS"));
        when(transactionRepository.findById(anyString())).thenReturn(Mono.just(transactionEntity));

        Mono<GetTransactionResponse> result = transactionService.getTrx(transactionExternalId);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getTransactionExternalId().equals(transactionExternalId))
                .verifyComplete();
    }

    @Test
    @DisplayName("Throw ApiException when error occurs while obtaining transaction")
    void throwApiExceptionWhenErrorOccursWhileObtainingTransaction() {

        PropertyUtils.setResolver(environment);

        given(environment.getProperty("spring.application.name")).willReturn("business-transaction");
        given(environment.getProperty("application.api.error-code.external-error.code")).willReturn("T0099");
        given(environment.getProperty("application.api.error-code.external-error.description")).willReturn(
                "Internal server error");
        given(environment.getProperty("application.api.error-code.external-error.error-type")).willReturn("Technical");

        var exception = new BadSqlGrammarException("Database error", "SQL", new SQLDataException("Database error"));
        when(transactionRepository.findById(anyString())).thenReturn(Mono.error(exception));

        Mono<GetTransactionResponse> result = transactionService.getTrx(transactionExternalId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable.getMessage().contains("Internal server error"))
                .verify();
    }

    @Test
    @DisplayName("Return empty when transaction is not found")
    void returnEmptyWhenTransactionIsNotFound() {
        when(transactionRepository.findById(anyString())).thenReturn(Mono.empty());

        Mono<GetTransactionResponse> result = transactionService.getTrx(transactionExternalId);

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }
}

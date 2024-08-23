package com.bcp.services.trxantifraud.business.impl;

import com.bcp.services.trxantifraud.config.ApplicationProperties;
import com.bcp.services.trxantifraud.model.RawTransaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RawTransactionServiceImplTest {

    @Mock
    private ApplicationProperties applicationProperties;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    @DisplayName("Return a legitimate transaction when the transaction amount is less than the maximum allowed amount")
    void returnALegitimateTransactionWhenTheTransactionAmountIsLessThanTheMaximumAllowedAmount() {

        // Arrange
        when(applicationProperties.getMaximumTransactionAmount()).thenReturn(BigDecimal.valueOf(1000));

        var uuid = UUID.randomUUID().toString();
        var rawTransaction = Mono.just(RawTransaction.builder()
                .id(uuid)
                .amount(BigDecimal.valueOf(500))
                .build());

        // Act
        var result = transactionService.analyzeTrx(rawTransaction);

        // Assert
        StepVerifier.create(result)
                .assertNext(result1 -> {
                    Assertions.assertNotNull(result1);
                    Assertions.assertEquals(uuid, result1.getTransactionId());
                    Assertions.assertEquals(false, result1.getIsFraudulent());
                })
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Return a legitimate transaction when the transaction amount is equal to the maximum allowed amount")
    void returnALegitimateTransactionWhenTheTransactionAmountIsEqualToTheMaximumAllowedAmount() {

        // Arrange
        when(applicationProperties.getMaximumTransactionAmount()).thenReturn(BigDecimal.valueOf(1000));

        var uuid = UUID.randomUUID().toString();
        var rawTransaction = Mono.just(RawTransaction.builder()
                .id(uuid)
                .amount(BigDecimal.valueOf(1000))
                .build());

        // Act
        var result = transactionService.analyzeTrx(rawTransaction);

        // Assert
        StepVerifier.create(result)
                .assertNext(result1 -> {
                    Assertions.assertNotNull(result1);
                    Assertions.assertEquals(uuid, result1.getTransactionId());
                    Assertions.assertEquals(false, result1.getIsFraudulent());
                })
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Return a fraudulent transaction when the transaction amount exceeds the maximum allowed amount")
    void returnAFraudulentTransactionWhenTheTransactionAmountExceedsTheMaximumAllowedAmount() {

        // Arrange
        when(applicationProperties.getMaximumTransactionAmount()).thenReturn(BigDecimal.valueOf(1000));

        var uuid = UUID.randomUUID().toString();
        var rawTransaction = Mono.just(RawTransaction.builder()
                .id(uuid)
                .amount(BigDecimal.valueOf(1500))
                .build());

        // Act
        var result = transactionService.analyzeTrx(rawTransaction);

        // Assert
        StepVerifier.create(result)
                .assertNext(result1 -> {
                    Assertions.assertNotNull(result1);
                    Assertions.assertEquals(uuid, result1.getTransactionId());
                    Assertions.assertEquals(true, result1.getIsFraudulent());
                    Assertions.assertEquals("Transaction amount exceeds the maximum allowed amount", result1.getReason());
                })
                .expectComplete()
                .verify();
    }
}
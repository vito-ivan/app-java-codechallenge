package com.bcp.services.transaction.business.impl;

import com.bcp.services.transaction.config.ApplicationProperties;
import com.bcp.services.transaction.config.core.utils.exception.ApiException;
import com.bcp.services.transaction.config.core.utils.utils.PropertyUtils;
import com.bcp.services.transaction.model.AnalysisResult;
import com.bcp.services.transaction.model.TransactionEntity;
import com.bcp.services.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateTransactionServiceImplTest {

    public static final ApplicationProperties.TrxStatuses TRX_STATUSES = ApplicationProperties.TrxStatuses.builder()
            .pending("PENDING")
            .approved("APPROVED")
            .rejected("REJECTED")
            .build();

    @Mock
    private Environment environment;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ApplicationProperties applicationProperties;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private AnalysisResult analysisResult;
    private TransactionEntity transactionEntity;

    @BeforeEach
    void setUp() {
        analysisResult = AnalysisResult.builder()
                .transactionId(UUID.randomUUID().toString())
                .isFraudulent(false)
                .build();

        transactionEntity = TransactionEntity.builder()
                .id(analysisResult.getTransactionId())
                .accountExternalIdDebit(UUID.randomUUID().toString())
                .accountExternalIdCredit(UUID.randomUUID().toString())
                .transactionTypeCode("220")
                .amount(new BigDecimal("100.00"))
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    class SuccessScenarios {

        @Test
        @DisplayName("Successfully update transaction when it is not fraudulent, found, and updated")
        void successfullyUpdateTransactionWhenItIsNotFraudulentFoundAndUpdated() {
            when(transactionRepository.findById(anyString())).thenReturn(Mono.just(transactionEntity));
            when(applicationProperties.getTrxStatuses()).thenReturn(TRX_STATUSES);
            when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(Mono.just(transactionEntity));

            Mono<Void> result = transactionService.updateTrx(analysisResult);

            StepVerifier.create(result)
                    .verifyComplete();

            verify(transactionRepository, times(1)).findById(anyString());
            verify(transactionRepository, times(1)).save(any(TransactionEntity.class));
        }

        @Test
        @DisplayName("Successfully update transaction when it is fraudulent, found, and updated")
        void successfullyUpdateTransactionWhenItIsFraudulentFoundAndUpdated() {
            analysisResult = AnalysisResult.builder()
                    .transactionId(UUID.randomUUID().toString())
                    .isFraudulent(true)
                    .build();

            when(transactionRepository.findById(anyString())).thenReturn(Mono.just(transactionEntity));
            when(applicationProperties.getTrxStatuses()).thenReturn(TRX_STATUSES);
            when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(Mono.just(transactionEntity));

            Mono<Void> result = transactionService.updateTrx(analysisResult);

            StepVerifier.create(result)
                    .verifyComplete();

            verify(transactionRepository, times(1)).findById(anyString());
            verify(transactionRepository, times(1)).save(any(TransactionEntity.class));
        }
    }

    @Nested
    class FailureScenarios {

        @Test
        @DisplayName("Throw ApiException when transaction not found")
        void throwApiExceptionWhenTransactionNotFound() {

            PropertyUtils.setResolver(environment);

            //doReturn("business-transaction").when(environment).getProperty("spring.application.name");
            doReturn("T0099").when(environment).getProperty("application.api.error-code.conflict.code");
            doReturn("conflict").when(environment).getProperty("application.api.error-code.conflict.description");
            doReturn("Functional").when(environment).getProperty("application.api.error-code.conflict.error-type");

            when(transactionRepository.findById(anyString())).thenReturn(Mono.empty());

            Mono<Void> result = transactionService.updateTrx(analysisResult);

            StepVerifier.create(result)
                    .expectErrorMatches(throwable -> throwable instanceof ApiException)
                    .verify();

            verify(transactionRepository, times(1)).findById(anyString());
            verify(transactionRepository, times(0)).save(any(TransactionEntity.class));
        }

        @Test
        @DisplayName("Throw ApiException when error occurs while finding transaction")
        void throwApiExceptionWhenErrorOccursWhileFindingTransaction() {

            PropertyUtils.setResolver(environment);

            //doReturn("business-transaction").when(environment).getProperty("spring.application.name");
            doReturn("T0099").when(environment).getProperty("application.api.error-code.external-error.code");
            doReturn("Internal server error").when(environment).getProperty("application.api.error-code.external-error.description");
            doReturn("Technical").when(environment).getProperty("application.api.error-code.external-error.error-type");

            when(transactionRepository.findById(anyString()))
                    .thenReturn(Mono.error(new RuntimeException("Database error")));

            Mono<Void> result = transactionService.updateTrx(analysisResult);

            StepVerifier.create(result)
                    .expectErrorMatches(throwable -> throwable instanceof ApiException)
                    .verify();

            verify(transactionRepository, times(1)).findById(anyString());
            verify(transactionRepository, times(0)).save(any(TransactionEntity.class));
        }

        @Test
        @DisplayName("Throw ApiException when error occurs while updating transaction")
        void throwApiExceptionWhenErrorOccursWhileUpdatingTransaction() {

            PropertyUtils.setResolver(environment);

            //doReturn("business-transaction").when(environment).getProperty("spring.application.name");
            doReturn("T0099").when(environment).getProperty("application.api.error-code.external-error.code");
            doReturn("Internal server error").when(environment).getProperty("application.api.error-code.external-error.description");
            doReturn("Technical").when(environment).getProperty("application.api.error-code.external-error.error-type");

            when(transactionRepository.findById(anyString())).thenReturn(Mono.just(transactionEntity));
            when(applicationProperties.getTrxStatuses()).thenReturn(TRX_STATUSES);
            when(transactionRepository.save(any(TransactionEntity.class)))
                    .thenReturn(Mono.error(new RuntimeException("Database error")));

            Mono<Void> result = transactionService.updateTrx(analysisResult);

            StepVerifier.create(result)
                    .expectErrorMatches(throwable -> throwable instanceof ApiException)
                    .verify();

            verify(transactionRepository, times(1)).findById(anyString());
            verify(transactionRepository, times(1)).save(any(TransactionEntity.class));
        }

    }
}
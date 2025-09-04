package com.bcp.services.transaction.business.impl;

import com.bcp.services.transaction.business.ReactiveKafkaProducer;
import com.bcp.services.transaction.business.TransactionService;
import com.bcp.services.transaction.config.ApplicationProperties;
import com.bcp.services.transaction.model.AnalysisResult;
import com.bcp.services.transaction.model.RawTransaction;
import com.bcp.services.transaction.model.TransactionEntity;
import com.bcp.services.transaction.repository.TransactionRepository;
import com.bcp.services.transaction.trx.model.GetTransactionResponse;
import com.bcp.services.transaction.trx.model.TransactionRequest;
import com.bcp.services.transaction.trx.model.TransactionResponse;
import com.bcp.services.transaction.trx.model.TransactionStatusResponse;
import com.bcp.services.transaction.trx.model.TransactionTypeResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.bcp.services.transaction.util.exception.CustomApiException.C4091;
import static com.bcp.services.transaction.util.exception.CustomApiException.C5001;
import static com.bcp.services.transaction.util.exception.CustomApiException.C5003;
import static com.bcp.services.transaction.util.exception.ExceptionUtils.buildApiExceptionFromPostgresqlThrowable;

/**
 * Transaction Service Implementation.<br/>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    @Value("${application.kafka.producer.topic}")
    private String topic;

    private final TransactionRepository transactionRepository;
    private final R2dbcEntityTemplate entityTemplate;
    private final ReactiveKafkaProducer reactiveKafkaProducer;
    private final ApplicationProperties applicationProperties;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<TransactionResponse> createTrx(final Mono<TransactionRequest> requestMono) {

        return requestMono
                .map(this::buildTransactionEntity)
                .flatMap(this::saveTransaction)
                .flatMap(transactionEntity -> sendMessage(transactionEntity)
                        .then(Mono.just(transactionEntity)))
                .map(this::buildTransactionResponse);
    }

    private TransactionEntity buildTransactionEntity(final TransactionRequest request) {
        return TransactionEntity.builder()
                .id(UUID.randomUUID().toString())
                .accountExternalIdDebit(request.getAccountExternalIdDebit().toString())
                .accountExternalIdCredit(request.getAccountExternalIdCredit().toString())
                .transactionTypeCode(request.getTransactionTypeCode())
                .amount(request.getAmount())
                .status(applicationProperties.getTrxStatuses().getPending())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Mono<TransactionEntity> saveTransaction(final TransactionEntity transactionEntity) {
        return entityTemplate
                .insert(TransactionEntity.class)
                .using(transactionEntity)
                .doOnError((Throwable ex) -> log.error("Error saving transaction", ex))
                .onErrorResume((Throwable ex) -> Mono.error(buildApiExceptionFromPostgresqlThrowable(C5001, ex)));
    }

    private Mono<Void> sendMessage(final TransactionEntity entity) {
        return convertTransactionToJson(entity)
                .flatMap(trxJson -> reactiveKafkaProducer.sendMessage(topic, UUID.randomUUID().toString(), trxJson)
                        .doOnError(ex -> log.error("Error sending message to Kafka", ex)))
                .onErrorResume(ex -> Mono.empty());
    }

    private Mono<String> convertTransactionToJson(final TransactionEntity entity) {
        var rawTransaction = RawTransaction.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .build();

        try {
            return Mono.just(objectMapper.writeValueAsString(rawTransaction));
        } catch (JsonProcessingException ex) {
            log.error("Error processing transaction", ex);
            return Mono.error(C5003.getException(ex));
        }
    }

    private TransactionResponse buildTransactionResponse(final TransactionEntity entity) {
        return new TransactionResponse()
                .transactionExternalId(UUID.fromString(entity.getId()));
    }

    @Override
    public Mono<GetTransactionResponse> getTrx(final UUID transactionExternalId) {
        return transactionRepository
                .findById(transactionExternalId.toString())
                .map(this::buildGetTransactionResponse)
                .doOnError((Throwable ex) -> log.error("Error obtaining transaction", ex))
                .onErrorResume((Throwable ex) ->
                        Mono.error(buildApiExceptionFromPostgresqlThrowable(C5001, ex)));
    }

    private GetTransactionResponse buildGetTransactionResponse(final TransactionEntity entity) {
        return new GetTransactionResponse()
                .transactionExternalId(UUID.fromString(entity.getId()))
                .transactionType(new TransactionTypeResponse()
                        .code(entity.getTransactionTypeCode())
                        .name(applicationProperties.getTrxTypes().get(entity.getTransactionTypeCode())))
                .amount(entity.getAmount())
                .transactionStatus(new TransactionStatusResponse()
                        .name(entity.getStatus()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());
    }

    @Override
    public Mono<Void> updateTrx(final AnalysisResult analysisResult) {
        return transactionRepository
                .findById(analysisResult.getTransactionId())
                .switchIfEmpty(Mono.defer(() -> Mono.error(C4091.getException())))
                .flatMap(transactionEntity -> updateTransactionEntity(transactionEntity, analysisResult))
                .onErrorMap(this::handleError)
                .then();
    }

    private Mono<TransactionEntity> updateTransactionEntity(final TransactionEntity transactionEntity,
                                                            final AnalysisResult analysisResult) {

        String status = analysisResult.getIsFraudulent() ? applicationProperties
                .getTrxStatuses().getRejected() : applicationProperties.getTrxStatuses().getApproved();

        var trx = transactionEntity.toBuilder()
                .status(status)
                .updatedAt(LocalDateTime.now())
                .build();
        return transactionRepository.save(trx)
                .doOnError(ex -> log.error("Error updating transaction in PostgresSQL: {}", ex.getMessage()));
    }

    private Throwable handleError(final Throwable ex) {
        log.error("Error processing transaction update: {}", ex.getMessage());
        return buildApiExceptionFromPostgresqlThrowable(C5001, ex);
    }

}

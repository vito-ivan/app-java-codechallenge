package com.bcp.services.trxantifraud.business;

import com.bcp.services.trxantifraud.model.AnalysisResult;
import com.bcp.services.trxantifraud.model.RawTransaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.UUID;

/**
 * Reactive Kafka Consumer.<br/>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReactiveKafkaConsumer {

    @Value("${application.kafka.producer.topic}")
    private String topic;

    private final KafkaReceiver<String, String> kafkaReceiver;
    private final TransactionService transactionService;
    private final ReactiveKafkaProducer reactiveKafkaProducer;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void initialize() {
        startConsuming();
    }

    private void startConsuming() {

        kafkaReceiver.receive()
                .doOnNext(this::processMessage)
                .flatMap(this::processRecord)
                .subscribe();
    }

    private void processMessage(final ConsumerRecord<String, String> record) {
        log.info("Message received: key = {}, value = {}", record.key(), record.value());
    }

    private Mono<Void> processRecord(final ReceiverRecord<String, String> record) {
        return Mono.just(record)
                .map(this::getRawTransaction)
                .flatMap(this::analyzeRawTransaction)
                .flatMap(this::sendAnalysisResult)
                .doOnError(error -> log.error("Error processing record: ", error))
                .then(acknowledgeRecord(record));
    }

    private RawTransaction getRawTransaction(final ReceiverRecord<String, String> record) {
        try {
            return objectMapper.readValue(record.value(), RawTransaction.class);
        } catch (JsonProcessingException ex) {
            log.error("Error parsing message: ", ex);
            throw new RuntimeException(ex);
        }
    }

    private Mono<AnalysisResult> analyzeRawTransaction(final RawTransaction transaction) {
        return transactionService.analyzeTrx(Mono.just(transaction))
                .doOnError(error -> log.error("Error analyzing transaction: ", error));
    }

    private Mono<Void> sendAnalysisResult(final AnalysisResult analysisResult) {
        String analysisResultJson = getResultJson(analysisResult);
        return reactiveKafkaProducer.sendMessage(topic, UUID.randomUUID().toString(), analysisResultJson)
                .doOnError(error -> log.error("Error sending message: ", error));
    }

    private Mono<Void> acknowledgeRecord(final ReceiverRecord<String, String> record) {
        return Mono.fromRunnable(record.receiverOffset()::acknowledge)
                .doOnError(error -> log.error("Error acknowledging message: ", error))
                .then();
    }

    private String getResultJson(final AnalysisResult analysisResult) {
        try {
            return objectMapper.writeValueAsString(analysisResult);
        } catch (JsonProcessingException ex) {
            log.error("Error parsing message: ", ex);
            throw new RuntimeException(ex);
        }
    }

}

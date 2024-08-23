package com.bcp.services.transaction.business;

import com.bcp.services.transaction.model.AnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;

/**
 * Reactive Kafka Consumer.<br/>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReactiveKafkaConsumer {

    private final KafkaReceiver<String, String> kafkaReceiver;
    private final TransactionService transactionService;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void initialize() {
        startConsuming();
    }

    private void startConsuming() {

        kafkaReceiver.receive()
                .doOnNext(this::processMessage)
                .flatMap(record -> Mono.fromCallable(() -> objectMapper.readValue(record.value(), AnalysisResult.class))
                        .flatMap(transactionService::updateTrx)
                        .doOnError(error -> log.error("[0] Error processing analysis result: ", error))
                        .then(Mono.fromRunnable(record.receiverOffset()::acknowledge))
                        .onErrorResume(ex -> {
                            log.error("[1] Error processing analysis result: ", ex);
                            return Mono.empty();
                        }))
                .subscribe();

    }

    private void processMessage(final ConsumerRecord<String, String> record) {
        log.info("Message received: key = {}, value = {}", record.key(), record.value());
    }

}

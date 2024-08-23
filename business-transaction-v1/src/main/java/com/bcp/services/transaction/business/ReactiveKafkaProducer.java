package com.bcp.services.transaction.business;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

/**
 * Reactive Kafka Producer.<br/>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReactiveKafkaProducer {

    private final KafkaSender<String, String> kafkaSender;

    public Mono<Void> sendMessage(final String topic, final String key, final String value) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
        SenderRecord<String, String, String> senderRecord = SenderRecord.create(record, key);

        return kafkaSender.send(Mono.just(senderRecord))
                .flatMap(result -> Mono.empty())
                .onErrorResume(throwable -> {
                    log.error("Error sending message: {}", throwable.getMessage());
                    return Mono.empty();
                })
                .then();
    }
}

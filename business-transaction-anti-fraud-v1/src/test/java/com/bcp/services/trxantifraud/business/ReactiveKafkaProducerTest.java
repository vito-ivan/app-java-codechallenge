package com.bcp.services.trxantifraud.business;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ReactiveKafkaProducerTest {

    @Mock
    private KafkaSender<String, String> kafkaSender;

    @InjectMocks
    private ReactiveKafkaProducer reactiveKafkaProducer;

    @Test
    @DisplayName("Send message successfully")
    void sendMessageSuccessfully() {
        // Arrange
        doReturn(Flux.empty()).when(kafkaSender).send(any(Mono.class));

        // Act
        Mono<Void> result = reactiveKafkaProducer.sendMessage("test-topic", "test-key", "test-value");

        // Assert
        StepVerifier.create(result)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Fail to send message")
    void failToSendMessage() {

        // Arrange
        doReturn(Flux.error(new RuntimeException("Kafka send error"))).when(kafkaSender).send(any(Mono.class));

        // Act
        Mono<Void> result = reactiveKafkaProducer.sendMessage("test-topic", "test-key", "test-value");

        // Assert
        StepVerifier.create(result)
                .expectErrorMessage("Kafka send error")
                .verify();
    }
}
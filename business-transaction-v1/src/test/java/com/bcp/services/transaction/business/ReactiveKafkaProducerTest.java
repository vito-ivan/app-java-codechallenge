package com.bcp.services.transaction.business;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.annotation.DirtiesContext;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.kafka.sender.KafkaSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
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
        doReturn(Flux.error(new RuntimeException("Kafka send error"))).when(kafkaSender).send(any(Mono.class));

        Mono<Void> result = reactiveKafkaProducer.sendMessage("test-topic", "test-key", "test-value");

        StepVerifier.create(result)
                .expectComplete()
                .verify();
    }
}
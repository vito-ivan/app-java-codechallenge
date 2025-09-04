package com.bcp.services.trxantifraud.business;

import com.bcp.services.trxantifraud.model.AnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReactiveKafkaConsumerTest {

    @Mock
    private KafkaReceiver<String, String> kafkaReceiver;

    @Mock
    private TransactionService transactionService;

    @Mock
    private ReactiveKafkaProducer reactiveKafkaProducer;

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private ReactiveKafkaConsumer reactiveKafkaConsumer;

    @Test
    @DisplayName("Initialize successfully")
    void initializeSuccessfully() {

        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>("test-topic", 0, 0L,
                "9295f66a-6db4-4a39-9af2-2c524f49fb39",
                "{\"id\":\"cc34b401-ba74-41e6-9079-5e5b9567d938\",\"amount\":\"200.00\",\"currency\":\"USD\"}");

        ReceiverRecord<String, String> mockRecord = getStringStringReceiverRecord(consumerRecord);
        doReturn(Flux.just(mockRecord)).when(kafkaReceiver).receive();

        // Arrange
        var analysisResult = AnalysisResult.builder().build();
        doReturn(Mono.empty()).when(reactiveKafkaProducer).sendMessage(any(), any(), any());
        doReturn(Mono.just(analysisResult)).when(transactionService).analyzeTrx(any());

        // Act
        reactiveKafkaConsumer.initialize();

        // Assert
        StepVerifier.create(transactionService.analyzeTrx(any()))
                .expectNext(analysisResult)
                .expectComplete()
                .verify();

        StepVerifier.create(reactiveKafkaProducer.sendMessage(any(), any(), any()))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Fail to initialize due to Kafka error")
    void failToInitializeDueToKafkaError() {
        // Arrange
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>("test-topic", 0, 0L,
                "9295f66a-6db4-4a39-9af2-2c524f49fb39",
                "{\"id\":\"cc34b401-ba74-41e6-9079-5e5b9567d938\",\"amount\":\"ee.00\",\"currency\":\"USD\"}");

        ReceiverRecord<String, String> mockRecord = getStringStringReceiverRecord(consumerRecord);
        doReturn(Flux.just(mockRecord)).when(kafkaReceiver).receive();

        reactiveKafkaConsumer.initialize();

        // Assert
        verify(transactionService, never()).analyzeTrx(any());
    }

    private static ReceiverRecord<String, String> getStringStringReceiverRecord(
            ConsumerRecord<String, String> consumerRecord) {
        ReceiverOffset receiverOffset = new ReceiverOffset() {

            @Override
            public TopicPartition topicPartition() {
                return new TopicPartition("test-topic", 0);
            }

            @Override
            public long offset() {
                return 0;
            }

            @Override
            public void acknowledge() {

            }

            @Override
            public Mono<Void> commit() {
                return null;
            }
        };

        return new ReceiverRecord<>(consumerRecord, receiverOffset);
    }
}
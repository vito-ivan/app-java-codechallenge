package com.bcp.services.transaction.business;

import com.bcp.services.transaction.model.AnalysisResult;
import com.bcp.services.transaction.trx.model.TransactionResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReactiveKafkaConsumerTest {

    @Mock
    private KafkaReceiver<String, String> kafkaReceiver;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private ReactiveKafkaConsumer reactiveKafkaConsumer;

    @Mock
    private ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<Class<AnalysisResult>> classCaptor;

    @Test
    @DisplayName("Initialize successfully")
    void initializeSuccessfully() throws JsonProcessingException {
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>(
                "test-topic",
                0,
                0L,
                "9295f66a-6db4-4a39-9af2-2c524f49fb39",
                "{\"transactionId\":\"4e375479-048e-43d5-ae02-6612a03e9037\",\"isFraudulent\":false}");

        ReceiverRecord<String, String> mockRecord = getStringStringReceiverRecord(consumerRecord);
        doReturn(Flux.just(mockRecord)).when(kafkaReceiver).receive();

        when(objectMapper.readValue(anyString(), classCaptor.capture()))
                .thenReturn(AnalysisResult.builder()
                        .transactionId("4e375479-048e-43d5-ae02-6612a03e9037")
                        .isFraudulent(false)
                        .build());

        var transactionResponse = new TransactionResponse()
                .transactionExternalId(UUID.fromString("9295f66a-6db4-4a39-9af2-2c524f49fb39"));
        doReturn(Mono.just(transactionResponse)).when(transactionService).updateTrx(any());

        reactiveKafkaConsumer.initialize();

        verify(transactionService, times(1)).updateTrx(any());
    }

    @Test
    @DisplayName("Fail to initialize due to Kafka error")
    void failToInitializeDueToKafkaError() throws JsonProcessingException {
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>(
                "test-topic",
                0,
                0L,
                "9295f66a-6db4-4a39-9af2-2c524f49fb39",
                "{\"transactionId0\":\"4e375479-048e-43d5-ae02-6612a03e9037\",\"isFraudulent0\":false}");

        ReceiverRecord<String, String> mockRecord = getStringStringReceiverRecord(consumerRecord);
        doReturn(Flux.just(mockRecord)).when(kafkaReceiver).receive();

        when(objectMapper.readValue(anyString(), classCaptor.capture()))
                .thenThrow(new RuntimeException("Error parsing JSON"));

        reactiveKafkaConsumer.initialize();

        verify(transactionService, never()).updateTrx(any());
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
                // Acknowledge logic
            }

            @Override
            public Mono<Void> commit() {
                return Mono.empty();
            }
        };

        return new ReceiverRecord<>(consumerRecord, receiverOffset);
    }
}
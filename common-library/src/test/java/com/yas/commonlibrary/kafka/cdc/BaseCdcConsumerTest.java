package com.yas.commonlibrary.kafka.cdc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.MessageHeaders;

class BaseCdcConsumerTest {

    // Concrete subclass to allow instantiation of abstract class
    static class TestCdcConsumer extends BaseCdcConsumer<String, String> {
    }

    private TestCdcConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new TestCdcConsumer();
    }

    @Test
    @SuppressWarnings("unchecked")
    void testProcessMessage_singleConsumer_invokesConsumerWithRecord() {
        MessageHeaders headers = new MessageHeaders(null);
        Consumer<String> mockConsumer = mock(Consumer.class);

        consumer.processMessage("testValue", headers, mockConsumer);

        verify(mockConsumer).accept("testValue");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testProcessMessage_singleConsumer_withNullRecord_invokesConsumerWithNull() {
        MessageHeaders headers = new MessageHeaders(null);
        Consumer<String> mockConsumer = mock(Consumer.class);

        consumer.processMessage(null, headers, mockConsumer);

        verify(mockConsumer).accept(null);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testProcessMessage_biConsumer_invokesConsumerWithKeyAndValue() {
        MessageHeaders headers = new MessageHeaders(null);
        BiConsumer<String, String> mockBiConsumer = mock(BiConsumer.class);

        consumer.processMessage("key1", "value1", headers, mockBiConsumer);

        verify(mockBiConsumer).accept("key1", "value1");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testProcessMessage_biConsumer_withNullValues_invokesConsumer() {
        MessageHeaders headers = new MessageHeaders(null);
        BiConsumer<String, String> mockBiConsumer = mock(BiConsumer.class);

        consumer.processMessage(null, null, headers, mockBiConsumer);

        verify(mockBiConsumer).accept(null, null);
    }

    @Test
    void testBaseCdcConsumer_constants_haveExpectedValues() {
        org.junit.jupiter.api.Assertions.assertNotNull(BaseCdcConsumer.RECEIVED_MESSAGE_HEADERS);
        org.junit.jupiter.api.Assertions.assertNotNull(BaseCdcConsumer.PROCESSING_RECORD_KEY_VALUE);
        org.junit.jupiter.api.Assertions.assertNotNull(BaseCdcConsumer.RECORD_PROCESSED_SUCCESSFULLY_KEY);
    }
}

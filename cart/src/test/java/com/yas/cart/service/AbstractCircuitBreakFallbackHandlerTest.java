package com.yas.cart.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AbstractCircuitBreakFallbackHandlerTest {

    private ConcreteCircuitBreakFallbackHandler concreteHandler;

    @BeforeEach
    void setUp() {
        concreteHandler = new ConcreteCircuitBreakFallbackHandler();
    }

    @Nested
    class HandleBodilessFallbackTest {

        @Test
        void testHandleBodilessFallback_whenThrowableProvided_shouldThrowException() {
            Exception testException = new IllegalStateException("Test error");

            assertThrows(IllegalStateException.class, () ->
                concreteHandler.handleBodilessFallback(testException)
            );
        }

        @Test
        void testHandleBodilessFallback_whenNullMessageException_shouldThrowException() {
            Exception testException = new RuntimeException();

            assertThrows(RuntimeException.class, () ->
                concreteHandler.handleBodilessFallback(testException)
            );
        }
    }

    @Nested
    class HandleTypedFallbackTest {

        @Test
        void testHandleTypedFallback_whenThrowableProvided_shouldThrowException() {
            Exception testException = new IllegalArgumentException("Invalid argument");

            assertThrows(IllegalArgumentException.class, () ->
                concreteHandler.handleTypedFallback(testException)
            );
        }

        @Test
        void testHandleTypedFallback_whenExceptionOccurs_shouldNotReturnValue() {
            Exception testException = new TimeoutException("Timeout occurred");

            assertThrows(TimeoutException.class, () ->
                concreteHandler.handleTypedFallback(testException)
            );
        }
    }

    /**
     * Concrete implementation for testing abstract class
     */
    static class ConcreteCircuitBreakFallbackHandler extends AbstractCircuitBreakFallbackHandler {
        @Override
        public void handleBodilessFallback(Throwable throwable) throws Throwable {
            super.handleBodilessFallback(throwable);
        }

        @Override
        public <T> T handleTypedFallback(Throwable throwable) throws Throwable {
            return super.handleTypedFallback(throwable);
        }
    }

    /**
     * Custom exception for testing
     */
    static class TimeoutException extends Exception {
        TimeoutException(String message) {
            super(message);
        }
    }
}

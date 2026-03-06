package com.yas.recommendation.vector.common.document;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class DefaultIdGeneratorTest {

    @Test
    void testGenerateId_withValidInputs_shouldReturnUUIDString() {
        // Given
        String idPrefix = "product";
        Long identity = 123L;
        DefaultIdGenerator generator = new DefaultIdGenerator(idPrefix, identity);

        // When
        String result = generator.generateId();

        // Then
        assertNotNull(result);
        assertDoesNotThrow(() -> UUID.fromString(result));
    }

    @Test
    void testGenerateId_withSameInputs_shouldReturnSameUUID() {
        // Given
        String idPrefix = "product";
        Long identity = 456L;
        DefaultIdGenerator generator1 = new DefaultIdGenerator(idPrefix, identity);
        DefaultIdGenerator generator2 = new DefaultIdGenerator(idPrefix, identity);

        // When
        String result1 = generator1.generateId();
        String result2 = generator2.generateId();

        // Then
        assertEquals(result1, result2);
    }

    @Test
    void testGenerateId_withDifferentIdentity_shouldReturnDifferentUUID() {
        // Given
        String idPrefix = "product";
        DefaultIdGenerator generator1 = new DefaultIdGenerator(idPrefix, 1L);
        DefaultIdGenerator generator2 = new DefaultIdGenerator(idPrefix, 2L);

        // When
        String result1 = generator1.generateId();
        String result2 = generator2.generateId();

        // Then
        assertNotEquals(result1, result2);
    }

    @Test
    void testGenerateId_withDifferentPrefix_shouldReturnDifferentUUID() {
        // Given
        Long identity = 789L;
        DefaultIdGenerator generator1 = new DefaultIdGenerator("product", identity);
        DefaultIdGenerator generator2 = new DefaultIdGenerator("order", identity);

        // When
        String result1 = generator1.generateId();
        String result2 = generator2.generateId();

        // Then
        assertNotEquals(result1, result2);
    }

    @Test
    void testGenerateId_withZeroIdentity_shouldGenerateValidUUID() {
        // Given
        String idPrefix = "test";
        Long identity = 0L;
        DefaultIdGenerator generator = new DefaultIdGenerator(idPrefix, identity);

        // When
        String result = generator.generateId();

        // Then
        assertNotNull(result);
        assertDoesNotThrow(() -> UUID.fromString(result));
    }

    @Test
    void testGenerateId_withNegativeIdentity_shouldGenerateValidUUID() {
        // Given
        String idPrefix = "test";
        Long identity = -100L;
        DefaultIdGenerator generator = new DefaultIdGenerator(idPrefix, identity);

        // When
        String result = generator.generateId();

        // Then
        assertNotNull(result);
        assertDoesNotThrow(() -> UUID.fromString(result));
    }

    @Test
    void testGenerateId_withEmptyPrefix_shouldGenerateValidUUID() {
        // Given
        String idPrefix = "";
        Long identity = 999L;
        DefaultIdGenerator generator = new DefaultIdGenerator(idPrefix, identity);

        // When
        String result = generator.generateId();

        // Then
        assertNotNull(result);
        assertDoesNotThrow(() -> UUID.fromString(result));
    }

    @Test
    void testGenerateId_multipleCallsOnSameInstance_shouldReturnSameUUID() {
        // Given
        DefaultIdGenerator generator = new DefaultIdGenerator("test", 555L);

        // When
        String result1 = generator.generateId();
        String result2 = generator.generateId();
        String result3 = generator.generateId();

        // Then
        assertEquals(result1, result2);
        assertEquals(result2, result3);
    }
}

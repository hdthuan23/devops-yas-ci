package com.yas.commonlibrary.kafka.cdc.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class OperationTest {

    @Test
    void testOperation_read_hasCorrectName() {
        assertEquals("r", Operation.READ.getName());
    }

    @Test
    void testOperation_create_hasCorrectName() {
        assertEquals("c", Operation.CREATE.getName());
    }

    @Test
    void testOperation_update_hasCorrectName() {
        assertEquals("u", Operation.UPDATE.getName());
    }

    @Test
    void testOperation_delete_hasCorrectName() {
        assertEquals("d", Operation.DELETE.getName());
    }

    @Test
    void testOperation_values_hasAllFourEntries() {
        assertEquals(4, Operation.values().length);
    }

    @Test
    void testOperation_valueOf_returnsCorrectEntry() {
        assertEquals(Operation.CREATE, Operation.valueOf("CREATE"));
        assertEquals(Operation.DELETE, Operation.valueOf("DELETE"));
    }

    @Test
    void testOperation_getName_isNotNull() {
        for (Operation op : Operation.values()) {
            assertNotNull(op.getName());
        }
    }
}

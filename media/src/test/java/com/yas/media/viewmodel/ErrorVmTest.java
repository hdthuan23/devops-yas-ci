package com.yas.media.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ErrorVmTest {

    @Test
    void errorVm_withFieldErrors_thenAllFieldsSet() {
        List<String> fieldErrors = List.of("field1 is required", "field2 is invalid");
        ErrorVm errorVm = new ErrorVm("400", "Bad Request", "Validation failed", fieldErrors);

        assertEquals("400", errorVm.statusCode());
        assertEquals("Bad Request", errorVm.title());
        assertEquals("Validation failed", errorVm.detail());
        assertEquals(2, errorVm.fieldErrors().size());
        assertEquals("field1 is required", errorVm.fieldErrors().get(0));
    }

    @Test
    void errorVm_withoutFieldErrors_thenEmptyList() {
        ErrorVm errorVm = new ErrorVm("500", "Internal Server Error", "Something went wrong");

        assertEquals("500", errorVm.statusCode());
        assertEquals("Internal Server Error", errorVm.title());
        assertEquals("Something went wrong", errorVm.detail());
        assertNotNull(errorVm.fieldErrors());
        assertTrue(errorVm.fieldErrors().isEmpty());
    }

    @Test
    void errorVm_withNullFieldErrors_thenNullList() {
        ErrorVm errorVm = new ErrorVm("404", "Not Found", "Resource not found", null);

        assertEquals("404", errorVm.statusCode());
        assertEquals("Not Found", errorVm.title());
        assertEquals("Resource not found", errorVm.detail());
    }
}

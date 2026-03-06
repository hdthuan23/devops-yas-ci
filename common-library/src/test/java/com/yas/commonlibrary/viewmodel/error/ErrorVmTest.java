package com.yas.commonlibrary.viewmodel.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ErrorVmTest {

    @Test
    void testErrorVm_threeArgConstructor_createsWithEmptyFieldErrors() {
        ErrorVm errorVm = new ErrorVm("400", "Bad Request", "Invalid input");

        assertEquals("400", errorVm.statusCode());
        assertEquals("Bad Request", errorVm.title());
        assertEquals("Invalid input", errorVm.detail());
        assertNotNull(errorVm.fieldErrors());
        assertTrue(errorVm.fieldErrors().isEmpty());
    }

    @Test
    void testErrorVm_fourArgConstructor_createsWithGivenFieldErrors() {
        List<String> fieldErrors = List.of("name is required", "email is invalid");
        ErrorVm errorVm = new ErrorVm("422", "Unprocessable Entity", "Validation failed", fieldErrors);

        assertEquals("422", errorVm.statusCode());
        assertEquals("Unprocessable Entity", errorVm.title());
        assertEquals("Validation failed", errorVm.detail());
        assertEquals(2, errorVm.fieldErrors().size());
        assertTrue(errorVm.fieldErrors().contains("name is required"));
        assertTrue(errorVm.fieldErrors().contains("email is invalid"));
    }

    @Test
    void testErrorVm_threeArgConstructor_fieldErrorsAreMutable() {
        ErrorVm errorVm = new ErrorVm("500", "Internal Server Error", "Unexpected error");

        // The internal list created by new ArrayList<>() should be mutable
        assertNotNull(errorVm.fieldErrors());
        errorVm.fieldErrors().add("new error");
        assertEquals(1, errorVm.fieldErrors().size());
    }

    @Test
    void testErrorVm_withNullValues_createsSuccessfully() {
        ErrorVm errorVm = new ErrorVm(null, null, null);

        assertNotNull(errorVm.fieldErrors());
    }
}

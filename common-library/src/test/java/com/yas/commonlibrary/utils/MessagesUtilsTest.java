package com.yas.commonlibrary.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class MessagesUtilsTest {

    @Test
    void testGetMessage_withKnownKey_returnsFormattedMessage() {
        String message = MessagesUtils.getMessage("NOT_FOUND_PRODUCT", "123");
        assertNotNull(message);
        assertEquals("Not found product 123", message);
    }

    @Test
    void testGetMessage_withKnownKeyAndNoArgs_returnsMessageWithPlaceholder() {
        String message = MessagesUtils.getMessage("USER_NOT_FOUND");
        assertNotNull(message);
        assertEquals("User not found", message);
    }

    @Test
    void testGetMessage_withUnknownKey_returnsFallbackToErrorCode() {
        String message = MessagesUtils.getMessage("THIS_KEY_DOES_NOT_EXIST");
        assertNotNull(message);
        assertEquals("THIS_KEY_DOES_NOT_EXIST", message);
    }

    @Test
    void testGetMessage_withMultipleArgs_replacesAllPlaceholders() {
        String message = MessagesUtils.getMessage("NOT_FOUND_PRODUCT", "product-xyz");
        assertNotNull(message);
        assertEquals("Not found product product-xyz", message);
    }

    @Test
    void testGetMessage_withOrderNotFound_returnsCorrectMessage() {
        String message = MessagesUtils.getMessage("ORDER_NOT_FOUND", "ORD-001");
        assertEquals("Order ORD-001 is not found", message);
    }

    @Test
    void testGetMessage_withUnknownKeyAndArguments_returnsKeyWithArgumentsUnchanged() {
        String message = MessagesUtils.getMessage("UNKNOWN_KEY_WITH_ARG", "arg1");
        assertEquals("UNKNOWN_KEY_WITH_ARG", message);
    }
}

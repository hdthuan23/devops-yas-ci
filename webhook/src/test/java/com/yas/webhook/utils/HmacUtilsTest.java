package com.yas.webhook.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.Test;

class HmacUtilsTest {

    @Test
    void hash_WithSameInput_ShouldReturnSameResult() throws NoSuchAlgorithmException, InvalidKeyException {
        String firstHash = HmacUtils.hash("payload", "secret-key");
        String secondHash = HmacUtils.hash("payload", "secret-key");

        assertEquals(firstHash, secondHash);
    }

    @Test
    void hash_WithDifferentInput_ShouldReturnDifferentResult() throws NoSuchAlgorithmException, InvalidKeyException {
        String firstHash = HmacUtils.hash("payload-1", "secret-key");
        String secondHash = HmacUtils.hash("payload-2", "secret-key");

        assertNotEquals(firstHash, secondHash);
    }
}
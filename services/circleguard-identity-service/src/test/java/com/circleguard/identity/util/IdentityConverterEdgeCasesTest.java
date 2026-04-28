package com.circleguard.identity.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdentityConverterEdgeCasesTest {

    private IdentityEncryptionConverter converter;
    private static final String SECRET = "test-secret-32-chars-long-123456";
    private static final String SALT = "deadbeef";

    @BeforeEach
    void setUp() {
        converter = new IdentityEncryptionConverter(SECRET, SALT);
    }

    @Test
    void shouldCorrectlyRoundTripUnicodeCharacters() {
        String original = "ñoño@universidad.edu.co";
        byte[] encrypted = converter.convertToDatabaseColumn(original);
        String decrypted = converter.convertToEntityAttribute(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    void shouldProduceDifferentCiphertextForDifferentInputs() {
        byte[] enc1 = converter.convertToDatabaseColumn("user1@example.com");
        byte[] enc2 = converter.convertToDatabaseColumn("user2@example.com");
        assertFalse(java.util.Arrays.equals(enc1, enc2));
    }

    @Test
    void shouldRoundTripLongEmailAddress() {
        String longEmail = "very.long.email.address.student@subdomain.universidad.edu.co";
        byte[] encrypted = converter.convertToDatabaseColumn(longEmail);
        String decrypted = converter.convertToEntityAttribute(encrypted);
        assertEquals(longEmail, decrypted);
    }
}

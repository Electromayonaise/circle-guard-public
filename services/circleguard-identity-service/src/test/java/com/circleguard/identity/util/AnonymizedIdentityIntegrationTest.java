package com.circleguard.identity.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnonymizedIdentityIntegrationTest {

    private static final String SECRET = "test-secret-32-chars-long-123456";
    private static final String SALT = "deadbeef";

    @Test
    void encryptedIdentityShouldNotContainPlaintextEmail() {
        IdentityEncryptionConverter converter = new IdentityEncryptionConverter(SECRET, SALT);
        String email = "student@universidad.edu.co";

        byte[] encrypted = converter.convertToDatabaseColumn(email);
        String encryptedStr = new String(encrypted);

        assertFalse(encryptedStr.contains("student"));
        assertFalse(encryptedStr.contains("universidad"));
        assertFalse(encryptedStr.contains("@"));
    }

    @Test
    void twoUsersWithDifferentEmailsShouldHaveDifferentAnonymousIds() {
        IdentityEncryptionConverter converter = new IdentityEncryptionConverter(SECRET, SALT);

        byte[] enc1 = converter.convertToDatabaseColumn("alice@universidad.edu.co");
        byte[] enc2 = converter.convertToDatabaseColumn("bob@universidad.edu.co");

        assertFalse(java.util.Arrays.equals(enc1, enc2),
                "Different emails must produce different encrypted identities");
    }

    @Test
    void decryptedIdentityShouldMatchOriginalEmailForAuditPurposes() {
        IdentityEncryptionConverter converter = new IdentityEncryptionConverter(SECRET, SALT);
        String email = "audit@universidad.edu.co";

        byte[] encrypted = converter.convertToDatabaseColumn(email);
        String decrypted = converter.convertToEntityAttribute(encrypted);

        assertEquals(email, decrypted, "Decrypted value must match original for audit trail");
    }
}

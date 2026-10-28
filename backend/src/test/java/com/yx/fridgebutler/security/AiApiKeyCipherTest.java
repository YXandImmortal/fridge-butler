package com.yx.fridgebutler.security;

import com.yx.fridgebutler.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class AiApiKeyCipherTest {

    private static final String TEST_KEY = Base64.getEncoder().encodeToString(
            new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
                    16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31});

    private final AiApiKeyCipher cipher = new AiApiKeyCipher(TEST_KEY);

    @Test
    void encryptsWithRandomIvAndDecrypts() {
        String first = cipher.encrypt("sk-test-1234");
        String second = cipher.encrypt("sk-test-1234");

        assertNotEquals("sk-test-1234", first);
        assertNotEquals(first, second);
        assertEquals("sk-test-1234", cipher.decrypt(first));
        assertEquals("sk-test-1234", cipher.decrypt(second));
        assertEquals("****1234", AiApiKeyCipher.mask("sk-test-1234"));
    }

    @Test
    void rejectsWrongKeyAndTamperedCiphertext() {
        String encrypted = cipher.encrypt("sk-test-1234");
        String otherKey = Base64.getEncoder().encodeToString(new byte[32]);
        AiApiKeyCipher otherCipher = new AiApiKeyCipher(otherKey);

        assertThrows(BusinessException.class, () -> otherCipher.decrypt(encrypted));
        String tampered = (encrypted.charAt(0) == 'A' ? 'B' : 'A') + encrypted.substring(1);
        assertThrows(BusinessException.class, () -> cipher.decrypt(tampered));
    }

    @Test
    void rejectsMissingOrInvalidMasterKey() {
        assertThrows(BusinessException.class, () -> new AiApiKeyCipher("").encrypt("sk-test"));
        assertThrows(BusinessException.class, () -> new AiApiKeyCipher("not-base64").encrypt("sk-test"));
        assertThrows(BusinessException.class, () -> new AiApiKeyCipher("YWJj").encrypt("sk-test"));
    }
}

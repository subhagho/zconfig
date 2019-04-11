package com.codekutter.zconfig.common.utils;

import com.codekutter.zconfig.common.LogUtils;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CypherUtilsTest {

    @Test
    void decrypt() {
        try {
            String passcode = UUID.randomUUID().toString().substring(0, 16);

            String data =
                    "I want to encrypt a string and then put it on a file. Also want to decrypt it when I want. I don’t need very strong security. I just want to make it harder to get my data others.";
            byte[] encrypted = CypherUtils.encrypt(data.getBytes(), passcode);
            assertNotNull(encrypted);
            assertTrue(encrypted.length > 0);

            String buff = new String(encrypted);
            LogUtils.debug(getClass(), String.format("Encrypted Data: [%s]", buff));

            byte[] decrypted = CypherUtils.decrypt(encrypted, passcode);
            assertNotNull(decrypted);
            assertTrue(decrypted.length > 0);
            buff = new String(decrypted);
            assertEquals(data, buff);
            LogUtils.debug(getClass(), buff);
        } catch (Exception ex) {
            LogUtils.error(getClass(), ex);
            fail(ex.getLocalizedMessage());
        }
    }

    @Test
    void decryptString() {
        try {
            String passcode = UUID.randomUUID().toString().substring(0, 16);

            String data =
                    "I want to encrypt a string and then put it on a file. Also want to decrypt it when I want. I don’t need very strong security. I just want to make it harder to get my data others.";
            String encrypted =
                    CypherUtils.encryptAsString(data.getBytes(), passcode);
            assertNotNull(encrypted);
            assertTrue(encrypted.length() > 0);

            LogUtils.debug(getClass(),
                           String.format("Encrypted Data: [%s]", encrypted));

            byte[] decrypted = CypherUtils.decrypt(
                    encrypted, passcode);
            assertNotNull(decrypted);
            assertTrue(decrypted.length > 0);
            String buff = new String(decrypted);
            assertEquals(data, buff);
            LogUtils.debug(getClass(), buff);
        } catch (Exception ex) {
            LogUtils.error(getClass(), ex);
            fail(ex.getLocalizedMessage());
        }
    }
}
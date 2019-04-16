package com.codekutter.zconfig.common.utils;

import com.codekutter.zconfig.common.model.Configuration;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.codec.binary.Base64;

import javax.annotation.Nonnull;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class CypherUtils {
    private static final String HASH_ALGO = "MD5";

    /**
     * Get an MD5 hash of the specified key.
     *
     * @param key - Input Key.
     * @return - String value of the Hash
     * @throws Exception
     */
    public static String getKeyHash(@Nonnull String key) throws Exception {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(key));

        MessageDigest digest = MessageDigest.getInstance(HASH_ALGO);
        byte[] d = digest.digest(key.getBytes(StandardCharsets.UTF_8));
        return new String(d, StandardCharsets.UTF_8);
    }

    /**
     * Encrypt the passed data buffer using the passcode.
     *
     * @param data     - Data Buffer.
     * @param password - Passcode.
     * @return - Encrypted Buffer.
     * @throws Exception
     */
    public static byte[] encrypt(@Nonnull byte[] data, @Nonnull String password)
    throws Exception {
        Preconditions.checkArgument(data != null && data.length > 0);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(password));

        // Create key and cipher
        Key aesKey = new SecretKeySpec(password.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        // encrypt the text
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        return cipher.doFinal(data);
    }

    /**
     * Encrypt the passed data buffer using the passcode.
     *
     * @param data     - Data Buffer.
     * @param password - Passcode.
     * @return - Base64 encoded String.
     * @throws Exception
     */
    public static String encryptAsString(@Nonnull byte[] data,
                                         @Nonnull String password)
    throws Exception {
        Preconditions.checkArgument(data != null && data.length > 0);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(password));

        byte[] encrypted = encrypt(data, password);
        return new String(Base64.encodeBase64(encrypted));
    }

    /**
     * Decrypt the data buffer using the passcode.
     *
     * @param data     - Encrypted Data buffer.
     * @param password - Passcode
     * @return - Decrypted Data Buffer.
     * @throws Exception
     */
    public static byte[] decrypt(byte[] data, String password) throws Exception {
        Preconditions.checkArgument(data != null && data.length > 0);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(password));

        // Create key and cipher
        Key aesKey = new SecretKeySpec(password.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");

        // decrypt the text
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        return cipher.doFinal(data);
    }

    /**
     * Decrypt the string data using the passcode.
     *
     * @param data     - Encrypted String data.
     * @param password - Passcode
     * @return - Decrypted Data Buffer.
     * @throws Exception
     */
    public static byte[] decrypt(String data, String password) throws Exception {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(data));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(password));

        byte[] array = Base64.decodeBase64(data.getBytes());
        return decrypt(array, password);
    }

    public static class ConfigVault {
        private Map<String, String> vault = new HashMap<>();

        public ConfigVault addPasscode(Configuration config, String passcode)
        throws Exception {
            Preconditions.checkArgument(config != null);
            Preconditions.checkArgument(!Strings.isNullOrEmpty(passcode));
            String key = getEncodingKey(config);
            String encrypted = encryptAsString(passcode.getBytes(), key);
            vault.put(config.getInstanceId(), encrypted);

            return this;
        }

        public String getPasscode(Configuration config) throws Exception {
            Preconditions.checkArgument(config != null);
            if (vault.containsKey(config.getInstanceId())) {
                String value = vault.get(config.getInstanceId());
                String key = getEncodingKey(config);

                return new String(CypherUtils.decrypt(value, key));
            }
            return null;
        }

        public String decrypt(String data, Configuration config) throws Exception {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(data));
            String passcode = getPasscode(config);
            if (Strings.isNullOrEmpty(passcode)) {
                throw new Exception(
                        "Invalid Passcode: NULL/Empty passcode returned.");
            }
            byte[] buff = CypherUtils.decrypt(data, passcode);
            if (buff != null && buff.length > 0) {
                return new String(buff, StandardCharsets.UTF_8);
            }
            return null;
        }

        private String getEncodingKey(Configuration config) {
            String key = String.format("%s%s%d", config.getName(),
                                       config.getInstanceId(),
                                       config.getCreatedBy().getTimestamp());
            int index = config.getUpdatedBy().hashCode() / key.length();

            if (index + 16 >= key.length()) {
                index = index - 16;
            }
            return key.substring(index, 16);
        }
    }
}

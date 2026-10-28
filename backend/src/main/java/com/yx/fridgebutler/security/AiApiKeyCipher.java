package com.yx.fridgebutler.security;

import com.yx.fridgebutler.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class AiApiKeyCipher {

    private static final int IV_LENGTH = 12;
    private static final int GCM_TAG_BITS = 128;
    private static final int AES_KEY_LENGTH = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final String encryptionKey;

    public AiApiKeyCipher(@Value("${ai.config-encryption-key:}") String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public String encrypt(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw BusinessException.aiConfigError("API 密钥不能为空");
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(apiKey.trim().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(ByteBuffer.allocate(iv.length + ciphertext.length)
                    .put(iv)
                    .put(ciphertext)
                    .array());
        } catch (GeneralSecurityException e) {
            throw BusinessException.aiConfigError("API 密钥加密失败，请检查 AI_CONFIG_SECRET");
        }
    }

    public String decrypt(String encryptedApiKey) {
        if (encryptedApiKey == null || encryptedApiKey.isBlank()) {
            throw BusinessException.aiConfigError("API 密钥密文不存在");
        }
        try {
            ByteBuffer buffer = ByteBuffer.wrap(Base64.getDecoder().decode(encryptedApiKey));
            if (buffer.remaining() <= IV_LENGTH) {
                throw BusinessException.aiConfigError("API 密钥密文格式错误");
            }
            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(ciphertext), java.nio.charset.StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | GeneralSecurityException e) {
            throw BusinessException.aiConfigError("API 密钥解密失败，请检查 AI_CONFIG_SECRET 是否正确");
        }
    }

    public static String mask(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return "";
        }
        String trimmed = apiKey.trim();
        return trimmed.length() <= 4 ? "****" : "****" + trimmed.substring(trimmed.length() - 4);
    }

    private SecretKeySpec getSecretKey() {
        if (encryptionKey == null || encryptionKey.isBlank()) {
            throw BusinessException.aiConfigError("未配置 AI_CONFIG_SECRET，无法加密保存 API 密钥");
        }
        final byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(encryptionKey.trim());
        } catch (IllegalArgumentException e) {
            throw BusinessException.aiConfigError("AI_CONFIG_SECRET 必须是 Base64 编码的 32 字节随机密钥");
        }
        if (keyBytes.length != AES_KEY_LENGTH) {
            throw BusinessException.aiConfigError("AI_CONFIG_SECRET 解码后必须为 32 字节");
        }
        return new SecretKeySpec(keyBytes, "AES");
    }
}

package com.codeforces.analyzer.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class PasswordUtil {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    private PasswordUtil() {
    }

    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống.");
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, buildKey(), new GCMParameterSpec(TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("Không mã hóa được mật khẩu.", e);
        }
    }

    public static String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isBlank()) {
            return "";
        }
        try {
            String[] parts = encryptedText.split(":", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Dữ liệu mật khẩu không hợp lệ.");
            }
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] encrypted = Base64.getDecoder().decode(parts[1]);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, buildKey(), new GCMParameterSpec(TAG_LENGTH, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Không giải mã được mật khẩu. Hãy kiểm tra khóa bảo mật trong cấu hình.", e);
        }
    }

    private static SecretKeySpec buildKey() throws Exception {
        String secret = AppConfig.get("security.secret", "khoa-bi-mat-mac-dinh");
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] key = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(Arrays.copyOf(key, 32), "AES");
    }
}

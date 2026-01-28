package com.example.DATN.constant.Util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class CheckSumUtil {
    private final String secret;
    public CheckSumUtil(@Value("${checksum.token}") String secret) {
        this.secret = secret;
    }
    private static final String CHARSET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    public String generate(String prefix) {
        String randomPart = randomString(6);
        String checksum = generateChecksum(randomPart);
        return prefix + "-" + randomPart + "-" + checksum;
    }

    public String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARSET.charAt(RANDOM.nextInt(CHARSET.length())));
        }
        return sb.toString();
    }

    public String generateChecksum(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key =
                    new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            mac.init(key);

            byte[] hash = mac.doFinal(data.getBytes());
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hash)
                    .substring(0, 2)
                    .toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public boolean verify(String code) {
        if (code == null || code.isBlank()) return false;

        // ép format đúng: PREFIX-RANDOM-CHECKSUM
        String[] parts = code.split("-");
        if (parts.length != 3) return false;

        String prefix = parts[0];
        String randomPart = parts[1];
        String checksum = parts[2];

        // validate từng phần
        if (prefix.isBlank() || randomPart.isBlank() || checksum.isBlank()) {
            return false;
        }

        String expectedChecksum = generateChecksum(randomPart);
        return expectedChecksum.equalsIgnoreCase(checksum);
    }
}

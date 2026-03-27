package com.user.register.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

public class SecurityUtils {

    private static final String AES = "AES";

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // PASSWORD HASH
    public static String hashPassword(String password) {
        return encoder.encode(password);
    }

    public static boolean matchPassword(String raw, String hashed) {
        return encoder.matches(raw, hashed);
    }

    // CREATE 16 BYTE AES KEY
    private static SecretKeySpec getKey(String myKey) throws Exception {

        byte[] key = myKey.getBytes(StandardCharsets.UTF_8);

        MessageDigest sha = MessageDigest.getInstance("SHA-256");

        key = sha.digest(key);

        key = Arrays.copyOf(key, 16); // 16 bytes for AES-128

        return new SecretKeySpec(key, AES);
    }

    // ENCRYPT
    public static String encrypt(String value, String key) throws Exception {

        SecretKeySpec secretKey = getKey(key);

        Cipher cipher = Cipher.getInstance(AES);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        return Base64.getEncoder().encodeToString(cipher.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }

    // DECRYPT
    public static String decrypt(String value, String key) throws Exception {

        SecretKeySpec secretKey = getKey(key);

        Cipher cipher = Cipher.getInstance(AES);

        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        return new String(cipher.doFinal(Base64.getDecoder().decode(value)));
    }

}
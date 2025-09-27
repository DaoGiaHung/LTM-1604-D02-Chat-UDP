package com.chatroom.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptionUtil {
    private static final String KEY = "Key12345654321"; 
    private static SecretKeySpec secretKey;

    static {
        secretKey = new SecretKeySpec(KEY.getBytes(), "AES");
    }

    public static String encrypt(String str) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(str.getBytes("UTF-8")));
        } catch (Exception e) {
            e.printStackTrace();
            return str;
        }
    }

    public static String decrypt(String str) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(str)), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return str;
        }
    }
}
package com.chatroom.utils;

import java.io.*;
import java.nio.file.Files;
import java.util.Base64;

public class FileHandler {
    public static String encodeFileToBase64(File file) throws IOException {
        if (file.length() > 1024 * 1024) { 
            throw new IOException("File too large");
        }
        byte[] bytes = Files.readAllBytes(file.toPath());
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static void decodeBase64ToFile(String base64, String fileName) throws IOException {
        byte[] bytes = Base64.getDecoder().decode(base64);
        Files.write(new File(fileName).toPath(), bytes);
    }
}
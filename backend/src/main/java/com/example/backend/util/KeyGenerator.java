package com.example.backend.util;

import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
public class KeyGenerator {
    public static void main(String[] args) {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        String encodeKey = Base64.getEncoder().encodeToString(key);
        log.info("JWT_SECRET={}", encodeKey);
    }
}

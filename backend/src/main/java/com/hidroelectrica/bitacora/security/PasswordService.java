package com.hidroelectrica.bitacora.security;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class PasswordService {

    public String hashPassword(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public boolean matches(String rawPassword, String storedHash) {
        if (storedHash == null || rawPassword == null) return false;
        // Check both direct hash match and plain match for simple dev migrations if needed
        return hashPassword(rawPassword).equalsIgnoreCase(storedHash) || rawPassword.equals(storedHash);
    }
}

package com.example.roomify.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Hashes passwords using PBKDF2 with a random salt per password.
 * Unlike Base64 (which is just an encoding and fully reversible),
 * this is a one-way hash: it cannot be decoded back to the original
 * password, only compared against a freshly-typed one via matches().
 */
public class PasswordEncoder {

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    /**
     * Hashes a raw password. Returns "saltBase64:hashBase64".
     */
    public static String encode(String password) {
        try {
            byte[] salt = new byte[SALT_LENGTH];
            new SecureRandom().nextBytes(salt);

            byte[] hash = pbkdf2(password.toCharArray(), salt);

            return Base64.getEncoder().encodeToString(salt)
                    + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    /**
     * Checks a raw password against a previously encoded "salt:hash" value.
     */
    public static boolean matches(String raw, String encoded) {
        try {
            String[] parts = encoded.split(":");
            if (parts.length != 2) {
                return false; // not in the expected salt:hash format
            }

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

            byte[] actualHash = pbkdf2(raw.toCharArray(), salt);

            return java.security.MessageDigest.isEqual(expectedHash, actualHash);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return factory.generateSecret(spec).getEncoded();
    }
}
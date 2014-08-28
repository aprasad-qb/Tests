package com.asp;

//Java ships with PBKDF2 support
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

//http://adambard.com/blog/3-wrong-ways-to-store-a-password/
class Pbkdf2Demo {
    private static final int ITERATIONS = 20000;
    private static final int KEY_LENGTH = 160; // bits // 192 -> 24

    public static String hashPassword(String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        char[] passwordChars = password.toCharArray();
        byte[] saltBytes = salt.getBytes();
        // byte[] saltBytes = new BigInteger(salt, 16).toByteArray();

        PBEKeySpec spec = new PBEKeySpec(passwordChars, saltBytes, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hashedPassword = key.generateSecret(spec).getEncoded();

        return toHex(hashedPassword);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(hashPassword("passwordpasswordpassword", "5dd201261ef2fd85"));

        byte[] slt = generateSalt();
        System.out.println("BI: " + String.format("%x", new BigInteger(slt)));
        System.out.println("HEX: " + toHex(slt));
    }

    private static String toHex(byte[] array) throws NoSuchAlgorithmException {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        } else {
            return hex;
        }
    }

    /**
     * Generate a random salt, to be used to create a hash of the password.
     *
     * @return
     * @throws NoSuchAlgorithmException
     */
    private static byte[] generateSalt() throws NoSuchAlgorithmException {
        // VERY important to use SecureRandom instead of just Random
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

        // Generate a 8 byte (64 bit) salt as recommended by RSA PKCS5
        byte[] salt = new byte[8];
        random.nextBytes(salt);

        return salt;
    }
}
package com.asp;

//Java ships with PBKDF2 support
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * http://adambard.com/blog/3-wrong-ways-to-store-a-password/
 */
class Pbkdf2Test {
//  private static final int ITERATIONS = 1000;
    private static final int ITERATIONS = 20000;
//  private static final int KEY_LENGTH = 192; // bits
    private static final int KEY_LENGTH = 160; // bits

    public static String hashPassword(String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        char[] passwordChars = password.toCharArray();
//      byte[] saltBytes = salt.getBytes();
        byte[] saltBytes = new BigInteger(salt, 16).toByteArray();

        PBEKeySpec spec = new PBEKeySpec(passwordChars, saltBytes, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

        byte[] hashedPassword = key.generateSecret(spec).getEncoded();
//      System.out.println("In byte: " + hashedPassword);

        BigInteger bi = new BigInteger(hashedPassword);
//      System.out.println("In BI: " + bi);

        StringBuilder sb = new StringBuilder();
        for (byte b : hashedPassword) {
            sb.append(String.format("%02X ", b));
        }
//      System.out.println("In hex: " + sb.toString());

        return String.format("%x", bi);
    }

    public static void main(String[] args) throws Exception {
        String hashedPassword = hashPassword("passwordpasswordpassword", "40f4cc0f7d8daceb");
        System.out.println("Hashed Password: " + hashedPassword);
//      BigInteger bi = new BigInteger(hashedPassword, 16);
//      System.out.println("In BI: " + bi);
//      System.out.println(String.format("To byte: %s  %s", bi.toByteArray(), "something else"));
    }
}
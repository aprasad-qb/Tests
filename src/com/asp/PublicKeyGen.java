package com.asp;

import java.security.Key;
import java.security.KeyPairGenerator;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class PublicKeyGen {

	public static void main(String[] args) throws Exception {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(512);
		byte[] publicKey = keyGen.genKeyPair().getPublic().getEncoded();
		System.out.println("publicKey: " + new String(publicKey));
		StringBuffer retString = new StringBuffer();
		retString.append("[");
		for (int i = 0; i < publicKey.length; ++i) {
			retString.append(publicKey[i]);
			retString.append(", ");
		}
		retString = retString.delete(retString.length() - 2, retString.length());
		retString.append("]");
		System.out.println(retString); // e.g. [48, 92, 48, .... , 0, 1]


		generateKey();


		System.out.println("Apache Key Gen : " + ApacheApiKeyGen.generateNext("thekey"));
		System.out.println("Apache Key Gen (1) : " + ApacheApiKeyGen.generateNext("thekey"));


		byte[] key = new byte[] {1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0};
		System.out.println("Example Key (b): " + key.hashCode());
		System.out.println("Example Key: " + new String(key));
	}

	private static void generateKey() throws Exception {
		byte[] input = "input".getBytes();
		byte[] ivBytes = "1234567812345678".getBytes();

		Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
		KeyGenerator generator = KeyGenerator.getInstance("AES");
		generator.init(128);
		Key encryptionKey = generator.generateKey();
		System.out.println("key : " + new String(encryptionKey.getEncoded()));

		cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, new IvParameterSpec(ivBytes));
		byte[] cipherText = new byte[cipher.getOutputSize(input.length)];
		int ctLength = cipher.update(input, 0, input.length, cipherText, 0);
		ctLength += cipher.doFinal(cipherText, ctLength);
		Key decryptionKey = new SecretKeySpec(encryptionKey.getEncoded(), encryptionKey.getAlgorithm());

		cipher.init(Cipher.DECRYPT_MODE, decryptionKey, new IvParameterSpec(ivBytes));
		byte[] plainText = new byte[cipher.getOutputSize(ctLength)];
		int ptLength = cipher.update(cipherText, 0, ctLength, plainText, 0);
		ptLength += cipher.doFinal(plainText, ptLength);
		System.out.println("plain : " + new String(plainText));
	}

}

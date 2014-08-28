package com.asp;

import java.security.SecureRandom;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class ApacheApiKeyGen {

	/**
	 * The default hash random size is 512
	 */
	private static final int size = 512;

	/**
	 * The algorithm to use for hashing.
	 * We use the strongest "HmacSHA512" as the default.
	 */
	private static final String alg = "HmacSHA512";

	/**
	 * The string used as the key for hashing
	 */
	private static SecretKeySpec key = null;

	/**
	 * Generates a random string that can be used as an API Key. The string is
	 * generated by creating a random array of bytes, generating an hmac, then
	 * base64 encoding those. All non alphanumeric characters in the base64
	 * encoded result are then replaced with periods ('.') to simplify the
	 * result a bit more. The resulting API Key can be expected to be reasonably
	 * random and suitable for use within a request URI (e.g. key={apikey}).
	 *
	 * @see
	 *         https://svn.apache.org/repos/asf/abdera/abdera2/common/src/main/java/org/apache/abdera2/common/security/ApiKey.java
	 *         https://svn.apache.org/repos/asf/abdera/abdera2/common/src/main/java/org/apache/abdera2/common/security/KeyBase.java
	 */
	public static String generateNext(String keystring) {
		key = secret(keystring.getBytes());

		int len = Math.min(20, size);
//		int len = Math.max(20, size);
		System.out.println("Size: " + size + "  Len: " + len);
		byte[] buf = hmac(randomBytes(len), randomBytes(len));
		buf = Base64.encodeBase64(buf, false, true);

		System.out.println("Apache Key Gen Org: " + new String(buf));

		StringBuilder sb = new StringBuilder();
		for (byte b : buf)
			sb.append(Character.isLetterOrDigit(b) ? (char) b : '.');
		return sb.toString();
	}

	private static byte[] hmac(byte[]... mat) {
		try {
			Mac hmac = Mac.getInstance(alg);
			hmac.init(key);
			for (byte[] m : mat)
				hmac.update(m);
			return hmac.doFinal();
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	private static byte[] randomBytes(int count) {
		SecureRandom random = new SecureRandom();
		byte[] buf = new byte[count];
		random.nextBytes(buf);
		return buf;
	}

	private static SecretKeySpec secret(byte[] key) {
		return new SecretKeySpec(key, "RAW");
	}

}

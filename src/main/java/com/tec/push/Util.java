package com.tec.push;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util {

	public static byte[] toMd5(byte[] bytes) {
		try {
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(bytes);
			return algorithm.digest();
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
}

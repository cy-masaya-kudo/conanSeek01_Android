package com.gency.crypto.aes;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import android.annotation.SuppressLint;
import android.util.Base64;
import android.util.Log;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES式暗号用メソッドクラス
 *
 */
public class GencyAES {

	private static final int KEY_SIZE = 32;
	private static final int IV_SIZE = 16;
	/**
	 * AES暗号 CBC PKCS7Padding
	 *
	 * @param iv Initialization Vector: 16bytes
	 * @return 暗号化したあとのバイナリ配列
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static byte[] encrypt(byte[] buf, String key, String iv) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

		// 秘密鍵を構築します
		SecretKeySpec sksSpec = new SecretKeySpec(GencyAESUtility.paddingStringWithZerotoByte(key, KEY_SIZE), "AES");
		// IV(初期化ベクトル)を構築します
		IvParameterSpec ivSpec = new IvParameterSpec(GencyAESUtility.paddingStringWithZerotoByte(iv, IV_SIZE));
		// 暗号化を行うアルゴリズム、モード、パディング方式を指定します
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
		// 初期化します
		cipher.init(Cipher.ENCRYPT_MODE, sksSpec, ivSpec);
		// 暗号化します
		return cipher.doFinal(buf);
	}

	/**
	 * AES復号 CBC PKCS7Padding
	 *
	 * @param buf 復号化したいバイナリ配列
	 * @param key 秘密鍵
	 * @param iv  Initialization Vector: 16bytes
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @return　復号化したあとのバイナリ配列
	 */
	public static byte[] decrypt(byte[] buf, String key, String iv) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

		// 秘密鍵を構築します
		SecretKeySpec sksSpec = new SecretKeySpec(GencyAESUtility.paddingStringWithZerotoByte(key, KEY_SIZE), "AES");
		// IV(初期化ベクトル)を構築します
		IvParameterSpec ivSpec = new IvParameterSpec(GencyAESUtility.paddingStringWithZerotoByte(iv, IV_SIZE));
		// 暗号化を行うアルゴリズム、モード、パディング方式を指定します
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
		// 初期化します
		cipher.init(Cipher.DECRYPT_MODE, sksSpec, ivSpec);
		// 復号化します
		return cipher.doFinal(buf);
	}
}
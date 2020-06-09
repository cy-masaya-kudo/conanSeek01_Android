package com.gency.crypto.aes;

import android.annotation.SuppressLint;
import android.util.Base64;

import java.io.UnsupportedEncodingException;

public class GencyAESUtility {

	private static final int KEY_SIZE = 32;
	private static final int IV_SIZE = 16;

	/**
	 * 改行なしBase64エンコード
	 *
	 * @param arg
	 * @return
	 */
	@SuppressLint("InlinedApi")
	public static String encodeToStringByBase64(byte[] arg) {
		return Base64.encodeToString(arg, Base64.NO_WRAP);
	}

	/**
	 * 改行なしBase64エンコード文字列をデコードする
	 *
	 * @param arg
	 * @return
	 */
	@SuppressLint("InlinedApi")
	public static byte[] decodeToByteByBase64(String arg) {
		return Base64.decode(arg, Base64.NO_WRAP);
	}

	/**
	 * UTF8エンコード
	 *
	 * @param arg
	 * @return
	 */
	@SuppressLint("InlinedApi")
	public static String encodeToStringByUTF8(byte[] arg) {
		try {
			return new String(arg, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * UTF8エンコード文字列をデコードする
	 *
	 * @param arg
	 * @return
	 */
	@SuppressLint("InlinedApi")
	public static byte[] decodeToByteByUTF8(String arg) {
		try {
			return arg.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Byte型ゼロ値後詰めパディングメソッド
	 *
	 * @param parameter
	 * @param byteNum
	 * @return
	 */
	public static byte[] paddingStringWithZerotoByte(String parameter, int byteNum) {

		if (parameter.length() > byteNum){
			try {
				throw new Exception();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		byte parameterByByte[] = parameter.getBytes();
		byte parameterByByteWithZero[] = new byte[byteNum];

		for (int i = 0; i < byteNum; i++) {
			parameterByByteWithZero[i] = 0;
		}

		for (int i = 0; i < parameterByByte.length; i++) {
			parameterByByteWithZero[i] = parameterByByte[i];
		}

		return parameterByByteWithZero;
	}
}
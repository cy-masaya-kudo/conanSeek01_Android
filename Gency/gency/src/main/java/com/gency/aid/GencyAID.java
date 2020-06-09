package com.gency.aid;

import android.content.Context;
import android.content.SharedPreferences;

import com.gency.crypto.aes.GencyAES;
import com.gency.crypto.aes.GencyAESUtility;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * ユーザー認識用ID管理クラス
 */
public class GencyAID {

	/**
	 * ユーザ識別用IDを取得する。
	 * @param context
	 */
	@Deprecated
	public static String getGencyAID(Context context) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, UnsupportedEncodingException {
		return getGencyAID(context, new GencyAIDConst());
	}

	/**
	 * ユーザ識別用IDを取得する。
	 * @param context
	 * @param consts keyとivなどを設定すること
	 * @return
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws NoSuchAlgorithmException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidKeyException
     * @throws UnsupportedEncodingException
     */
	public static String getGencyAID(Context context, GencyAIDConst consts) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, UnsupportedEncodingException {
		SharedPreferences uuidData = context.getSharedPreferences(consts.AID, Context.MODE_PRIVATE);
		String uuidString = uuidData.getString(consts.AID_CHARA, null);
		if (null == uuidString) {
			uuidString = UUID.randomUUID().toString();
			uuidString = GencyAESUtility.encodeToStringByBase64(GencyAES.encrypt(GencyAESUtility.decodeToByteByUTF8(uuidString), consts.AID_AES_KEY, consts.AID_AES_IV));
			SharedPreferences.Editor editor = uuidData.edit();
			editor.putString(consts.AID_CHARA, uuidString);
			editor.apply();
		}
		uuidString = GencyAESUtility.encodeToStringByUTF8(GencyAES.decrypt(GencyAESUtility.decodeToByteByBase64(uuidString), consts.AID_AES_KEY, consts.AID_AES_IV));
		return uuidString;
	}
}

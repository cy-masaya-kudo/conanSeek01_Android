package com.gency.cybirdid;

import android.content.Context;
import android.content.SharedPreferences;

import com.gency.crypto.aes.GencyAES;
import com.gency.crypto.aes.GencyAESUtility;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * ユーザー認識用ID管理クラス (CybirdCommonUserIdバックアップ用)
 * 注意!!!:メインアプリのtargetSdkVersionは22以下にすること
 */
public class CybirdCommonUserId {

    /**
     * jp.co.cybird.app.android.lib.cybirdid.CybirdCommonUserId.get()にて取得できるUUIDをSharedPreferencesに暗号化して保存、
     * その後取得できる
     * @param context
     * @return uuid
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     * @throws UnsupportedEncodingException
     */

    public static String get(Context context) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, UnsupportedEncodingException{
        SharedPreferences uuidData = context.getSharedPreferences(a(CybirdCommonUserIdConst.GCUID), Context.MODE_PRIVATE);
        String uuidString = uuidData.getString(a(CybirdCommonUserIdConst.GCUID_CHARA), null);
        if (null == uuidString) {
            uuidString = jp.co.cybird.app.android.lib.cybirdid.CybirdCommonUserId.getDeprecate(context);
            if(uuidString == null) return null;
            uuidString = GencyAESUtility.encodeToStringByBase64(GencyAES.encrypt(GencyAESUtility.decodeToByteByUTF8(uuidString), a(CybirdCommonUserIdConst.GCUID_AES_KEY), a(CybirdCommonUserIdConst.GCUID_AES_IV)));
            SharedPreferences.Editor editor = uuidData.edit();
            editor.putString(a(CybirdCommonUserIdConst.GCUID_CHARA), uuidString);
            editor.apply();
        } else {
            // ローカルにUUIDがあったとしても救済処置処理のためCybirdCommonUserId.getを呼び出す。
            // for CybirdUtility_1.0.2
            jp.co.cybird.app.android.lib.cybirdid.CybirdCommonUserId.getDeprecate(context);
        }
        uuidString = GencyAESUtility.encodeToStringByUTF8(GencyAES.decrypt(GencyAESUtility.decodeToByteByBase64(uuidString), a(CybirdCommonUserIdConst.GCUID_AES_KEY), a(CybirdCommonUserIdConst.GCUID_AES_IV)));
        return uuidString;
    }

    private static String a(String arg){
        return Utils.c(arg);
    }
}

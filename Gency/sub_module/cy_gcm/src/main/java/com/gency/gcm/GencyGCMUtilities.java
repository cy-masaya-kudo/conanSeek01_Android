package com.gency.gcm;

import android.content.Context;

import com.gency.commons.log.GencyDLog;
import com.gency.did.GencyDID;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * <h3>PUSH(GCM)の処理を行う。CY用</h3>
 *
 * PUSHの初期化及び設定画面を呼び出すためのユーティリティクラス<br />
 * <br />
 *
 * <br />
 * <b>使い方</b><br />
 * PUSH(GCM)初期化<br />
 * GCMUtilities.runGCM(this);<br />
 * <br />
 * PUSH設定画面の表示<br />
 * GCMUtilities.launchPerfActivity(this);<br />
 *
 * <br />
 * <b>参考URL:</b><br />
 * http://faq.intra.cybird.co.jp/app_support/index.php?Push%C4%CC%C3%CE<br />
 * http://tool.push.sf.intra.cybird.co.jp/<br />
 * http://faq.intra.cybird.co.jp/app_support/index.php?CY%B6%A6%C4%CC%B4%F0%C8%D7%20-%B3%B5%CD%D7-
 */
public class GencyGCMUtilities extends GencyGCMUtilitiesE {

    /**
     * UUID,DUUIDは直接cy_gencydid,cy_cybirdidを使用
     * @param c
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     */
    public static void runGCM(Context c) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        runGCM(c, new GencyGCMTokenRegisterManager(getUUID(c), null, getDUUID(c),false));
    }

    public static void runGCM(Context c, String auuid) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        runGCM(c, new GencyGCMTokenRegisterManager(getUUID(c), auuid, getDUUID(c),false));
    }

    public static void runGCM(Context c, String auuid, boolean isPOPgate) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        GencyGCMUtilitiesE.runGCM(c, new GencyGCMTokenRegisterManager(getUUID(c), auuid, getDUUID(c),isPOPgate));
    }

    public static String getUUID(Context c){
        String uuid = null;
        try{
            uuid = com.gency.cybirdid.CybirdCommonUserId.get(c);
        } catch (Exception e) {
            GencyDLog.e("GencyGCMUtilities",e.toString());
        }
        return uuid;
    }

    public static String getDUUID(Context c){
        String uuid = null;
        uuid = GencyDID.get(c);
        return uuid;
    }
}

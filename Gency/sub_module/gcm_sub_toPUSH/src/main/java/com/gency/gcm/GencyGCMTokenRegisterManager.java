package com.gency.gcm;

import android.content.Context;
import android.content.SharedPreferences;

import com.gency.commons.http.GencyRequestParams;
import com.gency.commons.http.GencyThreadHttpClient;
import com.gency.commons.log.GencyDLog;
import com.gency.crypto.aes.GencyAES;
import com.gency.crypto.aes.GencyAESUtility;
import com.gency.crypto.rsa.GencyRSA;
import com.gency.util.Utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * For Cybird
 */
public class GencyGCMTokenRegisterManager extends GencyGCMTokenRegister {
    protected String mUUID;
    protected String mAUUID;
    protected String mDUUID;

    protected boolean isPOPgate = false;

    // POPgate形式暗号方式（デフォルト）
    final static protected String PUSH_POPGATE_ENCYRPT = "1";
    // ハイブリッド暗号方式
    final static protected String PUSH_HYBRID_ENCYRPT = "2";
    
    private static final String TAG = "G_GCM_TRM";

    private static final String P_KEY = "ahCeiweaH3.der";

    /**
     * duuidは必須
     *
     * @param uuid
     * @param auuid
     * @param duuid
     * @param isPOPgate
     */
    public GencyGCMTokenRegisterManager(String uuid, String auuid, String duuid, boolean isPOPgate) {
        if(duuid == null) throw new IllegalArgumentException("duuid is null.");
        this.mUUID = uuid;
        this.mAUUID = auuid;
        this.mDUUID = duuid;
        this.isPOPgate = isPOPgate;
    }

    @Override
    public void sendRegistrationInfo(Context context, String token, String userAgent) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, UnsupportedEncodingException {
        checkServerURL(context);
        GencyDLog.d(TAG, "sendRegistrationInfo() url: " + GencyGCMConst.REGISTER_URL);
        String registrationId = token;
        if ("".equals(registrationId) == false) {
            registrationId = GencyAESUtility.encodeToStringByUTF8(GencyAES.decrypt(GencyAESUtility.decodeToByteByBase64(registrationId), Utils.c(GencyGCMConst.GCM_AES_KEY), Utils.c(GencyGCMConst.GCM_AES_IV)));
        }
        GencyDLog.d(TAG, "sendRegistrationInfo() registrationId: " + registrationId);

        if ("".equals(registrationId) == false) {
            Boolean willSendMessage = true;
            if(this.mUUID == null) this.mUUID = this.mDUUID;
            String UUID = this.mUUID;

            if (UUID == null || "".equals(UUID)) {
                willSendMessage = false;
                GencyDLog.e(TAG, "CAN NOT GET UUID WHEN REGISTER!");
            }

            GencyDLog.d(TAG, "sendRegistrationInfo() UUID: " + UUID);

            String packageName = context.getApplicationContext().getPackageName();
            if (packageName == null || "".equals(packageName)) {
                willSendMessage = false;
                GencyDLog.e(TAG, "CAN NOT GET PACKAGE NAME WHEN REGISTER!");
            }
            String test = GencyGCMConst.IS_TESTING;

            if (willSendMessage) {
                GencyDLog.d(TAG, "sendRegistrationInfo() willSendMessage: " + true);
                // send info to server
                StringBuilder sb = new StringBuilder();
                sb.append("uuid=" + UUID);
                sb.append("&device_id=" + registrationId);
                sb.append("&product_id=" + packageName);
                sb.append("&user_agent=" + userAgent);
                if(this.mAUUID != null) sb.append("&auuid=" + this.mAUUID);
                if(this.mDUUID != null) sb.append("&duuid=" + this.mDUUID);
                sb.append("&test=" + test);
                String query = sb.toString();
                GencyDLog.v(TAG, "q=" + query);
                GencyRequestParams params = null;
                if(isPOPgate) {
                    params = createRequestParams(context, query);
                } else {
                    params = createRequestParamsAES(context, query);
                }
                GencyDLog.d(TAG, "sendRegistrationInfo() url: " + GencyGCMConst.REGISTER_URL);

                GencyThreadHttpClient threadClient = new GencyThreadHttpClient();
                threadClient.setUserAgent(userAgent);

                // Finally, send request to Cybird PUSH server
                threadClient.post(GencyGCMConst.REGISTER_URL, params);
            } else {
                GencyDLog.d(TAG, "sendRegistrationInfo() willSendMessage: " + false);
            }
        }
    }

    @Override
    public void sendUnregistrationInfo(Context context, String token, String userAgent) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, UnsupportedEncodingException {
        // サーバーに送信する
        checkServerURL(context);
        String registrationId = token;
        if ("".equals(registrationId) ==  false) {
            registrationId = GencyAESUtility.encodeToStringByUTF8(GencyAES.decrypt(GencyAESUtility.decodeToByteByBase64(registrationId), Utils.c(GencyGCMConst.GCM_AES_KEY), Utils.c(GencyGCMConst.GCM_AES_IV)));
        }
        if ("".equals(registrationId) == true) return;

        if(this.mUUID == null) this.mUUID = this.mDUUID;
        String UUID = this.mUUID;

        if (UUID == null || "".equals(UUID)) {
            GencyDLog.e(TAG, "CAN NOT GET UUID WHEN UNREGISTER!");
        }
        String packageName = context.getApplicationContext().getPackageName();
        if (packageName == null || "".equals(packageName)) {
            GencyDLog.e(TAG, "CAN NOT GET PACKAGE NAME WHEN UNREGISTER!");
        }
        String test = GencyGCMConst.IS_TESTING;

        // send info to server
        StringBuilder sb = new StringBuilder();
        sb.append("uuid=" + UUID);
        sb.append("&device_id=" + registrationId);
        sb.append("&product_id=" + packageName);
        if(this.mAUUID != null) sb.append("&auuid=" + this.mAUUID);
        if(this.mDUUID != null) sb.append("&duuid=" + this.mDUUID);
        sb.append("&test=" + test);
        String query = sb.toString();
        GencyDLog.v(TAG, "q=" + query);

        GencyRequestParams params = null;
        if(isPOPgate) {
            params = createRequestParams(context, query);
        } else {
            params = createRequestParamsAES(context, query);
        }

        GencyThreadHttpClient threadClient = new GencyThreadHttpClient();
        threadClient.setUserAgent(userAgent);

        // Finally, send request to Cybird PUSH server
        threadClient.post(GencyGCMConst.UNREGISTER_URL, params);
    }

    /**
     * CYBIRD用:PUSHサーバへ送るパラメータ生成。（POPgate）
     * @param context
     * @param query PUSHサーバへ送るパラメータ
     * @return 暗号化済みのパラメータとデータバージョンをセットしたParams
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     * @throws UnsupportedEncodingException
     */
    private static GencyRequestParams createRequestParams(Context context, String query) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, UnsupportedEncodingException {
        GencyRequestParams params = new GencyRequestParams();
        // POPgateで暗号化する場合
        params.put("v", PUSH_POPGATE_ENCYRPT);
        params.put("q", jp.co.cybird.app.android.lib.commons.security.popgate.Codec.encode(query));
        return params;
    }

    /**
     * CYBIRD用:PUSHサーバへ送るパラメータ生成。（RSA,AESハイブリッド）
     * @param context
     * @param query PUSHサーバへ送るパラメータ
     * @return 暗号化済みのパラメータとデータバージョンをセットしたParams
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     * @throws UnsupportedEncodingException
     */
    private static GencyRequestParams createRequestParamsAES(Context context, String query) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, UnsupportedEncodingException {
        GencyRequestParams params = new GencyRequestParams();
        // Generate random string for AES key and IV
        String aesKey = generateSessionKey(32);
        String aesIV  = generateSessionKey(16);

        GencyDLog.v(TAG, "AES Key = " + aesKey);
        GencyDLog.v(TAG, "AES IV = " + aesIV);
        GencyDLog.v(TAG, "Query = " + query);
        GencyDLog.v(TAG, "params = " + query);

        byte[] plainAesKey = aesKey.getBytes();
        byte[] plainAesIV  = aesIV.getBytes();

        // Encrypt query by AES
        byte[] aesQueryByte = GencyAES.encrypt(query.getBytes(), aesKey, aesIV);
        String aesQuery = GencyAESUtility.encodeToStringByBase64(aesQueryByte);

        // Import RSA public key
        PublicKey publicKey = null;
        try {
            publicKey = GencyRSA.getPublicKey(context, P_KEY);
        } catch (Exception e) {
            GencyDLog.e(TAG,e.toString());
        }

        // Use public key to encrypt key and IV by RSA
        String encryptedAesKey = GencyAESUtility.encodeToStringByBase64(GencyRSA.encryptData(plainAesKey, publicKey));
        String encryptedAesIV  = GencyAESUtility.encodeToStringByBase64(GencyRSA.encryptData(plainAesIV,  publicKey));

        // Parameters of POST request
        params.put("v", PUSH_HYBRID_ENCYRPT);
        params.put("q", aesQuery);
        params.put("k", encryptedAesKey);
        params.put("i", encryptedAesIV);
        return params;
    }

    private static String generateSessionKey(int length) {
        String alphabet =
                new String("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"); //9
        int n = alphabet.length(); //10

        String result = new String();
        Random r = new Random(); //11

        for (int i = 0; i < length; i++) //12
            result = result + alphabet.charAt(r.nextInt(n)); //13

        return result;
    }

    private void checkServerURL(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(GencyGCMConst.PREF_FILE_NAME,Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(GencyGCMConst.PREF_KEY_DOES_INCLUDE_SANDBOX, false)) {
            GencyGCMConst.UNREGISTER_URL = "https://sandbox.push.cybird.ne.jp/unregister";
            GencyGCMConst.REGISTER_URL = "https://sandbox.push.cybird.ne.jp/register";
            GencyGCMConst.TRACK_URL = "https://sandbox.push.cybird.ne.jp/track";
        } else {
            GencyGCMConst.UNREGISTER_URL = "https://push.cybird.ne.jp/unregister";
            GencyGCMConst.REGISTER_URL = "https://push.cybird.ne.jp/register";
            GencyGCMConst.TRACK_URL = "https://push.cybird.ne.jp/track";
        }
    }
}

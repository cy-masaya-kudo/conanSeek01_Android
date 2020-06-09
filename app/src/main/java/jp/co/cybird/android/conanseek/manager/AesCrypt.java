package jp.co.cybird.android.conanseek.manager;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES暗号復号
 */
public class AesCrypt {

    /**
     * 暗号
     */
    public static String encrypt(String plain, String key, String iv) {
        String retString = "";
        try {

            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            byte[] encrypted = cipher.doFinal(plain.getBytes());

            retString = Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception e) {
            Common.logD("encrypt error:"+e.toString());
            return retString;
        }
        return retString;
    }

    /**
     * 復号
     */
    public static String decrypt(String base64String, String key, String iv) {
        String retString = "";
        try {

            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] textByte = Base64.decode(base64String, Base64.DEFAULT);

            byte[] decryptedByte = cipher.doFinal(textByte);

            retString = new String(decryptedByte, "UTF-8");

        } catch (Exception e) {
            return retString;
        }
        return retString;
    }
}

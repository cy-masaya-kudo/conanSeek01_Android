package com.gency.crypto.rsa;

import android.content.Context;
import android.util.Log;

import com.gency.crypto.aes.GencyAESUtility;
import com.gency.crypto.log.GencyDLog;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/**
 * RSA暗号化クラス.
 */
public class GencyRSA {

    private static final String TAG = "GencyRSA";

    private static String RSA_PADDING = "RSA/ECB/PKCS1Padding";

    private static String CYBIRD_RSA_PUBLIC_KEY = "public_key.der";

    private static String RSA_CERTIFICATION_TYPE = "X.509";

    /**
     * RSA暗号化.
     * @param data
     * @param pubKey
     * @return
     */
    public static byte[] encryptData(byte[] data, PublicKey pubKey)
    {
        try
        {
            Cipher cipher = Cipher.getInstance(RSA_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            return cipher.doFinal(data);
        }
        catch (Exception e)
        {
            GencyDLog.e(TAG, e.getMessage());
            return null;
        }
    }

    /**
     * RSA復号
     * @param encryptedData
     * @param priKey
     * @return
     */
    public static byte[] decryptData(byte[] encryptedData, PrivateKey priKey)
    {
        try
        {
            Cipher cipher = Cipher.getInstance(RSA_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, priKey);
            return cipher.doFinal(encryptedData);
        }
        catch (Exception e)
        {
            GencyDLog.e(TAG, e.getMessage());
            return null;
        }
    }

    /**
     * 公開鍵取得
     * @param context
     * @return
     */
    public static PublicKey getPublicKey(Context context) {

        PublicKey serverPublicKey = null;
        try {
            serverPublicKey = getPublicKey(context, CYBIRD_RSA_PUBLIC_KEY);
        } catch (Exception e) {
            GencyDLog.e(TAG, e.getMessage());
        }
        return serverPublicKey;
    }

    /**
     * 公開鍵取得
     * @param context
     * @param fileNamePublicKey
     * @return
     * @throws Exception
     */
    public static PublicKey getPublicKey(Context context, String fileNamePublicKey)
            throws Exception {

        PublicKey serverPublicKey = null;

        try {
            // read file of public key
            InputStream is = context.getResources().getAssets().open(fileNamePublicKey);

            CertificateFactory certFactory = CertificateFactory.getInstance(RSA_CERTIFICATION_TYPE);

            // Generate certification for encryption
            X509Certificate cer = (X509Certificate) certFactory.generateCertificate(is);

            serverPublicKey = cer.getPublicKey();

        } catch (CertificateException e) {
            GencyDLog.e(TAG, "Unable to load certificate " + e.getMessage());
        } catch (FileNotFoundException e){
            GencyDLog.e(TAG,"Server certificate file missing " + e.getMessage());
        } catch (IOException e) {
            GencyDLog.e(TAG, e.getMessage());
        }

        return serverPublicKey;
    }
}

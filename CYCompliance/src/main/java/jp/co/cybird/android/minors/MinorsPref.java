/**
 * Copyright (c) 2015年 CYBIRD Co., Ltd.
 */

package jp.co.cybird.android.minors;

import android.content.Context;
import android.content.SharedPreferences;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import jp.co.cybird.android.utils.Util;

/**
 * SharedPreferencesへのアクセスクラス
 */
public class MinorsPref {

    private static MinorsPref mMinorsSharedPreferences = new MinorsPref();

    private Context mContext = null;

    private static final String A = "pfrpb9hfdb";//PREF_FILE_NAME
    private static final String B = "6nid08jnfg";//PREF_KEY_AGE_REGISTRATION
    private static final String C = "gkd453d4mm";//PREF_KEY_MINOR_AGREEMENT
    private static final String D = "zuhnovc5qr";//PREF_KEY_BIRTH_YEER
    private static final String E = "3z739alw32";//PREF_KEY_BIRTH_MONTH
    private static final String F = "pq1dqq9ylk";//PREF_KEY_BIRTH_INPUT_DAY
    private static final String G = "r8ljsg6e2j";//PREF_KEY_MINOR_CONCENT_DAY

    public static MinorsPref sharedInstance(Context context){
        if(mMinorsSharedPreferences == null){
            mMinorsSharedPreferences = new MinorsPref();
        }
        mMinorsSharedPreferences.mContext = context;
        return mMinorsSharedPreferences;
    }

    /**
     * 年齢を入力したかどうか
     * @return boolean 年齢入力済みかどうか
     */
    public boolean isAgeRegist(int eulaVer) {
        // ここで同意済みかどうか確認 ( YYYYMMDDHH )
        return eulaVer <= mContext.getSharedPreferences(A, Context.MODE_PRIVATE).getInt(B, 0);
    }

    /**
     * 年齢を入力した日を取得できる
     * @return 年齢入力日。もし同意されていなければnull。
     */
    public String getAgeRegistDate(){
        return mContext.getSharedPreferences(A, Context.MODE_PRIVATE).getString(F, null);
    }

    /**
     * 親権者同意済みかどうか
     * @return boolean 同意済みか否か
     */
    public boolean isMinorAgreed(int eulaVer) {
        // ここで同意済みかどうか確認 ( YYYYMMDDHH )
        return eulaVer <= mContext.getSharedPreferences(A, Context.MODE_PRIVATE).getInt(C, 0);
    }

    /**
     * 親権者同意をした日を取得できる
     * @return 親権者同意日。同意されていなければnull。
     */
    public String getMinorAgreementDate(){
        return mContext.getSharedPreferences(A, Context.MODE_PRIVATE).getString(G, null);
    }

    /**
     * 親権者同意をした日を保存
     * @param eulaVer
     */
    public void saveMinorAgreement(int eulaVer, Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat(Util.DATE_FORMAT_01);
        String currentTimeString = sdf.format(Util.convertFromCalendarToDate(calendar));

        // 同意したEULAのバージョンを保存
        SharedPreferences.Editor e = mContext.getSharedPreferences(A, Context.MODE_PRIVATE).edit();
        e.putInt(C, eulaVer);
        e.putString(G, currentTimeString);
        e.commit();
    }

    /**
     *
     * @param context
     * @param year
     * @param month
     * @param eulaVer
     * @return
     */
    public boolean saveBirthYearAndDay(Context context, int year, int month, int eulaVer, Calendar calendar){
        boolean ret = false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(Util.DATE_FORMAT_01);
            String currentTimeString = sdf.format(Util.convertFromCalendarToDate(calendar));

            String encYear = Util.encodeToString(Util.aesEncrypt(String.valueOf(year).getBytes("UTF-8"), Util.c(Consts.K), Util.c(Consts.I)));
            String encMonth = Util.encodeToString(Util.aesEncrypt(String.valueOf(month).getBytes("UTF-8"), Util.c(Consts.K), Util.c(Consts.I)));

            SharedPreferences.Editor e = context.getSharedPreferences(A, Context.MODE_PRIVATE).edit();
            e.putInt(B, eulaVer);
            e.putString(D, encYear);
            e.putString(E, encMonth);
            e.putString(F, currentTimeString);
            ret = e.commit();
        } catch (NoSuchAlgorithmException e) {
        } catch (NoSuchPaddingException e) {
        } catch (InvalidKeyException e) {
        } catch (InvalidAlgorithmParameterException e) {
        } catch (IllegalBlockSizeException e) {
        } catch (BadPaddingException e) {
        } finally {
            return ret;
        }
    }

    /**
     *
     * @return 生まれ年
     */
    public int getBirthYear(){
        int ret = -1;
        try {
            String encYear =  mContext.getSharedPreferences(A, Context.MODE_PRIVATE).getString(D, String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
            String plainYear = new String(Util.aesDecrypt(Util.decodeToString(encYear), Util.c(Consts.K), Util.c(Consts.I)));
            ret = Integer.valueOf(plainYear);
        } catch (NoSuchAlgorithmException e) {
        } catch (NoSuchPaddingException e) {
        } catch (InvalidKeyException e) {
        } catch (InvalidAlgorithmParameterException e) {
        } catch (IllegalBlockSizeException e) {
        } catch (BadPaddingException e) {
        } finally {
            return ret;
        }
    }

    /**
     *
     * @return 生まれ月
     */
    public int getBirthMonth(){
        int ret = -1;
        try {
            String encMonth = mContext.getSharedPreferences(A, Context.MODE_PRIVATE).getString(E, String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1));
            String plainMonth = new String(Util.aesDecrypt(Util.decodeToString(encMonth), Util.c(Consts.K), Util.c(Consts.I)));
            ret = Integer.valueOf(plainMonth);
        } catch (NoSuchAlgorithmException e) {
        } catch (NoSuchPaddingException e) {
        } catch (InvalidKeyException e) {
        } catch (InvalidAlgorithmParameterException e) {
        } catch (IllegalBlockSizeException e) {
        } catch (BadPaddingException e) {
        } finally {
            return ret;
        }
    }

}

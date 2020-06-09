/**
 * Copyright (c) 2015年 CYBIRD Co., Ltd.
 */

package jp.co.cybird.android.utils;

import android.annotation.SuppressLint;
import android.text.format.DateFormat;
import android.util.Base64;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Util {
    public static final String DATE_FORMAT_01 = "yyyyMMdd";

    /**
     * AES暗号 CBC PKCS7Padding
     * @param buf 暗号化したいバイナリ配列
     * @param key 秘密鍵
     * @param iv Initialization Vector: 16bytes
     * @return 暗号化したあとのバイナリ配列
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static byte[] aesEncrypt(byte[] buf, String key, String iv) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        // 秘密鍵を構築します
        SecretKeySpec sksSpec = new SecretKeySpec(key.getBytes(), "AES");
        // IV(初期化ベクトル)を構築します
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
        // 暗号化を行うアルゴリズム、モード、パディング方式を指定します
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        // 初期化します
        cipher.init(Cipher.ENCRYPT_MODE, sksSpec, ivSpec);
        // 暗号化します
        return cipher.doFinal(buf);
    }

    /**
     * AES復号 CBC PKCS7Padding
     * @param buf 復号化したいバイナリ配列
     * @param key 秘密鍵
     * @param iv Initialization Vector: 16bytes
     * @return　復号化したあとのバイナリ配列
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static byte[] aesDecrypt(byte[] buf, String key, String iv) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        // 秘密鍵を構築します
        SecretKeySpec sksSpec = new SecretKeySpec(key.getBytes(), "AES");
        // IV(初期化ベクトル)を構築します
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
        // 暗号化を行うアルゴリズム、モード、パディング方式を指定します
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        // 初期化します
        cipher.init(Cipher.DECRYPT_MODE, sksSpec, ivSpec);
        // 復号化します
        return cipher.doFinal(buf);
    }

    /**
     * 改行なしBase64エンコード
     * @param arg
     * @return
     */
    @SuppressLint("InlinedApi")
    public static String encodeToString(byte[] arg) {
        return Base64.encodeToString(arg, Base64.NO_WRAP);
    }

    /**
     * 改行なしのBase64エンコード文字列をデコードする
     * @param arg
     * @return
     */
    @SuppressLint("InlinedApi")
    public static byte[] decodeToString(String arg) {
        return Base64.decode(arg, Base64.NO_WRAP);
    }

    /**
     * abcdef0123456789から生成されるランダムな文字列
     * @param cnt 文字数
     * @return
     */
    public static String getRandomString(int cnt) {
        final String chars ="abcdef0123456789";
        Random rnd=new Random();
        StringBuffer buf = new StringBuffer();
        for(int i=0;i<cnt;i++){
            int val=rnd.nextInt(chars.length());
            buf.append(chars.charAt(val));
        }
        return buf.toString();
    }

    public static boolean isFuture(int year, int month, Calendar currentCalendar){
        boolean ret = false;
        if(currentCalendar.get(Calendar.YEAR) <= year && (currentCalendar.get(Calendar.MONTH)+1) < month){
            ret = true;
        }
        return ret;
    }

    /**
     * 誕生日を計算 (年齢計算基準日 – 生年月日 ) / 10000
     * @param birthCalendar
     * @return 年齢
     */
    public static int ageCalculation(Calendar birthCalendar, Calendar currentCalendar){
        int ret = 0;
        String currentTimeString = (String) DateFormat.format(DATE_FORMAT_01, currentCalendar);
        int currentTime = Integer.valueOf(currentTimeString);
        String birthTimeString = (String)DateFormat.format(DATE_FORMAT_01,birthCalendar);
        int birthTime = Integer.valueOf(birthTimeString);
        ret = (currentTime - birthTime) / 10000;
        return ret;
    }

    /**
     * 指定した年月のCalendarインスタンスを返却
     * @param year
     * @param month
     * @return 指定月の最終日を設定したCalendarインスタンス
     */
    public static Calendar birthCalendar(int year, int month){
        int tmpMonth = month -1;
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, tmpMonth, 1);
        int maxDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.clear();
        calendar.set(year, tmpMonth, maxDayOfMonth);
        return calendar;
    }


    /**
     *
     * @return 未成年か否か ※まだ同意が得られていないなどであればtrue(未成年者扱い)
     */
    public static boolean isMinor(int year, int month, Calendar currentCalendar){
        boolean ret = true;

        if(year > 0 && year > 0) {
            int age = Util.ageCalculation(Util.birthCalendar(year, month), currentCalendar);
            if(age >= 20) {
                ret = false;
            }
        }
        return ret;
    }

    /**
     * 共通問い合わせ、共通基盤サーバ送信用<br>
     * 年齢をカテゴリ別にする
     * @param version カテゴリバージョン
     * @param age 年齢
     * @return V1（成人：1A、未成年：1B）
     */
    public static String getCategory(int version, int age){
        StringBuilder sb = new StringBuilder();
        sb.append(version);
        if(version == 1){
            if(age >= 20) {
                // 成人
                sb.append("A");
            } else {
                // 未成年
                sb.append("B");
            }
        }
        return sb.toString();
    }

    /**
     * 文字列並べ替えdecodeメソッド
     * 注意；ライブラリ内でのみ使用してください。
     * @param str
     * @return ランダムに見えてた文字列を元に戻したもの
     */
    public static String c(String str){
        final char symCipher[] = {
                '(', 'H', 'Z', '[', '9', '{', '+', 'k', ',', 'o',
                'g', 'U', ':', 'D', 'L', '#', 'S', ')', '!', 'F',
                '^', 'T', 'u', 'd', 'a', '-', 'A', 'f', 'z', ';',
                'b', '\'', 'v', 'm', 'B', '0', 'J', 'c', 'W', 't',
                '*', '|', 'O', '\\', '7', 'E', '@', 'x', '"', 'X',
                'V', 'r', 'n', 'Q', 'y', '>', ']', '$', '%', '_',
                '/', 'P', 'R', 'K', '}', '?', 'I', '8', 'Y', '=',
                'N', '3', '.', 's', '<', 'l', '4', 'w', 'j', 'G',
                '`', '2', 'i', 'C', '6', 'q', 'M', 'p', '1', '5',
                '&', 'e', 'h' };
        StringBuilder b = new StringBuilder();
        b.append("");
        for (char c : str.toCharArray()) {
            for(int j=0;j<symCipher.length;j++){
                if(c == symCipher[j]){
                    String hex = "21";
                    int v = Integer.parseInt(hex, 16);
                    int a = j+v;
                    String l = new String(hexStringToByteArray(Integer.toHexString(a)));
                    b.append(l);
                }
            }
        }
        return b.toString();
    }

    /**
     * 文字列並べ替えencodeメソッド
     * 注意；ライブラリ内でのみ使用してください。
     * @param str
     * @return 文字をランダム文字のように見せたもの
     */
    public static String d(String str){
        final char symCipher[] = {
                '(', 'H', 'Z', '[', '9', '{', '+', 'k', ',', 'o',
                'g', 'U', ':', 'D', 'L', '#', 'S', ')', '!', 'F',
                '^', 'T', 'u', 'd', 'a', '-', 'A', 'f', 'z', ';',
                'b', '\'', 'v', 'm', 'B', '0', 'J', 'c', 'W', 't',
                '*', '|', 'O', '\\', '7', 'E', '@', 'x', '"', 'X',
                'V', 'r', 'n', 'Q', 'y', '>', ']', '$', '%', '_',
                '/', 'P', 'R', 'K', '}', '?', 'I', '8', 'Y', '=',
                'N', '3', '.', 's', '<', 'l', '4', 'w', 'j', 'G',
                '`', '2', 'i', 'C', '6', 'q', 'M', 'p', '1', '5',
                '&', 'e', 'h' };
        StringBuilder b = new StringBuilder();
        b.append("");
        for (char c : str.toCharArray()) {
            for(int j=0;j<symCipher.length;j++){
                String hex = "21";
                int v = Integer.parseInt(hex, 16);
                int a = j+v;
                String l = new String(hexStringToByteArray(Integer.toHexString(a)));
                char d = l.charAt(0);
                if(c == d){
                    b.append(symCipher[j]);
                }
            }
        }
        return b.toString();
    }

    /**
     * hexがそのままString文字列となっているのをByteArrayに変換
     * @param s
     * @return
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    /**
     * CalendarクラスをDateクラスに変換
     * @param calendar
     * @return {@Link Date}
     */
    public static Date convertFromCalendarToDate(Calendar calendar){
        try{
            Date date = calendar.getTime();
            //カレンダー型からDate型に変換
            return date;
        }catch(Exception e){
            return null;
        }
    }
}

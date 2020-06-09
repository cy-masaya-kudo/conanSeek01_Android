package com.gency.util;

import android.app.Activity;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

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
     * uuid形式の文字列の並びになっているかチェック
     * @param uuid
     * @return true:並びはOK
     */
    public static Boolean isUUID(String uuid) {
        String reg = "[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}";

        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(uuid);

        return m.find();
    }
}

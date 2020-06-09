package jp.co.cybird.android.conanseek.manager;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.gency.aid.GencyAID;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import jp.souling.android.conanseek01.Settings;

/**
 * 端末内保存の記録読み書き
 * - SharedPreferencesで記録
 * - 全てAesCryptで暗号化して扱う
 * - 配列やJSONで記録した情報が多いのでGSONを利用している。参考ブログ
 * http://kawaidesu.hatenablog.com/entry/2015/07/22/084642
 */
public class SaveManager {

    /**
     * キー
     * - 返す型を忘れそうなのでここに書いておいた
     */
    public enum KEY {
        AP_UUID__string,
        //CY_UUID__string,
        USER_INFO__string,
        USER_INFO_LAST_LOGIN__string,
        USER_INFO_TIME__string,
        TOP_SHOW_DATE__string,
        SOUND_BGM_DISABLE__boolean,
        SOUND_SE_DISABLE__boolean,
        CARD_ORDER__integer,
        CARD_FAV__integerList,
        CARD_DECK__integerList,
        CARD_DECK_IDX__integer,
        CARD_NEW_SERIAL__integer,
        JIKEN_SHINCHOKU__string,
        JIKEN_UNLOCKED__stringList,
        JIKEN_ANGOU_IDX__string,
        CHOUHEN_SCROLL_INDEX__map,
        KIKIKOMI_INDEX__stringList,
        SHOUGEN__string,
        TUTORIAL__stringList,
        FIRST_KAIWA__boolena,

        DEBUG_SETUP__boolean,
        DEBUG_CARD_ALL_HAVE__boolean,
        DEBUG_KIKIKOMI_ALWAYS__boolean,
        DEBUG_JIKEN_ALL_SHOW__boolean,
        DEBUG_KUNREN_ALL_SHOW__boolean,
        DEBUG_REDOWNLOAD__boolean,

    }

    /**
     * ソート値
     */
    public static final int ORDER_GET = 0;
    public static final int ORDER_NUM = 1;
    public static final int ORDER_RARE = 2;
    public static final int ORDER_FAV = 3;
    public static final int ORDER_TGT = 4;
    public static final int ORDER_STIME = 5;
    public static final int ORDER_SDIR = 6;
    public static final int ORDER_SNUM = 7;
    public static final int ORDER_SCOL = 8;


    //--- cy uuid
    // gency
    public static String cyUUID() {

        String aid = null;
        try {
            aid = GencyAID.getGencyAID(Common.myAppContext);
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return aid;
    }


    //------------------------------------------

    /**
     * デッキ配列取得
     * - 配列で保存しているデッキ情報取得補助
     */
    public static ArrayList<Integer> deckListByDeckIndex(int deckIndex) {

        if (deckIndex < 0) {
            deckIndex = integerValue(KEY.CARD_DECK_IDX__integer, 0);
        }

        ArrayList<Integer> list = integerList(KEY.CARD_DECK__integerList);

        ArrayList<Integer> retArray = new ArrayList<>();

        for (int i = deckIndex * 3; i < (deckIndex + 1) * 3; i++) {
            if (list != null && list.size() > i) {
                retArray.add(list.get(i));
            } else {
                //保存データにない部分は0で埋める
                retArray.add(0);
            }
        }

        return retArray;
    }

    /**
     * デッキ更新補助
     */
    public static void updateDeck(ArrayList<Integer> deck) {

        ArrayList<Integer> list = integerList(KEY.CARD_DECK__integerList);

        ArrayList<Integer> saveArray = new ArrayList<>();

        int deckIndex = integerValue(KEY.CARD_DECK_IDX__integer, 0);

        int k = 0;
        for (int i = 0; i < Settings.totalDecks * 3; i++) {

            if (deckIndex * 3 <= i && i < (deckIndex + 1) * 3) {
                saveArray.add(deck.get(k));
                k++;
            } else {

                if (list != null && list.size() > i) {
                    saveArray.add(list.get(i));
                } else {
                    //保存データにない部分は0で埋める
                    saveArray.add(0);
                }
            }
        }

        SaveManager.updateIntegerList(KEY.CARD_DECK__integerList, saveArray);

    }

    //------------------------------------------ 事件


    /**
     * 事件進捗数値
     */
    public static int jikenShinchoku(String jikenID) {

        //クリア済み
        if (UserInfoManager.jikenCleared(jikenID, true))
            return 999;

        //デバッグ開放
        if (Settings.isDebug) {
            if (SaveManager.boolValue(KEY.DEBUG_JIKEN_ALL_SHOW__boolean, false)) {
                return 999;
            }
        }

        Map<String, Integer> map = map(KEY.JIKEN_SHINCHOKU__string);

        if (map.containsKey(jikenID))
            return map.get(jikenID);

        return 0;
    }

    /**
     * 事件進捗数字更新
     */
    public static void updateJikenShinchoku(String jikenID, int value) {
        int currentShinchoku = jikenShinchoku(jikenID);
        if (currentShinchoku < value)
            updateMapValue(KEY.JIKEN_SHINCHOKU__string, jikenID, value);
    }

    /**
     * 聞き込み済み証言index一覧
     */
    public static ArrayList<Integer> kikikomizumiShougenIndexList(String jikenID) {

        String string = stringValue(KEY.SHOUGEN__string, "");

        Map<String, ArrayList<Integer>> json = new Gson().fromJson(string, new TypeToken<Map<String, ArrayList<Integer>>>() {
        }.getType());

        if (json != null) {
            if (json.containsKey(jikenID)) {
                return json.get(jikenID);
            }
        }
        return new ArrayList<>();
    }

    public static void updateKikikomizumiShougen(String jikenID, Integer sumiIndex) {

        String string = stringValue(KEY.SHOUGEN__string, "");

        Map<String, ArrayList<Integer>> json = new Gson().fromJson(string, new TypeToken<Map<String, ArrayList<Integer>>>() {
        }.getType());

        ArrayList<Integer> list = new ArrayList<>();

        if (json != null) {
            if (json.containsKey(jikenID)) {
                list = json.get(jikenID);
                json.remove(jikenID);
            }
        } else {
            json = new HashMap<>();
        }

        if (list.contains(sumiIndex))
            return;

        list.add(sumiIndex);

        json.put(jikenID, list);

        String jsonString = new Gson().toJson(json);

        updateStringValue(KEY.SHOUGEN__string, jsonString);
    }


    /**
     * 取得済み暗号indexリスト
     */
    public static ArrayList<Integer> gottenAngouIndexList(String jikenID) {

        String string = stringValue(KEY.JIKEN_ANGOU_IDX__string, "");

        Map<String, ArrayList<Integer>> json = new Gson().fromJson(string, new TypeToken<Map<String, ArrayList<Integer>>>() {
        }.getType());

        if (json != null) {
            if (json.containsKey(jikenID)) {
                return json.get(jikenID);
            }
        }
        return new ArrayList<>();
    }

    /**
     * 取得済み暗号文字リスト
     */
    public static ArrayList<String> gottenAngouStringList(String jikenID, String angouText, String kakushiText) {

        ArrayList<Integer> gottenAngouIndexList = gottenAngouIndexList(jikenID);
        //返り値
        ArrayList<String> retList = new ArrayList<>();


        for (int i : gottenAngouIndexList) {
            int trueIndex = i;

            if (kakushiText != null) {
                for (int j = 0; j < kakushiText.length(); j++) {
                    String str = kakushiText.substring(j, j + 1);
                    int hIndex = angouText.indexOf(str);
                    if (hIndex <= trueIndex) {
                        trueIndex++;
                    }
                }
            }

            retList.add(angouText.substring(trueIndex, trueIndex + 1));
        }

        return retList;
    }

    /**
     * 新規暗号取得
     */
    public static ArrayList<String> getNewAngouList(String jikenID, String angouText, String kakushiText) {

        //４回で全ての暗号が手に入るように調整

        //取得済み
        ArrayList<Integer> gottenAngouList = gottenAngouIndexList(jikenID);
        //未取得暗号
        ArrayList<Integer> notGetAngouList = new ArrayList<>();
        //暗号文字列配列
        ArrayList<String> allAngouList = new ArrayList<>();
        //返り値
        ArrayList<String> retList = new ArrayList<>();

        for (int i = 0; i < angouText.length(); i++) {
            allAngouList.add(angouText.substring(i, i + 1));
        }
        ArrayList<Integer> removedeIndexList = new ArrayList<>();
        if (kakushiText != null) {

            for (int i = 0; i < kakushiText.length(); i++) {
                String str = kakushiText.substring(i, i + 1);
                int index = allAngouList.indexOf(str);
                allAngouList.remove(index);
                removedeIndexList.add(new Integer(index));
            }
        }

        //見取得
        for (int i = 0; i < allAngouList.size(); i++) {
            if (!gottenAngouList.contains(i))
                notGetAngouList.add(i);
        }
        Common.logD("allAngouList:"+allAngouList);
        Common.logD("notGetAngouList:"+notGetAngouList);

        //単純4分割
        float div = (float) allAngouList.size() / 4f;
        //残り取得数
        int nokoriCount = notGetAngouList.size();
        //取得済み数
        int sumiCount = gottenAngouList.size();

        //今回獲得できる数
        int konkaiShutokusuu = (int) Math.ceil(div);

        if (sumiCount != 0) {
            if ((int) Math.ceil(div) == sumiCount) {
                konkaiShutokusuu = (int) Math.ceil(div * 2) - (int) Math.ceil(div);
            } else if ((int) Math.ceil(div * 2) == sumiCount) {
                konkaiShutokusuu = (int) Math.ceil(div * 3) - (int) Math.ceil(div * 2);
            } else if ((int) Math.ceil(div * 3) == sumiCount) {
                konkaiShutokusuu = nokoriCount;
            }
        }

        //シャッフル
        Collections.shuffle(notGetAngouList);

        //先頭から必要数分取得
        for (int i = 0; i < konkaiShutokusuu; i++) {
            if (notGetAngouList.size() > i) {
                int index = notGetAngouList.get(i);
                retList.add(allAngouList.get(index));
                gottenAngouList.add(index);
            }
        }
        Common.logD("gottenAngouList:" + gottenAngouList);

        //取得済み更新
        updateAngouList(jikenID, gottenAngouList);

        return retList;
    }

    /**
     * 取得済み暗号リスト更新
     */
    public static void updateAngouList(String jikenID, ArrayList<Integer> list) {

        String string = stringValue(KEY.JIKEN_ANGOU_IDX__string, "");

        Map<String, ArrayList<Integer>> json = new Gson().fromJson(string, new TypeToken<Map<String, ArrayList<Integer>>>() {
        }.getType());

        if (json == null)
            json = new HashMap<>();

        if (!json.containsKey(jikenID))
            json.put(jikenID, new ArrayList<Integer>());

        json.remove(jikenID);
        json.put(jikenID, list);

        String jsonString = new Gson().toJson(json);

        updateStringValue(KEY.JIKEN_ANGOU_IDX__string, jsonString);

    }

    /**
     * 聞き込み可能か
     */
    public static boolean canKikikomi(String jikenID) {
        ArrayList<String> kikikomiList = stringArray(KEY.KIKIKOMI_INDEX__stringList);

        if (Settings.isDebug) {
            if (boolValue(KEY.DEBUG_KIKIKOMI_ALWAYS__boolean, false)) {
                return true;
            }
        }

        if (kikikomiList == null) {
            return false;
        }

        return kikikomiList.contains(jikenID);
    }

    public static void updateCanKikikomi(String jikenID, boolean state) {
        //ArrayList<String> kikikomiList = stringArray(context, KEY.KIKIKOMI_INDEX__stringList);
        updateStringArray(KEY.KIKIKOMI_INDEX__stringList, jikenID, state);
    }


    //------------------------------------------

    /**
     * ブーリアン読み込み
     * - 暗号化しても仕方ないので生
     */
    public static boolean boolValue(SaveManager.KEY key, boolean defValue) {

        SharedPreferences pref = Common.myAppContext.getSharedPreferences(
                Settings.PrefName,
                Context.MODE_PRIVATE
        );

        return pref.getBoolean(key.name(), defValue);
    }

    /**
     * ブーリアン更新
     */
    public static void updateBoolValue(SaveManager.KEY key, boolean value) {

        SharedPreferences pref = Common.myAppContext.getSharedPreferences(
                Settings.PrefName,
                Context.MODE_PRIVATE
        );
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key.name(), value);
        editor.apply();
    }


    //------------------------------------------

    /**
     * 文字列読み込み
     */
    public static String stringValue(SaveManager.KEY key, String defValue) {

        SharedPreferences pref = Common.myAppContext.getSharedPreferences(
                Settings.PrefName,
                Context.MODE_PRIVATE
        );

        String encString = pref.getString(key.name(), defValue);
        if (encString.length() > 0) {
            return AesCrypt.decrypt(encString, Settings.AES_KEY, Settings.AES_IV);
        }
        return defValue;
    }

    /**
     * 文字列更新
     */
    public static void updateStringValue(SaveManager.KEY key, String value) {
        SharedPreferences pref = Common.myAppContext.getSharedPreferences(
                Settings.PrefName,
                Context.MODE_PRIVATE
        );

        String encString = AesCrypt.encrypt(value, Settings.AES_KEY, Settings.AES_IV);

        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key.name(), encString);
        editor.apply();
    }


    //------------------------------------------

    /**
     * 数値読み込み
     */
    public static Integer integerValue(SaveManager.KEY key, Integer defValue) {

        SharedPreferences pref = Common.myAppContext.getSharedPreferences(
                Settings.PrefName,
                Context.MODE_PRIVATE
        );

        return pref.getInt(key.name(), defValue);
    }

    /**
     * 数値更新
     */
    public static void updateInegerValue(SaveManager.KEY key, Integer value) {
        SharedPreferences pref = Common.myAppContext.getSharedPreferences(
                Settings.PrefName,
                Context.MODE_PRIVATE
        );

        pref.edit().putInt(key.name(), value).apply();
    }


    //------------------------------------------

    /**
     * 数値一覧
     */
    public static ArrayList<Integer> integerList(SaveManager.KEY key) {
        ArrayList<Integer> retArray = new ArrayList<>();

        SharedPreferences pref = Common.myAppContext.getSharedPreferences(
                Settings.PrefName,
                Context.MODE_PRIVATE
        );
        String encString = pref.getString(key.name(), "");

        if (encString.length() > 0) {
            String decString = AesCrypt.decrypt(encString, Settings.AES_KEY, Settings.AES_IV);

            retArray = new Gson().fromJson(decString, new TypeToken<ArrayList<Integer>>() {
            }.getType());

            if (retArray == null) {
                retArray = new ArrayList<>();
            }

        }

        return retArray;
    }


    /**
     * 数値配列保存
     * - base64＆暗号化して保存
     */
    public static void updateIntegerList(SaveManager.KEY key, ArrayList<Integer> list) {

        Gson gson = new Gson();
        String jsonString = gson.toJson(list);

        updateStringValue(key, jsonString);
    }

    //------------------------------------------

    /**
     * 数値マップ
     */
    public static Map<String, Integer> map(SaveManager.KEY key) {

        String string = stringValue(key, "");

        if (string.length() > 0) {

            return new Gson().fromJson(string, new TypeToken<Map<String, Integer>>() {
            }.getType());
        }

        return new HashMap<>();
    }


    /**
     * 数値マップ保存
     */
    public static void updateMapValue(SaveManager.KEY key, String mapKey, int mapValue) {

        String string = stringValue(key, "");

        Map<String, Integer> json = new Gson().fromJson(string, new TypeToken<Map<String, Integer>>() {
        }.getType());

        if (json == null)
            json = new HashMap<>();

        if (json.containsKey(mapKey))
            json.remove(mapKey);

        json.put(mapKey, mapValue);

        String jsonString = new Gson().toJson(json);

        updateStringValue(key, jsonString);
    }

    //--------------------


    /**
     * 文字列一覧
     */
    public static ArrayList<String> stringArray(SaveManager.KEY key) {

        String string = stringValue(key, "");

        ArrayList<String> json = new Gson().fromJson(string, new TypeToken<ArrayList<String>>() {
        }.getType());

        if (json != null)
            return json;

        return new ArrayList<>();
    }

    /**
     * 文字列一覧更新
     */
    public static void updateStringArray(SaveManager.KEY key, String value, boolean addFlag) {

        String string = stringValue(key, "");

        Gson gson = new Gson();
        ArrayList<String> json = gson.fromJson(string, new TypeToken<ArrayList<String>>() {
        }.getType());

        if (json == null)
            json = new ArrayList<>();

        if (json.contains(value)) {
            if (addFlag)
                return;
            else
                json.remove(value);
        } else {
            if (addFlag)
                json.add(value);
            else
                return;
        }

        String jsonString = gson.toJson(json);

        updateStringValue(key, jsonString);
    }
}

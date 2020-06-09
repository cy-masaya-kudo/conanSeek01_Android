package jp.co.cybird.android.conanseek.manager;

import android.content.Context;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

import jp.co.cybird.android.conanseek.param.APIResponseParam;
import jp.co.cybird.android.conanseek.param.CardParam;
import jp.souling.android.conanseek01.Settings;

/**
 * UserInfoによる情報読み書き補助
 */
public class UserInfoManager {

    //private static APIResponseParam userInfo;

    public static APIResponseParam responseParam() {
        String jsonString = SaveManager.stringValue(SaveManager.KEY.USER_INFO__string, "");
        return new Gson().fromJson(jsonString, APIResponseParam.class);
    }

    /*
    public static APIResponseParam lastLoginResponseParam(Context context) {
        String jsonString = SaveManager.stringValue(context, SaveManager.KEY.USER_INFO_LAST_LOGIN__string, "");
        return new Gson().fromJson(jsonString, APIResponseParam.class);
    }*/

    public static void updateUserInfoString(String userInfoString) {
        SaveManager.updateStringValue(SaveManager.KEY.USER_INFO__string, userInfoString);

        long nowTime = new Date().getTime();
        String nowStr = String.valueOf(nowTime);
        SaveManager.updateStringValue(SaveManager.KEY.USER_INFO_TIME__string, nowStr);

        APIResponseParam currentInfo = new Gson().fromJson(userInfoString, APIResponseParam.class);
        if (!currentInfo.item.login.toString().equals("false")) {
            SaveManager.updateStringValue(SaveManager.KEY.USER_INFO_LAST_LOGIN__string, userInfoString);
        }
        //userInfo = null;
    }

    public static int coinCount() {
        APIResponseParam responseParam = responseParam();
        return responseParam.item.tsuuka.coin.count;
    }

    public static int meganeSolidCount() {
        APIResponseParam responseParam = responseParam();
        return responseParam.item.tsuuka.megane.count;
    }

    public static int meganeCount() {
        APIResponseParam responseParam = responseParam();

        //UserInfo更新秒
        String thenStr = SaveManager.stringValue(SaveManager.KEY.USER_INFO_TIME__string, "");
        long thenTime = Long.parseLong(thenStr);

        //現在秒
        long currentTime = new Date().getTime();
        //更新してからの経過時間
        int gap = (int)(currentTime - thenTime) / 1000;

        //タイマー
        ArrayList<Integer> timerList = new ArrayList<>();
        timerList.addAll(responseParam.item.tsuuka.megane.timer);

        Common.logD("currentTime:" + currentTime + " " + thenTime + " " + gap);

        //追加個数
        int additional = 0;


        while (true) {

            if (timerList.size() <= 0) break;

            int num = timerList.get(0);
            int cNextTime = num - gap;

            if (cNextTime <= 0) {
                timerList.remove(0);
                additional++;
            } else {
                break;
            }
        }

        return responseParam.item.tsuuka.megane.count + additional;
    }

    public static int heartSolidCount() {
        return responseParam().item.tsuuka.heart.count;
    }

    public static int heartCount() {
        APIResponseParam responseParam = responseParam();

        //UserInfo更新秒
        String thenStr = SaveManager.stringValue(SaveManager.KEY.USER_INFO_TIME__string, "");
        long thenTime = Long.parseLong(thenStr);

        //現在ymd
        SimpleDateFormat df;
        df = new SimpleDateFormat("yyyyMMdd");
        long nowYMD = Common.parseLong(df.format(new Date()));
        long thenYMD = Common.parseLong(df.format(new Date(thenTime)));

        if (nowYMD > thenYMD) {
            return 10;
        } else {
            return responseParam.item.tsuuka.heart.count;
        }
    }

    public static int ticketCount() {
        APIResponseParam responseParam = responseParam();
        return responseParam.item.tsuuka.ticket.count;
    }


    /**
     * 所有カード一覧
     *
     */
    public static ArrayList<APIResponseParam.Item.Card> myCardList() {

        APIResponseParam param = responseParam();
        return param.item.card;
    }


    /**
     * ソートしたカード一覧
     * - orderが-1だと記録しているオーダー値
     */
    public static ArrayList<APIResponseParam.Item.Card> mySortedCardList(int order) {

        ArrayList<APIResponseParam.Item.Card> defaultArray = myCardList();

        ArrayList<APIResponseParam.Item.Card> retArray = new ArrayList<>();

        if (order < 0) {
            order = SaveManager.integerValue(SaveManager.KEY.CARD_ORDER__integer, SaveManager.ORDER_GET);
        }
        Common.logD("mySortedCardList:" + order);


        if (order == SaveManager.ORDER_GET) {
            //入手順
            Collections.sort(defaultArray, new Comparator<APIResponseParam.Item.Card>() {
                @Override
                public int compare(APIResponseParam.Item.Card lhs, APIResponseParam.Item.Card rhs) {

                    Integer num1 = lhs.id;
                    Integer num2 = rhs.id;

                    return num2.compareTo(num1);
                }
            });

            return defaultArray;

        } else if (order == SaveManager.ORDER_NUM) {

            //No順
            Collections.sort(defaultArray, new Comparator<APIResponseParam.Item.Card>() {
                @Override
                public int compare(APIResponseParam.Item.Card lhs, APIResponseParam.Item.Card rhs) {

                    Integer num1 = lhs.card_id;
                    Integer num2 = rhs.card_id;

                    return num1.compareTo(num2);
                }
            });

            return defaultArray;
        } else if (order == SaveManager.ORDER_RARE) {

            //レア度高い順

            //詳細持ちカード情報
            final Map<Integer, CardParam> cardParamArray = CsvManager.cardParamsWithSkillDetail();

            //レア度情報追加
            for (Integer i = 0; i < defaultArray.size(); i++) {
                Integer cardID = defaultArray.get(i).card_id;
                CardParam cardParam = cardParamArray.get(cardID);

                defaultArray.get(i).rareInt = cardParam.rareInt;
            }

            //ソート
            Collections.sort(defaultArray, new Comparator<APIResponseParam.Item.Card>() {
                @Override
                public int compare(APIResponseParam.Item.Card lhs, APIResponseParam.Item.Card rhs) {

                    Integer num1 = lhs.card_id;
                    Integer num2 = rhs.card_id;

                    Integer rare1 = lhs.rareInt;
                    Integer rare2 = rhs.rareInt;

                    if (rare1.equals(rare2)) {
                        return num1.compareTo(num2);
                    }
                    return rare2.compareTo(rare1);
                }
            });

            return defaultArray;

        } else if (order == SaveManager.ORDER_FAV) {

            //お気に入り抽出

            //お気に入り配列
            ArrayList<Integer> favList = SaveManager.integerList(SaveManager.KEY.CARD_FAV__integerList);

            //シリアルIDがお気に入りに入っているもののみ
            if (favList != null) for (int i = 0; i < defaultArray.size(); i++) {

                int serialID = defaultArray.get(i).id;

                if (favList.contains(serialID)) {
                    retArray.add(defaultArray.get(i));
                }
            }


        } else {

            //スキル抽出

            //詳細持ちカード情報
            final Map<Integer, CardParam> cardParamArray = CsvManager.cardParamsWithSkillDetail();


            //対応するスキルのみ濾し取る
            for (int i = 0; i < defaultArray.size(); i++) {

                int cardID = defaultArray.get(i).card_id;
                CardParam cardParam = cardParamArray.get(cardID);

                if (cardParam.skillType == CardParam.SkillType.Time && order == SaveManager.ORDER_STIME) {
                    //time
                    retArray.add(defaultArray.get(i));
                } else if (cardParam.skillType == CardParam.SkillType.Direction && order == SaveManager.ORDER_SDIR) {
                    //direction
                    retArray.add(defaultArray.get(i));
                } else if (cardParam.skillType == CardParam.SkillType.Color && order == SaveManager.ORDER_SCOL) {
                    //color
                    retArray.add(defaultArray.get(i));
                } else if (cardParam.skillType == CardParam.SkillType.Target && order == SaveManager.ORDER_TGT) {
                    //target
                    retArray.add(defaultArray.get(i));
                } else if (cardParam.skillType == CardParam.SkillType.Number && order == SaveManager.ORDER_SNUM) {
                    //Number-Order
                    retArray.add(defaultArray.get(i));
                }
            }
        }

        return retArray;
    }


    /**
     * 該当エリアレベルの訓練クリア回数
     */
    public static int kunrenClearCount(int areaID, int level) {

        ArrayList<APIResponseParam.Item.Kunren> kunrenList = responseParam().item.kunren;

        for (APIResponseParam.Item.Kunren kunren : kunrenList) {
            if (kunren.area_id == areaID && kunren.level == level) {
                return kunren.clear_count;
            }
        }
        return 0;
    }

    public static boolean jikenCleared(String jikenID, boolean debugClearFlag) {

        ArrayList<String> jikenArrayList = responseParam().item.jiken;

        if (debugClearFlag && Settings.isDebug) {
            if (SaveManager.boolValue(SaveManager.KEY.DEBUG_JIKEN_ALL_SHOW__boolean, false)) {
                return true;
            }
        }

        return jikenArrayList.contains(jikenID);
    }


    /**
     * 図鑑に出す所有カード一覧
     */
    public static ArrayList<Integer> zukanList() {

        APIResponseParam param = responseParam();
        return param.item.zukan;
    }

}

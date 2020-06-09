/**
 * Copyright (c) 2015年 CYBIRD Co., Ltd.
 */

package jp.co.cybird.android.minors;

import android.content.Context;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import jp.co.cybird.android.utils.AsyncHttpTask;
import jp.co.cybird.android.utils.Util;

/**
 * 共通基盤にデータ送信用クラス
 */
public class MinorSender {

    private static final String URL_PREFIX = "https://";
    private static final String URL_FQDN = "app.sf.cybird.ne.jp";
    private static final String CY_URL = URL_PREFIX +URL_FQDN +(Consts.isDebugable() ? "/dev/minor?" : "/minor?");
    private static final String SENDPARAM_FORMAT = "pid=%s&uuid=%s&age=%s&adate=%s&pagree=%s&pdate=%s";
    private static final String SENDPARAM_FORMAT2 = "v=%d&q=%s";

    private static CYMinorData d = null;

    /**
     * 共通基盤サーバにデータを送信する。
     * @param context
     * @param managerBase ManagerBaseインスタンス
     * @param uuid 共通基盤サーバへ送りたいuuid
     */
    public static void sendToCYLauncherServer(Context context, ManagerBase managerBase, String uuid){
        sendToCYLauncherServer(context, managerBase, uuid, null);
    }
    /**
     * 共通基盤サーバにデータを送信する。
     * @param context
     * @param managerBase ManagerBaseインスタンス
     * @param uuid 共通基盤サーバへ送りたいuuid
     * @param onResponseReceive 通信結果ハンドリング用
     */
    public static void sendToCYLauncherServer(Context context, ManagerBase managerBase, String uuid, AsyncHttpTask.OnResponseReceive onResponseReceive){
        if((managerBase != null && managerBase.isAgreement()) && uuid != null && !uuid.equals("")) {
            // カテゴリ決定のために年齢取得
            int age = Util.ageCalculation(Util.birthCalendar(managerBase.getBirthYear(), managerBase.getBirthMonth()),managerBase.getCurrentCalendar());

            d = new CYMinorData();
            d.pid = context.getPackageName();
            d.uuid = uuid;
            d.age = Util.getCategory(1, age);
            d.adate = managerBase.getAgeRegistDate();
            d.pagree = String.valueOf((managerBase.isMinorAgreed())? 1 : 0);
            d.pdate = managerBase.getMinorAgreementDate();

            // 送信用パラメータを生成
            String tmpsendString = String.format(SENDPARAM_FORMAT, d.pid, d.uuid, d.age, d.adate, d.pagree, d.pdate);
            String endSendString = null;
            try {
                endSendString = Util.encodeToString(Util.aesEncrypt(tmpsendString.getBytes("UTF-8"), Util.c(Consts.K), Util.c(Consts.I)));
            } catch (NoSuchAlgorithmException e) {
            } catch (NoSuchPaddingException e) {
            } catch (InvalidKeyException e) {
            } catch (InvalidAlgorithmParameterException e) {
            } catch (IllegalBlockSizeException e) {
            } catch (BadPaddingException e) {
            } catch (UnsupportedEncodingException e) {
            }

            String sendString = null;
            if(endSendString == null || endSendString.equals("")) {
                // 暗号化が失敗してたら何もしない
                return;
            } else {
                sendString= String.format(SENDPARAM_FORMAT2, 6, endSendString);
            }

            AsyncHttpTask task = new AsyncHttpTask(context);
            if(onResponseReceive != null) {
                task.setOnResponseReceive(onResponseReceive);
            }
            // GETで送る
            task.execute(CY_URL + sendString);
        }
    }

    private static class CYMinorData {
        /* Bundle ID or Package Name */
        public String pid;
        /* uuid */
        public String uuid;
        /* カテゴリ化された年齢情報 */
        public String age;
        /* 年齢入力日 */
        public String adate;
        /* 親権者同意の有無 */
        public String pagree;
        /* 親権者同意日 */
        public String pdate;
    }
}

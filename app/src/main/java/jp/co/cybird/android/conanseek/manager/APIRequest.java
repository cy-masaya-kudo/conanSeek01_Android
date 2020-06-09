package jp.co.cybird.android.conanseek.manager;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jp.souling.android.conanseek01.R;

/**
 * APIリクエスト
 */
public class APIRequest {

    public boolean connecting = false;

    protected String requestURL;

    public APIDialogFragment.APIName name;

    public Map<String, Object> params = new HashMap<>();

    public String transactionString;

    public Map<String,String> receiptMap;


    /**
     * リクエストURL生成
     * - 付加するパラメーターに不足があればnullを返す
     */
    public String getRequestURL() {

        requestURL = Common.myAppContext.getString(R.string.api);

        switch (name) {
            case VERSION_CHECK:
                requestURL += Common.myAppContext.getString(R.string.api_version_check);
                break;
            case ADD_TOKUTEN:
                requestURL += Common.myAppContext.getString(R.string.api_tokuten_add);
                if (params.get("tokuten_code") == null) {
                    return null;
                }
                break;
            case USER_CREATE:
                requestURL += Common.myAppContext.getString(R.string.api_user_create);
                break;
            case USER_INFO:
                requestURL += Common.myAppContext.getString(R.string.api_user_info);
                break;
            case GACHA_LIST:
                requestURL += Common.myAppContext.getString(R.string.api_gacha_list);
                break;
            case GACHA_FIRE:
                requestURL += Common.myAppContext.getString(R.string.api_gacha_fire);
                if (params.get("proc_id") == null || params.get("delivery_id") == null) {
                    return null;
                }
                if (params.get("proc_id") == Common.myAppContext.getString(R.string.api_proc_gacha_card) && params.get("tsukau_card") == null) {
                    return null;
                }
                break;
            case PRESENT_LIST:
                requestURL += Common.myAppContext.getString(R.string.api_present_list);
                break;
            case PRESENT_FIRE:
                requestURL += Common.myAppContext.getString(R.string.api_present_fire);
                if (params.get("present_id") == null) {
                    return null;
                }
                break;
            case POINT_FUYO:
                requestURL += Common.myAppContext.getString(R.string.api_point_fuyo);
                if (params.get("proc_id") == null) {
                    return null;
                }
                break;
            case COIN_CHARGE:
                requestURL += Common.myAppContext.getString(R.string.api_coin_charge);
                if (receiptMap == null || params.get("proc_id") == null) {
                    return null;
                }
                break;
            case POINT_TRADE:
                requestURL += Common.myAppContext.getString(R.string.api_point_trade);
                if (params.get("proc_id") == null) {
                    return null;
                }
                break;
            case FILE_DOWNLOAD:
                requestURL += Common.myAppContext.getString(R.string.api_file_download);
                if (params.get("zip") == null) {
                    return null;
                }
                break;
            case CONTENT_TRADE:
                requestURL += Common.myAppContext.getString(R.string.api_content_trade);
                if (params.get("proc_id") == null) {
                    return null;
                }
                break;
            case TRANSACTION_COMMIT:
                requestURL += Common.myAppContext.getString(R.string.api_transaction_commit);
                if (transactionString == null || params.get("proc_id") == null) {
                    return null;
                }
                break;
            case KUNREN_CLEAR:
                requestURL += Common.myAppContext.getString(R.string.api_kunren_clear);
                if (params.get("area_id") == null || params.get("level") == null) {
                    return null;
                }
                break;
            case JIKEN_CLEAR:
                requestURL += Common.myAppContext.getString(R.string.api_jiken_clear);
                if (params.get("jiken_id") == null) {
                    return null;
                }
                break;
        }

        return requestURL;
    }

}

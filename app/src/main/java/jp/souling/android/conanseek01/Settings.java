package jp.souling.android.conanseek01;


/**
 * 設定情報
 */
public class Settings {

    /**
     * デバッグ : 本番はfalse
     */
    public static final boolean isDebug = BuildConfig.DEBUG_SETTING;

    /**
     * アプリケーションID
     */
    //public static final String appID = "jp.co.cybird.android.conanseek01";

    /**
     * OS名
     */
    public static final String osName = "android";

    /**
     * ストア名
     */
    public static final String storeName = "android";

    /**
     * AES key
     */
    public static final String AES_KEY = "NWzNxfPxcXWQAbS0DrLxmc7x90XM4QDV";

    /**
     * AES iv
     */
    public static final String AES_IV = "u4d2e6oPimnzYxtx";

    /**
     * TapJoy Key
     */
    //souling_debug
    //public static final String TAPJOY_KEY = "wwjIbSydScaXJVESVYgPJwECmhw6O0qt78cpLdpUg4gLSe5ONemBLXEvhPO2";
    //cy debug
    public static final String TAPJOY_KEY = "nQHV7e19TOWSNBNiJ7acjgEC1uzev7PQVJrDaJV4EarL0RUcQa5yBs9C6jRI";
    //cy release
    //public static final String TAPJOY_KEY = "wwjIbSydScaXJVESVYgPJwECmhw6O0qt78cpLdpUg4gLSe5ONemBLXEvhPO2";

    /**
     * SharedPreference名
     */
    //public static final String PrefName = "pref04_l";
    public static final String PrefName = "pref07_1";

    /**
     * 全カード数
     */
    public static final int totalCards = 186;

    /**
     * デッキ数
     */
    public static final int totalDecks = 5;

    /**
     * 最終事件ID
     */
    //public static final String finalJikenID = "172";
    public static int RC_REQUEST = 10001;

}

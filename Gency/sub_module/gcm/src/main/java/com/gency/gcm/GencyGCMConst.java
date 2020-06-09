package com.gency.gcm;

public class GencyGCMConst {
	
    // 共通部分
	final static String TAG = "COM.GENCY.GCM";

	final static String PARAM_ACTION = "act";
	final static String PARAM_ACTION_NONE = "none";
	final static String PARAM_ACTION_MARKET = "market";
	final static String PARAM_ACTION_BROWSER = "browser";
	final static String PARAM_ACTION_APP = "app";
	final static String PARAM_URL = "url";

	final static String PREF_KEY_REGISTRATION_ID = "lib_gcm_registration_ID";
	final static public String PREF_FILE_NAME = "lib_gcm";

	final static String PREF_KEY_GOOGLE_PLAY_STATUS = "lib_gcm_google_play_status";

	final static String GCM_BUNDLE_NAME = "push";
	static String IS_TESTING = "0";
	
	// GCMIntentService用
	final static String PREF_KEY_WILLSENDNOTIFICATION = "lib_gcm_willSendNotification";
	final static String PREF_KEY_WILLPLAYSOUND = "lib_gcm_willPlaySound";
	final static String PREF_KEY_WILLVIBRATE = "lib_gcm_willVibrate";
	final static String PREF_KEY_CUSTOMIZED = "tickets_fulfilled";

	final static String PREF_KEY_LARGEICON = "large_icon_bitmap";

	final static String PARAM_MESSAGE = "msg";
	final static String PARAM_NUMBER = "num";
	final static String PARAM_NID = "nid";
	final static String PARAM_SND = "snd";

	final static String TRACK_CATEGORY_RECEIVE = "receive";
	protected static String UNREGISTER_URL = "";
	
	// GCMIntermediateActivity用
	final static String TRACK_CATEGORY_TAP = "tap";
	final static String PREF_KEY_IS_NORMAL = "lib_gcm_is_normal";
	// GA
	static String GA_NOTIFICATION_CATEGORY = "通知";
    static String GA_NOTIFICATION_ACTION_TAP = "通知をタッチ";
    static String GA_NOTIFICATION_ACTION_DISPLAY = "通知を表示";

    static String GA_NOTIFICATION_NONE_LABEL = "メール通知";
    static String GA_NOTIFICATION_APP_LABEL = "アプリ起動通知";
    static String GA_NOTIFICATION_URL_LABEL = "ブラウザ通知";
    static String GA_NOTIFICATION_MARKET_LABEL = "Google Play通知";
    static String GA_NOTIFICATION_DISPLAY_LABEL = "通知を用意";


	static final String GCM_AES_KEY = "jdaY@`S4S$0v71`0Tw6Cv^)Wp>wx\\2>l";//o89eOq1m1ZDAMyqD6nutA52GxXnPLrXl
	static final String GCM_AES_IV = "Cc@^Y!7x\\MW|xyuC";//tFO5e3MPLwGJPW7t
	
	// GCMUtilities用
	static String REGISTER_URL = "";
	static String TRACK_URL = "";
	
	// PrefsActivity
	final static public String PREF_KEY_IS_TESTING = "lib_gcm_is_testing";
	final static public String PREF_KEY_DOES_INCLUDE_SANDBOX = "lib_gcm_does_include_sandbox";
	
	public static void switchIsTestingFLAG(boolean isTesting) {
		if (isTesting) {
			IS_TESTING = "1";
		} else {
			IS_TESTING = "0";
		}
	}

	// TODO:URLをセットすること
	public static void switchServerURL(boolean doesIncludeSandbox) {
		if (doesIncludeSandbox) {
			// テスト環境
		} else {
			// 本番環境
		}
	}
}

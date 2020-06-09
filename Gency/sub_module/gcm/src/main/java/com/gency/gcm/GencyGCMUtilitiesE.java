package com.gency.gcm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
//import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Patterns;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.gency.commons.log.GencyDLog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.iid.InstanceID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


/**
 * <h3>PUSH(GCM)の処理を行う。社外提供可能。</h3>
 *
 * PUSHの初期化及び設定画面を呼び出すためのユーティリティクラス<br />
 * <br />
 *
 * <br />
 * <b>使い方</b><br />
 * PUSH(GCM)初期化<br />
 * GCMUtilities.runGCM(this, new GencyGCMTokenRegister());<br />
 * <br />
 * PUSH設定画面の表示<br />
 * GCMUtilities.launchPerfActivity(this);<br />
 *
 * <br />
 * <b>参考URL:</b><br />
 * http://faq.intra.cybird.co.jp/app_support/index.php?Push%C4%CC%C3%CE<br />
 * http://tool.push.sf.intra.cybird.co.jp/<br />
 * http://faq.intra.cybird.co.jp/app_support/index.php?CY%B6%A6%C4%CC%B4%F0%C8%D7%20-%B3%B5%CD%D7-
 */
public class GencyGCMUtilitiesE {
	public final static String CYLIB_GCM_PARAM_ISPUSH = "cylib_gcm_ispush";
	public final static String CYLIB_GCM_PARAM_DATA = "cylib_gcm_data";

	/**
	 * カスタムPUSHか否か
	 */
	public static boolean IS_CUSTOM_PUSH      = false;

	/**
	 * カスタムPUSHで表示された
	 */
	public static boolean CUSTOM_PUSH_VIEWED  = false;

	/**
	 * カスタムPUSHで開始ボタンが押された(JSObjectのstartActivityメソッドが呼ばれた)
	 */
	public static boolean START_BUTTON_PUSHED = false;

	/**
	 * カスタムPUSHで閉じるボタンが押された(JSObjectのcloseDialogメソッドが呼ばれた)
	 */
	public static boolean CLOSE_BUTTON_PUSHED = false;

	/**
	 * 標準のPUSH通知が行われたか否か
	 */
	public static boolean PUSH_BAR_PUSHED     = false;
	public static Bundle  NOTIFICATION_PARAM  =  null;

	private static String mUserAgent;
	private static Runnable mRegistrationRunnable;
	private static Runnable mUnregistrationRunnable;
	private static GencyPrefsActivity.CustomizedSettingsHandler mCustomizedSettingsHandler;
	private static SharedPreferences mPrefs;
	private static boolean allowCustomizationEveryLaunch = false;
	private static GencyGCMTokenRegister mGencyGCMTokenRegister;

	private enum GooglePlayStatus{
		UNKNOWN,
		USER_REJECTED
	}

	/**
	 * register処理をハンドルするRunnableインスタンスをセット
	 * @param r Runnable
	 */
	public static void setRegistrationRunnable(Runnable r) {
		mRegistrationRunnable = r;
	}

	/**
	 * unregister処理をハンドルするRunnableインスタンスをセット
	 * @param r
	 */
	public static void setUnRegistrationRunnable(Runnable r) {
		mUnregistrationRunnable = r;
	}

	/**
	 * GCMの設定が変更された場合にカスタム処理を実行したい場合にセット
	 * @param h
	 */
	public static void setCustomizedSettingsRunnable(GencyPrefsActivity.CustomizedSettingsHandler h) {
		mCustomizedSettingsHandler = h;
	}

	public static void handleCustomizedSettingsChange(boolean b) {
		if (mCustomizedSettingsHandler != null) {
			mCustomizedSettingsHandler.handleSettingsChange(b);
		}
	}

	/**
	 * 設定画面に遷移する
	 * Google Accountをログインしてない場合、toastを表示する
	 * @param context Android context
	 */
	public static void launchPerfActivity(Context context) {
		/*
		TODO GET_ACCOUNTSがGCMで必要なのは4.0.4までだが、
		ICE_CREAM_SANDWICH_MR1	December 2011: Android 4.0.3. ではある。
		コンテンツにあわせて処理を最適化すること。
		*/
		if(android.os.Build.VERSION.SDK_INT < 23) {
			Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
			Account[] accounts = AccountManager.get(context).getAccounts();
			String possibleEmail = "";
			for (Account account : accounts) {
				if (emailPattern.matcher(account.name).matches()) {
					possibleEmail = account.name;
					GencyDLog.d(GencyGCMConst.TAG, possibleEmail);
					Intent intent = new Intent(context, GencyPrefsActivity.class);
					context.startActivity(intent);
				}
			}
			if ("".equals(possibleEmail)) {
				// Google Accountをログインしていない
				// toastを表示します
				String R_LIB_GCM_GOOGLE_ACCOUNT_MISSING_INFO = GencyParameterLoader.getString("LIB_GCM_GOOGLE_ACCOUNT_MISSING_INFO", context);
				Toast.makeText(context, R_LIB_GCM_GOOGLE_ACCOUNT_MISSING_INFO,
						Toast.LENGTH_LONG).show();
			}
		} else {
			Intent intent = new Intent(context, GencyPrefsActivity.class);
			context.startActivity(intent);
		}
	}

	/**
	 * strings.xmlからSENDERIDを取得する
	 * @param context
	 * @return SenderID
	 */
	public static String getSendID(Context context) {
		String sender_id = GencyParameterLoader.getString("LIB_GCM_SENDERID", context);
		if(sender_id.equals("")){
			GencyDLog.e(GencyGCMConst.TAG, "Failed to load string.LIB_GCM_SENDERID.");
		}
		GencyDLog.d(GencyGCMConst.TAG, "sender_id is " + sender_id);
		return sender_id;
	}

	/**
	 * ユーザーをGCMに登録する
	 * prefs変更された時呼ばれる
	 * @param context
	 */
	public static void registerGCM(Context context) throws IOException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException{
		String sender_id = getSendID(context);
		if (sender_id.equals("")) {
			GencyDLog.e(GencyGCMConst.TAG, "SEND_ID not exist.");
		} else {
			try {
				GencyGCMUtilitiesE.initGCM(context);

			} catch (UnsupportedOperationException e) {}
		}
	}

	/**
	 * ユーザーをGCM削除する
	 * prefs変更された時呼ばれる
	 * @param context
	 */
	public static void unregisterGCM(final Context context) throws IOException {

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					InstanceID.getInstance(context).deleteInstanceID();
					sendUnregistrationInfo(context);
					deleteLocalRegistrationID(context);
				} catch (Exception bug) {
					bug.printStackTrace();
				}

			}
		});
		thread.start();
	}

	/**
	 * notificationからのurlを解析する
	 * 例：app://jp.co.cybird.android.lib.gcm.sample/PARAMETER1/PARAMETER2
	 * PARAMETER1/PARAMETER2を返す
	 * 情報が無い場合、空を返す
	 * @param intent
	 * @return
	 */
	public static String parseParametersString(Intent intent) {
		String parametersString = "";
		Bundle extras = intent.getExtras();
		if (extras != null) {
			Boolean isFromPush = extras.getBoolean(GencyGCMConst.GCM_BUNDLE_NAME, false);
			GencyDLog.e(GencyGCMConst.TAG, String.valueOf(isFromPush));
			if (isFromPush == true) {
				GencyDLog.e(GencyGCMConst.TAG, "Get parameters from bundle");
				Intent pushIntent = intent;
				if(pushIntent != null && pushIntent.getData() != null) {
					try {
						String uriWithParametersString = pushIntent.getData().toString();
						if (uriWithParametersString == null) uriWithParametersString ="";
						if (uriWithParametersString.startsWith("app://")) {
							int j = uriWithParametersString.indexOf("/", 6);
							if (j > 5) {
								parametersString = uriWithParametersString.substring(j + 1);
								GencyDLog.e(GencyGCMConst.TAG, "Parameters  is " + parametersString);
							}
						}
						GencyDLog.e(GencyGCMConst.TAG, "Parameters String is " + uriWithParametersString);
					} catch (IndexOutOfBoundsException e){
						GencyDLog.e(GencyGCMConst.TAG, "ERROR.");
					}

				}
			}
		}
		return parametersString;
	}

	/**
	 * ユーザー同意したら、registrationIdを検証して、サーバーに送信します
	 * @param c
	 * @param register RegistrationTokenを取得した際にコールバックしてくれる
	 */
	public static void runGCM(Context c, GencyGCMTokenRegister register) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
		mPrefs = c.getSharedPreferences(GencyGCMConst.PREF_FILE_NAME, Context.MODE_PRIVATE);
		mGencyGCMTokenRegister = register;
		boolean willSendNotif = mPrefs.getBoolean(GencyGCMConst.PREF_KEY_WILLSENDNOTIFICATION, true);
		if (willSendNotif) {
			try {
				GencyGCMUtilitiesE.setUserAgent(new WebView(c).getSettings().getUserAgentString());
			} catch (Exception e) {
				String defaultUseragent = System.getProperty("http.agent");
				if (defaultUseragent == null) defaultUseragent = "GCM lib";
				GencyGCMUtilitiesE.setUserAgent(defaultUseragent);
			}
			try {
				GencyGCMUtilitiesE.initGCM(c);

			} catch (UnsupportedOperationException e) {} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * PUSH（GCM）で起動したか否か
	 *
	 * @param activity
	 * @return
	 */
	public static int isPush(Activity activity){
		Context context = activity.getApplicationContext();

		SharedPreferences packagePrefs = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);

		return packagePrefs.getInt(CYLIB_GCM_PARAM_ISPUSH, 0);
	}

	/**
	 * PUSH(GCM)で受け取ったパラメータをパースする
	 * @param activity
	 * @return
	 */
	public static String parseParametersString(Activity activity) {

		SharedPreferences packagePrefs = activity.getSharedPreferences(
				activity.getPackageName(), Context.MODE_PRIVATE);
		String returnString = packagePrefs.getString(CYLIB_GCM_PARAM_DATA, "");

		Editor editor = packagePrefs.edit();
		editor.remove(CYLIB_GCM_PARAM_ISPUSH);
		editor.remove(CYLIB_GCM_PARAM_DATA);
		editor.commit();

		return returnString;
	}

	/**
	 * 端末を登録する時と毎回起動する時呼ぶ
	 * @param context
	 */
	public static void sendRegistrationInfo(Context context, boolean doesAllowCustmization) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, UnsupportedEncodingException {
		String registrationId = getLocalRegistrationID(context);
		if(mGencyGCMTokenRegister != null) {
			mGencyGCMTokenRegister.sendRegistrationInfo(context, registrationId, mUserAgent);
		}

		if ("".equals(registrationId) == false) {
			if (mRegistrationRunnable != null && doesAllowCustmization == true) {
				Thread myThread = new Thread(mRegistrationRunnable);
				myThread.start();
			}
		}
	}

	/**
	 * ユーザーがGCMをオプトアウトした際にサーバーへ送信し、処理を行う
	 * @param context Android context
	 */
	public static void sendUnregistrationInfo(Context context) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, UnsupportedEncodingException {
		// サーバーに送信する
		String registrationId = getLocalRegistrationID(context);
		if(mGencyGCMTokenRegister != null) {
			mGencyGCMTokenRegister.sendUnregistrationInfo(context, registrationId, mUserAgent);
		}

		// 各アプリカスタマイズ処理
		if (mUnregistrationRunnable != null) {
			Thread myThread = new Thread(mUnregistrationRunnable);
			myThread.start();
		}
	}

	/**
	 * レジストレーションIDを取得
	 * @param c Android context
	 * @return レジストレーションID
	 */
	public static String getLocalRegistrationID(Context c) {
		SharedPreferences prefs = c.getSharedPreferences(GencyGCMConst.PREF_FILE_NAME, Context.MODE_PRIVATE);
		return prefs.getString(GencyGCMConst.PREF_KEY_REGISTRATION_ID, "");
	}

	public static boolean deleteLocalRegistrationID(Context context) {
		Editor sharedata = context.getSharedPreferences(GencyGCMConst.PREF_FILE_NAME, Context.MODE_PRIVATE).edit();
		return sharedata.remove(GencyGCMConst.PREF_KEY_REGISTRATION_ID).commit();
	}

	/**
	 * ユーザーエージェントを取得
	 * @param context Android context
	 * @return ユーザーエージェント
	 */
	public static String getUserAgent(Context context) {
		return mUserAgent ==  null ? "GCM lib" : mUserAgent;
	}

	/**
	 * ユーザーエージェントをセット
	 * @param userAgent
	 */
	public static void setUserAgent(String userAgent) {
		// 端末デフォルトUA以外を設定したい場合
		GencyGCMUtilitiesE.mUserAgent = userAgent;
	}

	/**
	 * Unityか否か
	 * @param context
	 * @return
	 */
	public static boolean isUnity(Context context){
		return GencyParameterLoader.getBool("LIB_GCM_IS_UNITY", context);
	}

	/**
	 * PUSHを受け取るか否かをセット<br />
	 * <br />
	 * 共通の設定ダイアログ以外からGCMの設定を制御する場合に使用
	 * @param willSendNotification true = 受け取る / false = 受け取らない
	 */
	public static void setWillSendNotification(boolean willSendNotification) {
		if (mPrefs == null) return;
		Editor editor = mPrefs.edit();
		editor.putBoolean("lib_gcm_willSendNotification", willSendNotification).commit();
	}

	/**
	 * PUSHを受け取る設定か否かを確認
	 * @return true = 受け取る / false = 受け取らない
	 */
	public static boolean willSendNotification() {
		if (mPrefs == null) return false;
		return mPrefs.getBoolean("lib_gcm_willSendNotification", true);
	}

	/**
	 * 音を鳴らすか否かをセット<br />
	 * <br />
	 * 共通の設定ダイアログ以外からGCMの設定を制御する場合に使用
	 * @param willPlaySound true = 鳴らす/ false = 鳴らさない
	 */
	public static void setWillPlaySound(boolean willPlaySound) {
		if (mPrefs == null) return;
		Editor editor = mPrefs.edit();
		editor.putBoolean("lib_gcm_willPlaySound", willPlaySound).commit();
	}

	/**
	 * 音を鳴らす設定か否か
	 * @return true = 鳴らす / false = 鳴らさない
	 */
	public static boolean willPlaySound() {
		if (mPrefs == null) return false;
		return mPrefs.getBoolean("lib_gcm_willPlaySound", false);
	}

	/**
	 * バイブレーションを実行するか否かをセット
	 * <br />
	 * 共通の設定ダイアログ以外からGCMの設定を制御する場合に使用
	 * @param willVibrate true = 実行する/ false = 実行しない
	 */
	public static void setWillVibrate(boolean willVibrate) {
		if (mPrefs == null) return;
		Editor editor = mPrefs.edit();
		editor.putBoolean("lib_gcm_willVibrate", willVibrate).commit();
	}

	/**
	 * バイブレーションを実行するか否か
	 * @return true = 実行する / false = 実行しない
	 */
	public static boolean willVibrate() {
		if (mPrefs == null) return false;
		return mPrefs.getBoolean("lib_gcm_willVibrate", false);
	}

	public static void allowCustomizationEveryLaunch() {
		allowCustomizationEveryLaunch = true;
	}

	public static boolean getAllowCustomizationEveryLaunch() {
		return allowCustomizationEveryLaunch;
	}

	/**
	 * インスタンスIDを取得
	 * @param c Android context
	 * @return インスタンスID
	 */
	public static String getInstanceID(Context c) {
		return InstanceID.getInstance(c).getId();
	}

	/**
	 * GCMを初期化
	 * 登録してないユーザーを登録する
	 * @param context
	 */
	private static void initGCM(Context context) throws IOException, NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Activity a = (Activity) context;

		// サーバに負荷をかけないように、アクセスしない
		try {

			Intent i = a.getIntent();
			Bundle b = i.getExtras();
			if (b != null) {
				Boolean flag = b.getBoolean(GencyGCMConst.GCM_BUNDLE_NAME, false);
				if (flag == true) return;
			}
		} catch (Exception e) {
		}

		// GooglePlayServices が 可能かどうかのチェック
		GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

		int playServiceStatus = googleApiAvailability.isGooglePlayServicesAvailable(context);

		// 通常処理
		if (playServiceStatus == ConnectionResult.SUCCESS) {
			// Start IntentService to register this application with GCM.
			Intent intent = new Intent(context, GencyRegistrationIntentService.class);
			context.startService(intent);
		}
		// GooglePlayServiceが使えないの場合
		else {
			// ユーザ地震がインストールしたら解決できるの場合
			if (googleApiAvailability.isUserResolvableError(playServiceStatus)) {

				// 「今後表示しない」をユーザが以前押しましたか
				SharedPreferences settings = context.getSharedPreferences(GencyGCMConst.PREF_FILE_NAME, Context.MODE_PRIVATE);

				int isUserRejected = settings.getInt(GencyGCMConst.PREF_KEY_GOOGLE_PLAY_STATUS, GooglePlayStatus.UNKNOWN.ordinal());

				// 押しましたら、辞めます
				if (GooglePlayStatus.USER_REJECTED.ordinal() == isUserRejected) {
					return;
				}

				// 押したことがなければ、もう一回「GooglePlayServiceをインストール」と表示します
				GencyGCMUtilitiesE.getGooglePlayServiceDialog(playServiceStatus, a, context).show();

			} else {
				// ユーザでは対応不可
				return;
			}
		}
	}

	/**
	 * Googleが用意した「GooglePlay開発者サービス」をインストールのポップアップ
	 *
	 * @param context
	 */
	public static Dialog getGooglePlayServiceDialog(final int retCode, final Activity activity, final Context context) {

		Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(activity, retCode, 1, new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				promptWarningDialog(retCode, activity, context).show();
			}
		});

		return dialog;
	}

	/**
	 * ユーザが「GooglePlay開発者サービス」をインストールしたくないのポップアップ
	 *
	 * @param context
	 */
	private static AlertDialog.Builder promptWarningDialog(final int retCode, final Activity activity, final Context context){

		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setTitle(context.getString(R.string.LIB_GCM_DIALOG_WARNING_TITLE));

		builder.setMessage(R.string.LIB_GCM_DIALOG_WARNING_BODY);

		builder.setPositiveButton(R.string.LIB_GCM_DIALOG_WARNING_INSTALL,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						GencyGCMUtilitiesE.getGooglePlayServiceDialog(retCode, activity, context).show();
					}
				});

		builder.setNegativeButton(R.string.LIB_GCM_DIALOG_WARNING_REJECT,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
										int which) {
						SharedPreferences settings = context.getSharedPreferences(GencyGCMConst.PREF_FILE_NAME, Context.MODE_PRIVATE);
						final SharedPreferences.Editor editor = settings.edit();

						editor.putInt(GencyGCMConst.PREF_KEY_GOOGLE_PLAY_STATUS, GooglePlayStatus.USER_REJECTED.ordinal());
						editor.commit();
					}
				});

		builder.setNeutralButton(R.string.LIB_GCM_DIALOG_WARNING_PENDING,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						return;
					}
				});

		builder.setCancelable(false);

		return builder;
	}

	/**
	 * ユーザに「GooglePlay開発者サービス」をインストールするの提示を表示するかのフラグをリセットする
	 *
	 * @param context
	 */
	public static void resetGooglePlayServicePrompt(Context context) {
		SharedPreferences settings = context.getSharedPreferences(GencyGCMConst.PREF_FILE_NAME, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = settings.edit();

		editor.putInt(GencyGCMConst.PREF_KEY_GOOGLE_PLAY_STATUS, GooglePlayStatus.UNKNOWN.ordinal());
		editor.commit();
	}

	/**
	 * アプリケーション名を取得
	 * @param appContest
	 */
	public static String getApplicationName(Context appContest) {
		final PackageManager pm = appContest.getApplicationContext().getPackageManager();
		ApplicationInfo ai;
		try {
			ai = pm.getApplicationInfo( appContest.getPackageName(), 0);
		} catch (final NameNotFoundException e) {
			ai = null;
		}
		String default_application_name = "(unknown)";
		final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : default_application_name);
		return applicationName;
	}



	/**
	 * ノーティフィケーションを生成表示
	 * @param appContext
	 * @param extras
	 */
	@SuppressWarnings("deprecation")
	public static void addNotification(Context appContext, Bundle extras) {
		GencyDLog.d("GencyGCMUtilitiesE", "addNotification");
		String message = "";
		String snd = "";
		int nid = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
		GencyDLog.e(GencyGCMConst.TAG, "Init nid:" + nid);
		int number = 0;
		if (extras.containsKey(GencyGCMConst.PARAM_MESSAGE)) {
			message = extras.getString(GencyGCMConst.PARAM_MESSAGE);
		}
		if (extras.containsKey(GencyGCMConst.PARAM_NUMBER)) {
			number = Integer.parseInt(extras.getString(GencyGCMConst.PARAM_NUMBER));
		}
		if (extras.containsKey(GencyGCMConst.PARAM_NID)) {
			nid = Integer.parseInt(extras.getString(GencyGCMConst.PARAM_NID));
		}
		if (extras.containsKey(GencyGCMConst.PARAM_SND)) {
			snd = extras.getString(GencyGCMConst.PARAM_SND);
		}
		String notificationTitleOnNotificationbar = message;
		String notificationTitle = getApplicationName(appContext);
		String notificationContents = message;

		appContext.getApplicationContext();
		NotificationManager nm = (NotificationManager)appContext.getSystemService(Context.NOTIFICATION_SERVICE);
		int icon_ID = appContext.getApplicationContext().getApplicationInfo().icon;

		Intent i = new Intent();
		Boolean isActionValid = true;

		if(GencyGCMUtilitiesE.isUnity(appContext)){
			GencyDLog.d("DEBUG", "GCMProxyActivity");
			i = new Intent(appContext, com.gency.gcm.GencyGCMUnityProxyActivity.class);
		}else{
			GencyDLog.d("GencyGCMUtilitiesE", "GencyGCMIntermediateActivity");
			i = new Intent(appContext, GencyGCMIntermediateActivity.class);
		}
		i.putExtras(extras);
		i.putExtra("push", true);

		if (isActionValid == true) {
			GencyDLog.d("GencyGCMUtilitiesE", "isActionValid is true");

			PendingIntent contentIntent = PendingIntent.getActivity(appContext.getApplicationContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

			NotificationCompat.Builder builder = new NotificationCompat.Builder(
					appContext);
			builder.setTicker(notificationTitleOnNotificationbar);
			builder.setNumber(number);
			builder.setContentIntent(contentIntent);
			// 小アイコン
			if (mSmallIconId != 0) {
				builder.setSmallIcon(mSmallIconId);
			} else {
				builder.setSmallIcon(icon_ID);
			}
			// 大アイコン
			if (mLargeIconId != 0) {
				Bitmap largeIcon = BitmapFactory.decodeResource(appContext.getResources(), mLargeIconId);
				builder.setLargeIcon(largeIcon);
			}
			// アイコンの背景色
			if (mIconBgColor != 0) {
				builder.setColor(mIconBgColor);
			}
			// Notificationを開いたときに表示されるタイトル
			builder.setContentTitle(notificationTitle);
			// Notificationを開いたときに表示されるサブタイトル
			builder.setContentText(notificationContents);
			// 通知するタイミング
			builder.setWhen(System.currentTimeMillis());
			// タップするとキャンセル(消える)
			builder.setAutoCancel(true);

			SharedPreferences prefs = appContext.getSharedPreferences(GencyGCMConst.PREF_FILE_NAME, Context.MODE_PRIVATE);
			boolean willPlaySound = prefs.getBoolean(GencyGCMConst.PREF_KEY_WILLPLAYSOUND, false);
			boolean willVibrate = prefs.getBoolean(GencyGCMConst.PREF_KEY_WILLVIBRATE, false);

			if (willVibrate == true && willPlaySound == true) {
				builder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND);
			} else if(willVibrate == true) {
				builder.setDefaults(Notification.DEFAULT_VIBRATE);
			} else if(willPlaySound == true) {
				builder.setDefaults(Notification.DEFAULT_SOUND);
			}

			if (willPlaySound == true) {
				if ("".equals(snd) == false) {
					GencyDLog.e(GencyGCMConst.TAG, "sound uri is  " + "android.resource://" + appContext.getPackageName() + "/raw/" + snd);
					int doesSoundExist = appContext.getResources().getIdentifier(snd, "raw", appContext.getPackageName());
					GencyDLog.e(GencyGCMConst.TAG, "doesSoundExist is " + doesSoundExist);
					if (doesSoundExist != 0) {
						builder.setSound(Uri.parse("android.resource://" + appContext.getPackageName() + "/raw/" + snd));
					}
				}
			}

			Notification n = builder.build();
			nm.notify(nid, n);
		}
	}

	//private static boolean hasLargeIcon = false;

	private static int mSmallIconId = 0;
	private static int mLargeIconId = 0;
	private static int mIconBgColor = 0;

	/**
	 * 通知アイコン(大)を設定する<br/>
	 * Application#onCreate()からコールしてください.<br/>
	 * Android4.0未満では反映されません.<br/>
	 * @param context アプリケーションコンテキスト
	 * @param resid リソースID
     */
	public static void setLargeIcon(Context context, int resid) {
		mLargeIconId = resid;
	}

//	/**
//	 * 通知アイコン(大)を設定する
//	 * Application#onCreate()からコールしてください.<br/>
//	 * Android4.0未満では反映されません.<br/>
//	 * @param context アプリケーションコンテキスト
//	 * @param bitmap Bitmap形式の画像リソース
//	 */
//	public static void setLargeIcon(Context context, Bitmap bitmap) {
//		if (bitmap != null) {
//			hasLargeIcon = true;
//			saveLargeIcon(context, bitmap);
//		} else {
//			hasLargeIcon = false;
//		}
//	}

	/**
	 * 通知バーに表示するアイコン(小)を設定する.
	 * Application#onCreate()からコールしてください.<br/>
	 * Android5.0以上をサポートする場合、
	 * モノクロで表示できるアイコン（アルファ値のみを使用します）を指定してください.<br/>
	 * @param context アプリケーションコンテキスト
	 * @param resid リソースID
	 */
	public static void setSmallIcon(Context context, int resid) {
		mSmallIconId = resid;
	}

	/**
	 * 通知バーを開いた時に表示されるアイコン(小)の背景色を設定する
	 * Application#onCreate()からコールしてください.<br/>
	 * Android4.0未満では反映されません.<br/>
	 * @param context アプリケーションコンテキスト
	 * @param argb 背景色
	 */
	public static void setIconBgColor(Context context, int argb) {
		mIconBgColor = argb;
	}

	static void saveLargeIcon(Context context, Bitmap bitmap) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		String bitmapStr = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

		SharedPreferences pref = context.getSharedPreferences(GencyGCMConst.PREF_FILE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(GencyGCMConst.PREF_KEY_LARGEICON, bitmapStr);
		editor.apply();
	}

	static Bitmap loadLargeIcon(Context context) {
		
		SharedPreferences pref = context.getSharedPreferences(GencyGCMConst.PREF_FILE_NAME, Context.MODE_PRIVATE);
		String s = pref.getString(GencyGCMConst.PREF_KEY_LARGEICON, "");
		if (!s.equals("")) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			byte[] bytecode = Base64.decode(s, Base64.DEFAULT);
			return BitmapFactory.decodeByteArray(bytecode, 0, bytecode.length).copy(Bitmap.Config.ARGB_8888, true);
		}
		return null;
	}

}
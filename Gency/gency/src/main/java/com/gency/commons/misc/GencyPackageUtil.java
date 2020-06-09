package com.gency.commons.misc;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;

import com.gency.commons.log.GencyDLog;
//import com.gency.commons.tracker.GencyTrackerWrapper;

/***
 * <h3>アプリのインストールの処理を行うクラス</h3>
 */
public class GencyPackageUtil {

	/**
	 * インストール済みか否かを確認
	 *
	 * パッケージ名を指定して端末上にアプリケーションが インストール済みか否かを判定する機能を提供
	 *
	 * @param context Android context
	 * @param packageName
	 *            チェックするアプリケーションのパッケージ名
	 * @return boolean インストール済みか否かを返す
	 */
	public static boolean isInstalled(Context context, String packageName) {
		try {
			context.getPackageManager().getApplicationInfo(packageName, 0);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}
	
	/**
	 * market://から始まるURLを処理する関数
	 * @param context Android context
	 * @param url e.g. market://details?id=package_name&param=value
	 */
	public static void launchActivity(Context context, String url) {
		String packageNameWithParam = url.substring("market://details?id=".length());
		int i = packageNameWithParam.indexOf("&");
		if (i != -1) {
			// パラメータがある
			launchActivity(context, packageNameWithParam.substring(0, i), packageNameWithParam.substring(i));
		} else {
			launchActivity(context, packageNameWithParam, "");
		}
	}

	/**
	 * アプリを起動する
	 *
	 * パッケージ名とActivityクラス名を指定してアプリケーションを起動する機能を提供
	 *
	 * @param context
	 *            Android context
	 * @param packageName
	 *            アプリケーションのパッケージ名
	 * @param param
	 *            パラメータ
	 */
	public static void launchActivity(Context context, String packageName, String param) {
		Intent launchIntent =  null;
		String category = "アプリリンク";
		String labelMarket = "マーケット";
		String labelApp = "アプリ起動";
		try{
			launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
		}catch (Exception e){
		    e.printStackTrace();
		}
		if (launchIntent != null){
			// LaunchIntentが取得できた場合はActivityを起動
//			GencyTrackerWrapper.sendEvent(category, packageName, labelApp, 1L);
			launchIntent.putExtra("CYReferer", context.getPackageName());
			launchIntent.putExtra("CYReferer_params", param);
			context.startActivity(launchIntent);
		}else{
			// LaunchIntentが取得できない場合はマーケットに飛ばず
//			GencyTrackerWrapper.sendEvent(category, packageName, labelMarket, 1L);
			goMarket(context, packageName, param);
		}
	}
	
	/**
	 * CYRefererが設定された場合、送信します
	 * @param i referreが設定されたintent
	 */
	public static void trackIfFromCYReferer(Intent i) {
		String fromPackageName = i.getStringExtra("CYReferer");
		if (fromPackageName != null) {
			i.removeExtra("CYReferer");
			String param = i.getStringExtra("CYReferer_params");
//			GencyTrackerWrapper.sendEvent("CYReferer", fromPackageName, param, 1L);
		}
	}	

	/**
	 * Google Play Storeに遷移
	 *
	 * パッケージ名を指定してGoogle Play Storeに遷移する機能を提供
	 *
	 * @param context Android context
	 * @param packageName アプリケーションのパッケージ名
	 */
	public static void goMarket(Context context, String packageName, String param) {
		if (param == null) param = "";
		Uri uri = Uri.parse("market://details?id=".concat(packageName).concat(param));
		GencyDLog.d("DEBUG:goMarket", "uri: " + uri.toString());
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		try{
			context.startActivity(intent);
		}catch (Exception e){
		    e.printStackTrace();
		}
	}

	/**
	 * アプリの起動orストアに遷移
	 *
	 * インストール済みの場合はActivityを起動する。 未インストールの場合はGoogle Play Storeに遷移する。
	 *
     * @param context Android context
	 * @param packageName アプリケーションのパッケージ名
	 * @param param パラメータ
	 * 
	 * @return {@code true}:インストール済み, {@code false}:未インストール
	 */
	public static boolean goApp(Context context, String packageName, String param) {
		if (isInstalled(context, packageName)) {
			launchActivity(context, packageName, param);
			return true;
		} else {
			goMarket(context, packageName, param);
			return false;
		}
	}
}

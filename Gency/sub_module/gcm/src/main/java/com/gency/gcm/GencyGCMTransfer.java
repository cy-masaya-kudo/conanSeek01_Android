package com.gency.gcm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;

import com.gency.commons.log.GencyDLog;
//import com.gency.commons.tracker.GencyTrackerWrapper;

import java.util.List;

public class GencyGCMTransfer {

	/**
	 * ユーザーが通知をタップする時の処理 PARAM_ACTIONによって、新しいintentを作る トラック情報をサーバーに送信する
	 */
	static public void action(Activity activity){

		GencyDLog.e(GencyGCMConst.TAG, "GCMIntermediateActivity.onCreate()");
		String act = "";
		String url = "";
		Intent i = new Intent();
		Boolean isActionValid = true;
		Bundle extras = activity.getIntent().getExtras();

		// GA
//		GencyTrackerWrapper.init(activity);

        SharedPreferences prefs = activity.getSharedPreferences(GencyGCMConst.PREF_FILE_NAME, Context.MODE_PRIVATE);
        boolean isNormal = prefs.getBoolean(GencyGCMConst.PREF_KEY_IS_NORMAL, true);
        if (isNormal) {	// 本番用
    		if (extras.containsKey(GencyGCMConst.PARAM_ACTION)) {
    			act = extras.getString(GencyGCMConst.PARAM_ACTION);
    		}
    		if (extras.containsKey(GencyGCMConst.PARAM_URL)) {
    			url = extras.getString(GencyGCMConst.PARAM_URL);
    		}
    		if (act.toLowerCase().equals(GencyGCMConst.PARAM_ACTION_NONE)) {
    			isActionValid = false;
//    			GencyTrackerWrapper.sendEvent(GencyGCMConst.GA_NOTIFICATION_CATEGORY, GencyGCMConst.GA_NOTIFICATION_ACTION_TAP, GencyGCMConst.GA_NOTIFICATION_NONE_LABEL, 1L);
    		} else if ((act.toLowerCase().equals(GencyGCMConst.PARAM_ACTION_MARKET) && url.startsWith("market"))
    				|| (act.toLowerCase().equals(GencyGCMConst.PARAM_ACTION_BROWSER) && url.startsWith("http"))) {
    			i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//    			if (url.startsWith("http")) {
//    				GencyTrackerWrapper.sendEvent(GencyGCMConst.GA_NOTIFICATION_CATEGORY, GencyGCMConst.GA_NOTIFICATION_ACTION_TAP, GencyGCMConst.GA_NOTIFICATION_URL_LABEL, 1L);
//    			} else {
//    				GencyTrackerWrapper.sendEvent(GencyGCMConst.GA_NOTIFICATION_CATEGORY, GencyGCMConst.GA_NOTIFICATION_ACTION_TAP, GencyGCMConst.GA_NOTIFICATION_MARKET_LABEL, 1L);
//    			}
    		} else {
    			if (!act.toLowerCase().equals(GencyGCMConst.PARAM_ACTION_APP) || !url.startsWith("app://")) {
    				// actは"app"ではない場合とurl形が不正の場合、デフォルトアプリを起動する
    				url = "app://" + activity.getPackageName() + "/";
    			}
//    			GencyTrackerWrapper.sendEvent(GencyGCMConst.GA_NOTIFICATION_CATEGORY, GencyGCMConst.GA_NOTIFICATION_ACTION_TAP, GencyGCMConst.GA_NOTIFICATION_APP_LABEL, 1L);
    			GencyDLog.i(GencyGCMConst.TAG, "url = "+url);
    			Uri uri = Uri.parse(url);
    			Intent tempIntent = new Intent(Intent.ACTION_VIEW);
    			tempIntent.addCategory(Intent.CATEGORY_DEFAULT);
    			tempIntent.setData(uri);
    			List<ResolveInfo> list = activity.getPackageManager().queryIntentActivities(
    					tempIntent, PackageManager.MATCH_DEFAULT_ONLY);
    			if (list.size() > 0) {
    				i = tempIntent;
    				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    				Bundle bundle = new Bundle();
    				bundle.putBoolean(GencyGCMConst.GCM_BUNDLE_NAME, true);
    				i.putExtras(bundle);
    			} else {
    				isActionValid = false;
    			}
    		}
        } else {
        	// テスト用
	    	url = "app://" + activity.getPackageName() + "/";
			Uri uri = Uri.parse(url);
			Intent tempIntent = new Intent(Intent.ACTION_VIEW);
			tempIntent.addCategory(Intent.CATEGORY_DEFAULT);
			tempIntent.setData(uri);
			i = tempIntent;
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			Bundle bundle = new Bundle();
			bundle.putBoolean(GencyGCMConst.GCM_BUNDLE_NAME, true);
			i.putExtra("data", extras);
			i.putExtras(bundle);
        }
		if (extras != null && isActionValid == true) {
			GencyDLog.i(GencyGCMConst.TAG, "startActivity");
			activity.startActivity(i);
			GencyGCMUtilitiesE.PUSH_BAR_PUSHED = true;
		}
		activity.finish();
	}
}

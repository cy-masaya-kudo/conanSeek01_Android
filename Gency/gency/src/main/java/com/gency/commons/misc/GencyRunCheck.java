package com.gency.commons.misc;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * <h3>初回起動かをチェックする</h3>
 */
public class GencyRunCheck {
	private static String PREF_KEY_FIRSTRUN = "firstrun";

    /**
     * 初回起動かをチェックする
     * @param context Android context
     * @return {@code true}:初回起動, {@code false}:初回起動ではない
     */
	static public boolean isFirstRun(final Context context){
		SharedPreferences mPref = context.getSharedPreferences("lib_commons_runcheck_" + context.getPackageName(), Context.MODE_PRIVATE);
		if( mPref.getBoolean(PREF_KEY_FIRSTRUN , true) ){
			Editor e = mPref.edit();
			e.putBoolean(PREF_KEY_FIRSTRUN, false);
			e.commit();
			return true;
		}else{
			return false;
		}
	}
}

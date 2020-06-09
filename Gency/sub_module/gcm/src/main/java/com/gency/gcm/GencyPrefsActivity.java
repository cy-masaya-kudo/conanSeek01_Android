package com.gency.gcm;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.gency.commons.log.GencyDLog;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * <h3>GCM設定用アクティビティ</h3>
 *
 * PUSHを受け取るか否か、PUSH受信時に音を鳴らすか、バイブレーションを実行するかなどを設定するアクティビティ<br />
 * <br />
 * <b>使い方</b><br />
 * PUSH設定画面の表示<br />
 * GCMUtilities.launchPerfActivity(this);<br />
 * <br />
 * 必要に応じてPrefsActivityを継承してカスタマイズした設定画面を作成することが可能です<br />
 * <br />
 * <b>参考URL:</b><br />
 * http://faq.intra.cybird.co.jp/app_support/index.php?Push%C4%CC%C3%CE<br />
 * http://tool.push.sf.intra.cybird.co.jp/<br />
 * http://faq.intra.cybird.co.jp/app_support/index.php?CY%B6%A6%C4%CC%B4%F0%C8%D7%20-%B3%B5%CD%D7-
 */
@SuppressWarnings("deprecation")
public class GencyPrefsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

    /**
     * Activity起動時に呼ばれる
     * @param savedInstanceState
     */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PreferenceManager pm = getPreferenceManager();
		pm.setSharedPreferencesName(GencyGCMConst.PREF_FILE_NAME);
		int R_lib_gcm_prefs = GencyParameterLoader.getResourceIdForType("lib_gcm_prefs", "xml", this);
		addPreferencesFromResource(R_lib_gcm_prefs);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

    /**
     * 設定変更時に呼ばれる
     * @param sharedPreferences
     * @param key
     */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		GencyDLog.d(GencyGCMConst.TAG, "#onSharedPreferenceChanged() key = " + key);

		if (key.equals(GencyGCMConst.PREF_KEY_WILLSENDNOTIFICATION)) {
			boolean value = sharedPreferences.getBoolean(key, false);
			if (value == true) {
				// user select to receive notifications
				// Log.e(TAG, "user select to receive notifications");
				try {
					try {
						GencyGCMUtilitiesE.registerGCM(this);
					} catch (NoSuchPaddingException e) {
					} catch (InvalidAlgorithmParameterException e) {
					} catch (NoSuchAlgorithmException e) {
					} catch (IllegalBlockSizeException e) {
					} catch (BadPaddingException e) {
					} catch (InvalidKeyException e) {
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				// user select to refuse notifications
				// Log.e(TAG, "user select to refuse notifications");
				try {
					GencyGCMUtilitiesE.unregisterGCM(this);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (key.equals(GencyGCMConst.PREF_KEY_IS_TESTING)) {
			GencyGCMConst.switchIsTestingFLAG(sharedPreferences.getBoolean(key, false));
		} else if (key.equals(GencyGCMConst.PREF_KEY_DOES_INCLUDE_SANDBOX)) {
			GencyGCMConst.switchServerURL(sharedPreferences.getBoolean(key, false));
		} else if (key.equals(GencyGCMConst.PREF_KEY_CUSTOMIZED)){
			// send to cutomized handler
			GencyDLog.d(GencyGCMConst.TAG, "cusomized checkbox clicked");
			GencyGCMUtilitiesE.handleCustomizedSettingsChange(sharedPreferences.getBoolean(key, true));
		}
	}
	
	public interface CustomizedSettingsHandler {
		public void handleSettingsChange(boolean b);
	}

}

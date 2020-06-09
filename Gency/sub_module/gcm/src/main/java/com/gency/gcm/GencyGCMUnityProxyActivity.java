package com.gency.gcm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.gency.commons.log.GencyDLog;

import java.lang.ref.WeakReference;
import java.util.Set;

/**
 * UnityGCMライブラリ対応アクティビティサンプルソース
 */
public class GencyGCMUnityProxyActivity extends Activity {
	static String[] activityArray = new String[] {
		"com.unity3d.player.UnityPlayerActivity",
		"com.unity3d.player.UnityPlayerNativeActivity" };
	static String[] customActivityArray = new String[2];

	private static WeakReference<Activity> mThisActivity;

	private static Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// UnityPlayerActivityへ遷移
			if (mThisActivity.get() != null) {

				try {
					
					int i = Build.VERSION.SDK_INT >= 9 ? 1 : 0;
					
					Class<?> unityClass;
					
					// if custome activity is set,
					if(customActivityArray[0] != null && customActivityArray[1] != null){
						unityClass = Class.forName(customActivityArray[i]);
					}else{
						unityClass = Class.forName(activityArray[i]);
					}

					Intent unityIntent = new Intent(mThisActivity.get()
							.getApplication(), unityClass);
					unityIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

					mThisActivity.get().startActivity(unityIntent);

					return;
				} catch (ClassNotFoundException localClassNotFoundException) {
					return;
				} finally {
					mThisActivity.get().finish();
				}
			}
		}
	};

	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		
		Context context = getApplicationContext();

		SharedPreferences packagePrefs = context.getSharedPreferences(
				context.getPackageName(), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = packagePrefs.edit();

		// Push以外ならリターン

		Bundle bundle = getIntent().getExtras();
		
		if (bundle == null || bundle.getBoolean("push", false) == false) {
			// PUSH以外はデータをクリア
			editor.remove(GencyGCMUtilitiesE.CYLIB_GCM_PARAM_ISPUSH);
			editor.remove(GencyGCMUtilitiesE.CYLIB_GCM_PARAM_DATA);
			editor.commit();
			GencyGCMTransfer.action(this);
			return;
		}

		// load customActivityName if set,
		loadActivityClassName(this);
		
		// PUSHスキームで起動されたフラグをSharedPreferencesに保存
		editor.putInt(GencyGCMUtilitiesE.CYLIB_GCM_PARAM_ISPUSH, 1);

		String param = GencyGCMUtilitiesE.parseParametersString(getIntent());
		if (!param.equals("")) {
			editor.putString(GencyGCMUtilitiesE.CYLIB_GCM_PARAM_DATA, param);
		}
		editor.commit();

		mThisActivity = new WeakReference<Activity>(this);
		mHandler.sendEmptyMessage(0);
		
	}
	
	protected void loadActivityClassName(Context context){
		
		try {
			
			ApplicationInfo ai = context.getPackageManager()
					.getApplicationInfo(context.getPackageName(),
							PackageManager.GET_META_DATA);
			Bundle bundle = ai.metaData;
			if(bundle != null){
				Set<String> keys = bundle.keySet();
				for (String key : keys) {
					boolean classFound = false;
					if (key.equals("gcm.unity.activity")) {
						// load class name
						String className = bundle.getString(key);
						// load class
						try {
							Class.forName(className);
							classFound = true;
						} catch (ClassNotFoundException e) {
							GencyDLog.e("GencyGCMUnityProxyActivity", e.toString());
							classFound = false;
						}
						// set activity class name
						if(classFound){
							customActivityArray[0] = className;
						}else{
							customActivityArray[0] = null;
						}
					}else if(key.equals("gcm.unity.nativeactivity")) {
						// load class name
						String className = bundle.getString(key);
						// load class
						try {
							Class.forName(className);
							classFound = true;
						} catch (ClassNotFoundException e) {
							GencyDLog.e("GencyGCMUnityProxyActivity", e.toString());
							classFound = false;
						}
						// set activity class name
						if(classFound){
							customActivityArray[1] = className;
						}else{
							customActivityArray[1] = null;
						}
					}
				}
			}
		} catch (NameNotFoundException e) {
			GencyDLog.e("GencyGCMUnityProxyActivity", e.toString());
		} catch (NullPointerException e) {
			GencyDLog.e("GencyGCMUnityProxyActivity", e.toString());
		} catch (IllegalArgumentException e) {
			GencyDLog.e("GencyGCMUnityProxyActivity", e.toString());
		} catch (Exception e){
			GencyDLog.e("GencyGCMUnityProxyActivity", e.toString());
		}
		// if NativeActivity is not set,
		if(customActivityArray[1] == null && customActivityArray[0] != null){
			customActivityArray[1] = customActivityArray[0];
		}
	}
}

package com.gency.commons.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * <h3>ネットワーク関連のユーティリティクラス</h3>
 */
public class GencyNetworkUtil {

	/**
	 * ネットワークが利用可能かどうかを返却する。
	 *
	 * ※AndroidManifest.xmlに以下のパーミッション記述が必要
	 * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
	 *
	 * @param context Android context
	 * @return {@code true}:ネットワークに繋がっている, {@code false}:ネットワークに繋がっていない
	 */
	public static boolean isNetworkConnected(Context context) {
            ConnectivityManager connMng = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connMng.getActiveNetworkInfo();
            if( info != null ){
                return connMng.getActiveNetworkInfo().isConnected();
            }
            return false;
	}

}

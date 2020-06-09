package com.gency.commons.log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import com.gency.commons.file.json.util.Unicode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Base64;

/**
 * <h3>ログをDBへ保存できる</h3>
 *
 * <b>使い方</b><br />
 * ApplicationLogManager.addLog(activity, "purchase", url);<br />
 * @see GencyApplicationLog
 * @see GencyApplicationLogDB
 *
 */
public class GencyApplicationLogManager {
    /**
     * データ追加
     * @param context Android context
     * @param tag ログメッセージの送信元を識別するために使用する
     * @param message メッセージ
     */
	public synchronized static void addLog(Context context, String tag, String message) {
		GencyApplicationLog appLog = new GencyApplicationLog(tag, message);
		GencyApplicationLogDB db = new GencyApplicationLogDB(context);
		db.insert(appLog);
		db.close();
	}

    /**
     * 溜まっている課金済みデータを全て取得する
     * @param context Android context
     * @return 課金済みデータ
     */
	public synchronized static List<GencyApplicationLog> getAllLogs(Context context){
		GencyApplicationLogDB db = new GencyApplicationLogDB(context);
		List<GencyApplicationLog> resultArray = db.getAllLogs();
		db.close();
		return resultArray;
	}

    /**
     * 溜まっている課金済みデータをJson型で全て取得する
     * @param context Android context
     * @return Json型課金済みデータ
     */
	public synchronized static String getAllLogsJsonString(Context context){
		List<GencyApplicationLog> appLogs = getAllLogs(context);
		
		if(appLogs == null){
			return null;
		}
		int size = appLogs.size();
		GencyApplicationLog tmpAppLog = null;
		String tag = null, message = null;
		long timestamp = 0;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject;
		try {
			for(int i=0; i<size; i++){
				jsonObject = new JSONObject();
				timestamp = 0;
				tag = message = null;
				tmpAppLog = appLogs.get(i);
				if(tmpAppLog != null){
					timestamp = tmpAppLog.getTimestamp();
					tag = tmpAppLog.getTag();
					message = tmpAppLog.getMessage();
					if(timestamp != 0 && tag != null && message != null){
						jsonObject.put("timestamp", timestamp);
						jsonObject.put("tag", tag);
						jsonObject.put("message", Unicode.escape(message));
						jsonArray.put(jsonObject);
					}
				}
			}
			String jsonString = jsonArray.toString();
			return jsonString;
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

    /**
     * 溜まっている課金済みデータをJson型で全て取得する（base64エンコードした文字列）
     * @param context Android context
     * @return base64エンコードしたJson型課金済みデータ
     */
	public synchronized static String getAllLogsJsonStringBase64Encoded(Context context){
		String jsonArrayString = getAllLogsJsonString(context);
		if(jsonArrayString == null) return null;
		byte[] encode = Base64.encode(jsonArrayString.getBytes(), Base64.DEFAULT);
		if(encode == null) return null;
		try {
			return new String(encode, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

    /**
     * 溜まっている課金済みデータをJson型で全て取得する（urlエンコードした文字列）
     * @param context Android context
     * @return urlエンコードしたJson型課金済みデータ
     */
	public synchronized static String getAllLogsJsonStringURLEncoded(Context context){
		String jsonArrayString = getAllLogsJsonString(context);
		if(jsonArrayString == null) return null;
		try {
			return URLEncoder.encode(jsonArrayString , "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
}

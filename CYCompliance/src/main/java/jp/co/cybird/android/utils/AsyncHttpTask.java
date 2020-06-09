/**
 * Copyright (c) 2015年 CYBIRD Co., Ltd.
 */

package jp.co.cybird.android.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class AsyncHttpTask extends AsyncTask<String, Void, String> {

    private final static String EOL = "\r\n";

    private Context mContext;
    private String userAgent;

    public interface OnResponseReceive{
        void onResponseReceive(String ret);
    }

    public void setOnResponseReceive(OnResponseReceive mOnResponseReceive) {
        this.mOnResponseReceive = mOnResponseReceive;
    }

    OnResponseReceive mOnResponseReceive = null;

    public AsyncHttpTask(Context context) {
        this.mContext = context;
        userAgent = new WebView(mContext).getSettings().getUserAgentString();
    }

    @Override
    protected String doInBackground(String... str) {
        String result = null;
        String argurl = str[0];
        HttpURLConnection connection = null;
        BufferedReader br = null;
        try {
            URL url = new URL(argurl);

            connection = (HttpURLConnection) url.openConnection();

            connection.setUseCaches(false);

            connection.setRequestMethod("GET");

            connection.setRequestProperty("User-Agent", userAgent);

            // レスポンスを受信する
            connection.connect();

            // 接続が確立したとき
            StringBuilder resultBuilder = new StringBuilder();
            String line = "";

            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            // レスポンスの読み込み
            while ((line = br.readLine()) != null) {
                resultBuilder.append(String.format("%s%s", line, EOL));
            }
            result = resultBuilder.toString();

        } catch (MalformedURLException e) {
        } catch (IOException e) {
        } finally {
            // 開いたら閉じる
            try {
                if (br != null) br.close();
                if (connection != null) connection.disconnect();
            } catch (IOException e) {
            }
            return result;
        }
    }


    // このメソッドは非同期処理の終わった後に呼び出されます
    @Override
    protected void onPostExecute(String result) {
        if(mOnResponseReceive != null){
            mOnResponseReceive.onResponseReceive(result);
        }
    }
}

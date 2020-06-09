package com.gency.commons.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

public class GencyThreadHttpClient {
	enum Method{
		GET, POST
	};
	private String mUserAgent = null;
	private HashMap<String, String> clientHeaderMap;

	public GencyThreadHttpClient() {
		clientHeaderMap = new HashMap<String, String>();
	}


	public void get(String urlStr, GencyRequestParams params) {
		sendRequest(urlStr + "?" + params.getParamString(), null, Method.GET);
	}
	public void post(String urlStr, GencyRequestParams params) {
		sendRequest(urlStr, params, Method.POST);
	}
	public void sendRequest(final String urlStr, final GencyRequestParams params, final Method method) {
		Thread thread = new Thread(){
			@Override
			public void run(){
				String returnString = "";
				try {
					URL url = new URL(urlStr);
					URLConnection urlConnection = url.openConnection();
					
					// UAをセット
					if(getUserAgent() != null){
						urlConnection.setRequestProperty("User-Agent", getUserAgent());
					}
					// UA以外に必要なヘッダがあればセット
					if( clientHeaderMap.size() > 0 ){
			            for (String header : clientHeaderMap.keySet()) {
			            	urlConnection.setRequestProperty(header, clientHeaderMap.get(header));
			            }
					}
					if(method == Method.POST){
						// POST可能にする
						urlConnection.setDoOutput(true);
						
						// POST用のOutputStreamを取得
			            OutputStream outPutStream = urlConnection.getOutputStream(); 
			            
			            // POSTするデータを整形
			            String paramString = params.getParamString();
			            PrintStream ps = new PrintStream(outPutStream);
			            // データをPOSTする
			            ps.print(paramString);
			            ps.close();
		            }
		            // POSTした結果を取得
		            InputStream inputStream = urlConnection.getInputStream();
		            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(inputStream));
		            String str = null;
		            while((str = bufferReader.readLine()) != null){
		            	returnString = returnString + str;
		            }
		            bufferReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					finalize();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();
	}

	public void addHeader(String header, String value) {
		 clientHeaderMap.put(header, value);
	}

	public String getUserAgent() {
		return mUserAgent;
	}

	public void setUserAgent(String userAgent) {
		mUserAgent = userAgent;
	}
}

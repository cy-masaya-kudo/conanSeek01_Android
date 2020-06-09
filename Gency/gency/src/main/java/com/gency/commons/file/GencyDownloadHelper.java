package com.gency.commons.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.gency.commons.log.GencyDLog;

/**
 * <h3>サーバからファイルを非同期取得する際のヘルパー (故)</h3>
 * @deprecated
 */
public class GencyDownloadHelper {
	static final String TAG = "DownloadHelper";

	private static final String MSG_NOT_SET_DOWNLOAD_URL = "Not set download URL.";
	private static final String REGULAR_EXPRESSION = File.separator;
	public static final String REQUEST_METHOD_GET = "GET";
	public static final String REQUEST_METHOD_POST = "POST";
	public static final String REQUEST_HEADER_USERAGENT = "User-Agent";

	// public static final String REQUEST_HEADER_USERAGENT = "http.useragent";

	public enum SaveDir {
		SAVE_DIR_APP, SAVE_DIR_EXTERNAL
	}

	private URL mURL;
	private SaveDir mSaveDir;
	private String mFilePath;
	private String mFileName;
	private URLConnection mConn;
	private Context mContext;
	private String mRequestMethod;
	private Map<String, String> mRequestProperty;

	private String mUserAgent;

	public void setUserAgent(String userAgent) {
		mUserAgent = userAgent;
	}

	/**
	 * コンストラクタ
	 * 
	 * @param context
	 */
	public GencyDownloadHelper(Context context) {
		mContext = context;
		mSaveDir = SaveDir.SAVE_DIR_APP;
		mRequestMethod = REQUEST_METHOD_GET;
	}

	/**
	 * URLをセットする。
	 * 
	 * @param url
	 *            URL文字列
	 * @throws MalformedURLException
	 *             URLの形式が不正の場合
	 */
	public void setUrl(String url) throws MalformedURLException {
		mURL = new URL(url);
	}

	/**
	 * セットされているURLを返す。
	 * 
	 * @return URL
	 */
	public URL getUrl() {
		return mURL;
	}

	/**
	 * ダウンロードしたファイルの保存先パスをセットする。
	 * 
	 * @param filePath
	 */
	public void setFilePath(String filePath) {
		mFilePath = filePath;
		String[] splitPath = filePath.split(REGULAR_EXPRESSION);
		setFileName(splitPath[splitPath.length - 1]);
	}

	/**
	 * ダウンロードしたファイルのファイル名をセットする。
	 * 
	 * @param fileName
	 */
	public void setFileName(String fileName) {
		mFileName = fileName;
	}

	/**
	 * 保存する領域をセットする。 SaveDir.SAVE_DIR_APP ... /data/app/package/files
	 * SaveDir.SAVE_DIR_SDCARD ... /mnt/sdcard
	 * 
	 * @param saveDir
	 */
	public void setSaveDir(SaveDir saveDir) {
		mSaveDir = saveDir;
	}

	/**
	 * メソッドをセットする。 ・DownloadHelper.REQUEST_METHOD_GET
	 * ・DownloadHelper.REQUEST_METHOD_POST
	 * 
	 * @param method
	 */
	public void setRequestMethod(String method) {
		if (method != null) {
			if (method.equals(REQUEST_METHOD_GET)
					|| method.equals(REQUEST_METHOD_POST)) {
				mRequestMethod = method;
			}
		}
	}

	/**
	 * リクエストヘッダにパラメータを追加する。
	 * 
	 * @param field
	 * @param value
	 */
	public void addRequestProperty(String field, String value) {
		if (mRequestProperty == null) {
			mRequestProperty = new HashMap<String, String>();
		}
		mRequestProperty.put(field, value);
	}

	/**
	 * セットされたURLからファイルをダウンロードし、 指定のディレクトリ、ファイル名で保存する。
	 * 
	 * @throws IOException
	 *             ファイルの読み込み不可、書き込み不可
	 * @throws Exception
	 */
	public void download() throws IOException, Exception {
		if (mURL == null) {
			throw new GencyDownloadHelperException(MSG_NOT_SET_DOWNLOAD_URL);
		}

		if (mConn == null) {
			try {
				connect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// new DownloadTask().execute();
		new Download().execute();
	}

	private void connect() throws IOException {
		mConn = mURL.openConnection();
		mConn.setAllowUserInteraction(false);
	}

	public void cancel() {
		HttpURLConnection httpURLConn = (HttpURLConnection) mConn;
		httpURLConn.disconnect();
	}

	class Download {
		private void execute() {
			GencyDLog.d("DHP", "Download#execute()");
			InputStream is = null;
			HttpURLConnection httpURLConn = (HttpURLConnection) mConn;
			try {
				httpURLConn.setInstanceFollowRedirects(true);
				httpURLConn.setRequestMethod(mRequestMethod);
				httpURLConn.setRequestProperty(REQUEST_HEADER_USERAGENT,
						mUserAgent);

				// prop 確認
				// Map<String, List<String>> props =
				// httpURLConn.getRequestProperties();
				// for (Map.Entry<String, List<String>> entry: props.entrySet())
				// {
				// DLog.d("PROPS",
				// "key:"+entry.getKey()+"="+entry.getValue().get(0));
				// }

				httpURLConn.connect();

				int resCode = httpURLConn.getResponseCode();
				GencyDLog.d("CAC",
						"HTTP response code:".concat(String.valueOf(resCode)));
				if (resCode != HttpURLConnection.HTTP_OK) {
					throw new Exception("HttpURLConnection response code = "+String.valueOf(resCode));
				}

				is = httpURLConn.getInputStream();

				if (is == null)
					GencyDLog.i("CAC", "Http response InputStream is null");

				// アプリ占有ディレクトリに保存する場合
				if (SaveDir.SAVE_DIR_APP.equals(mSaveDir)) {
					GencyFileUtil.saveFileInAppDirectory(mContext, is, mFileName);
				} else if (SaveDir.SAVE_DIR_EXTERNAL.equals(mSaveDir)) {
					saveFileInExternalStorage(is);
				}

			} catch (ProtocolException pex) {
				GencyDLog.e(TAG, pex.toString());
			} catch (UnsupportedEncodingException insex) {
				GencyDLog.e(TAG, insex.toString());
			} catch (IOException ioex) {
				GencyDLog.e(TAG, ioex.toString());
			} catch (Exception httpex) {
				GencyDLog.e(TAG, httpex.toString());
			} finally {
				httpURLConn.disconnect();
			}

		}

		private void saveFileInExternalStorage(InputStream is)
				throws FileNotFoundException, IOException {
			// キャッシュディレクトリを作成
			GencyFileUtil.makeCommonIconCacheDir(mFilePath);

			FileOutputStream os = new FileOutputStream(mFilePath);
			byte[] buffer = new byte[1024];
			int bufferLength = 0;

			BufferedInputStream bis = new BufferedInputStream(is);
			while ((bufferLength = bis.read(buffer)) > 0) {
				os.write(buffer, 0, bufferLength);
			}
			bis.close();
			is.close();
			os.close();
		}
	};

}

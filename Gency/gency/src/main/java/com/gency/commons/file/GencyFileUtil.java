package com.gency.commons.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.os.Environment;

import com.gency.commons.log.GencyDLog;

/**
 * <h3>ファイル関連のユーティリティクラス</h3>
 */
public class GencyFileUtil {

    /** SDカード配下のフォルダ構成 */
	public static final String EXTERNAL_STORAGE_DATA_DIR = "/Android/data";


	/**
	 * 指定したInputStreamオブジェクトを指定ファイル名でアプリケーションディレクトリ内に保存
	 *
	 * @param context Android context
	 * @param is 保存対象のInputStream
	 * @param fileName 保存時のファイル名
	 * @throws FileNotFoundException 指定した出力ファイル名でファイルストリームを取得できなかった場合
	 * @throws IOException ファイルの読み書きでエラーが発生した場合
	 */
	public static void saveFileInAppDirectory(Context context, InputStream is, String fileName) throws FileNotFoundException, IOException {
		if (is == null) {
			GencyDLog.d("CAC", "InputStream is NULL.");
			return;
		}

		FileOutputStream os = context.getApplicationContext().openFileOutput(fileName, Context.MODE_PRIVATE);
		DataInputStream dis = new DataInputStream(is);
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(os));
		byte[] buff = new byte[1024];
		int bLen = 0;
		try {
			while ((bLen = dis.read(buff)) > 0) {
				dos.write(buff, 0, bLen);
			}
			dos.flush();
		} finally {
			if (dis != null) {
				dis.close();
			}
			if (dos != null) {
				dos.close();
			}
		}
	}

	/**
	 * 指定したInputStreamオブジェクトを指定ファイル名でアプリケーションディレクトリ内に保存
	 *
	 * @param context Android context
     * @param str 書き込みたい文字列
	 * @param fileName 保存時のファイル名
     * @param utf 未使用
	 * @throws FileNotFoundException 指定した出力ファイル名でファイルストリームを取得できなかった場合
	 * @throws IOException ファイルの読み書きでエラーが発生した場合
	 */
	public static void saveFileInAppDirectory(Context context, String str, String fileName, boolean utf) throws FileNotFoundException, IOException {
		if (str == null) {
			GencyDLog.d("CAC", "InputStream is NULL.");
			return;
		}
		FileOutputStream os = context.getApplicationContext().openFileOutput(fileName, Context.MODE_PRIVATE);
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(os));

		dos.write(str.getBytes());

		dos.flush();
		dos.close();
	}

	/**
	 * 指定したInputStreamオブジェクトを指定ファイル名で外部ストレージの
	 * \sdcard\Android\data に保存
	 *
	 * @param context Android context
	 * @param is 保存対象のInputStream
	 * @param filePath 保存時のファイル名
	 * @throws IOException
	 */
	public static void saveFileInExternalStorage(Context context, InputStream is, String filePath) throws IOException {
		makeCommonIconCacheDir(filePath);

		FileOutputStream os = new FileOutputStream(filePath);
		byte[] buffer = new byte[1024];
        int bufferLength = 0;
		
	       BufferedInputStream bis = new BufferedInputStream(is);  
	       
//	        byte []fbytes = new byte[1024];  
	  
//	        while ((bis.read(fbytes)) >= 0) {
//	            System.out.print(new String(fbytes));  
//	        }  
	        while ( (bufferLength = bis.read(buffer)) > 0 ) {
				GencyDLog.d("FU", "before write");
	        	os.write(buffer, 0, bufferLength);
				GencyDLog.d("FU", "bufferLength: " + bufferLength);
	        }
	        bis.close();  
	        

//
//        DLog.d("FU", "before while");
//        while ( is.available() > 0 && (bufferLength = is.read(buffer)) > 0 ) {
//        	DLog.d("FU", "before write");
//        	os.write(buffer, 0, bufferLength);
//        	DLog.d("FU", "bufferLength: " + bufferLength);
//        }
//        DLog.d("FU", "after while");
        os.close();
        is.close();
		GencyDLog.d("FU", "After save");
        
//		
//		FileOutputStream os = new FileOutputStream(file);
//		DataInputStream dis = new DataInputStream(is);
//		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(os));
//		byte[] buff = new byte[1024];
//		int bLen = 0;
//		try {
//			while ((bLen = dis.read(buff)) > 0) {
//				dos.write(buff, 0, bLen);
//			}
//			dos.flush();
//		} finally {
//			if (dis != null) {
//				dis.close();
//			}
//			if (dos != null) {
//				dos.close();
//			}
//		}
	}

	public static void makeCommonIconCacheDir(String filePath) {
		String externalPath = getExternalStorageDataDirPath();
		if (!filePath.startsWith(externalPath)) {
			if (!filePath.startsWith(File.separator)) {
				filePath = File.separator + filePath;
			}
			filePath = externalPath.concat(filePath);
		}
		
		File file = new File(filePath);
		File dir = new File(file.getParent());
		if (dir.exists()) {
			return;
		}else{
			dir.mkdirs();
		}
//		

//		DLog.d("CAC:filePath2", "filePath: " + filePath);
//		
//		
//		DLog.d("CAC:filePath2", "getParent: " + file.getParent());
//		File dir = new File(file.getParent());
//		if (!dir.exists()) {
//			dir.mkdirs();
//		}
//		DLog.d("FU", "After mkdir()");
	}

	/**
	 * SDカードインスタンスを取得
	 * @return SDカード配下の参照できる領域
	 */
	public static File getExternalStorage() {
		return Environment.getExternalStorageDirectory();
	}

	/**
	 * SDカードのディレクトリパスを取得
	 * @return SDカードのディレクトリパス
	 */
	public static String getExternalStoragePath() {
		return getExternalStorage().getPath();
	}

	/**
	 * SDカード下のデータディレクトリパスを取得
	 * @return SDカード下のデータディレクトリパス
	 */
	public static String getExternalStorageDataDirPath() {
		return getExternalStoragePath().concat(EXTERNAL_STORAGE_DATA_DIR);
	}

	/**
	 * SDカード下のパッケージ用データディレクトリパスを取得
	 * @param context Android context
	 * @param mkdir パッケージ用データディレクトリが存在しない場合、{@code true}:作成する, {@code false}:作成しない
	 * @return ディレクトリパス
	 */
	public static String getExternalStoragePackageDataDir(Context context, boolean mkdir) {
		String dirPath = getExternalStorageDataDirPath().concat(File.separator + context.getPackageName());
		if (mkdir) {
			File dir = new File(dirPath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}
		return dirPath;
	}

	/**
	 * キャッシュディレクトリインスタンスを取得
	 * @return キャッシュディレクトリインスタンス
	 */
	public static File getDownloadCacheDir() {
		return Environment.getDownloadCacheDirectory();
	}

	/**
	 * キャッシュディレクトリパスを取得
	 * @return キャッシュディレクトリパス
	 */
	public static String getDownloadCacheDirPath() {
		return getDownloadCacheDir().getPath();
	}

}

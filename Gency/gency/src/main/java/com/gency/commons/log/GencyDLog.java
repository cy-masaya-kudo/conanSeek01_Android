package com.gency.commons.log;

import android.util.Log;

/**
 * <h3>ログ出力</h3>
 *
 * <b>使い方</b><br />
 * DLog.d("MainActivity", "method is called.");
 */
public class GencyDLog {

	private static boolean mDebuggable = false;

    /**
     * ログを出力するかを設定
     * @param debuggable {@code true}:ログを出力する, {@code false}:ログを出力しない
     */
	public static void setDebuggable(boolean debuggable) {
		mDebuggable = debuggable;
	}

    /**
     * ログレベルDEBUGのログ出力をする
     * @param tag ログメッセージの送信元を識別するために使用する
     * @param msg 出力したいメッセージ
     */
	public static void d(String tag, String msg) {
		if (mDebuggable) {
			Log.d(tag, msg);
		}
	}

    /**
     * ログレベルVERBOSEのログ出力をする
     * @param tag ログメッセージの送信元を識別するために使用する
     * @param msg 出力したいメッセージ
     */
	public static void v(String tag, String msg) {
		if (mDebuggable) {
			Log.v(tag, msg);
		}
	}

    /**
     * ログレベルWARNのログ出力をする
     * @param tag ログメッセージの送信元を識別するために使用する
     * @param msg 出力したいメッセージ
     */
    public static void w(String tag, String msg) {
		if (mDebuggable) {
			Log.w(tag, msg);
		}
	}

    /**
     * ログレベルERRORのログ出力をする
     * @param tag ログメッセージの送信元を識別するために使用する
     * @param msg 出力したいメッセージ
     */
	public static void e(String tag, String msg) {
		if (mDebuggable) {
			Log.e(tag, msg);
		}
	}

    /**
     * ログレベルINFOのログ出力をする
     * @param tag ログメッセージの送信元を識別するために使用する
     * @param msg 出力したいメッセージ
     */
    public static void i(String tag, String msg) {
		if (mDebuggable) {
			Log.i(tag, msg);
		}
	}

}

package com.gency.commons.log;

/**
 * <h3>ログにタイムスタンプを付与するモデルクラス</h3>
 *
 * <b>使い方</b><br />
 * ApplicationLog applicationLog = new ApplicationLog(1022416607108, "MainActivity", "onCreate Start.");
 */
public class GencyApplicationLog {
	
	long mTimestamp;
    String mTag;
    String mMessage;

    /**
     * tagとmessageの他、ApplicationLogインスタンスの生成時のタイムスタンプを保持する
     * @param tag メッセージの送信元を識別するために使用する
     * @param message 保存したいメッセージ
     */
    public GencyApplicationLog(String tag, String message){
        mTimestamp = System.currentTimeMillis();
        mTag = tag;
        mMessage = message;
    }

    /**
     * タイムスタンプを指定できる
     * @param timestamp ログと一緒に保存したいタイムスタンプ
     * @param tag メッセージの送信元を識別するために使用する
     * @param message 保存したいメッセージ
     */
    public GencyApplicationLog(long timestamp, String tag, String message){
        mTimestamp = timestamp;
        mTag = tag;
        mMessage = message;
    }

    /**
     * @return タイムスタンプ
     */
    public long getTimestamp() { return mTimestamp; }

    /**
     * @return メッセージの送信元を識別する文字列
     */
    public String getTag() { return mTag; }

    /**
     * @return メッセージ
     */
    public String getMessage() { return mMessage; }
}

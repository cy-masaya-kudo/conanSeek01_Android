package com.gency.gcm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.PowerManager;
//import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.fragment.app.FragmentActivity;

/**
 * リッチプッシュダイアログのアクティビティ
 */
@SuppressLint("SetJavaScriptEnabled")
public class GencyCustomDialogActivity extends FragmentActivity {

	public PowerManager.WakeLock wakelock;
	public Context activityContext = this.getBaseContext();
	public boolean mIsFailure = false;
	public String startCheckUrl = null;
	public String closeCheckUrl = null;

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// BackGroundからのWakelock処理。スリープ時に画面を起こす処理。
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		wakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP
				, "Your App Tag");
		wakelock.acquire(5000);

		// このアクティビティを常に一番前に表示する処理。
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

		//ホームボタン押された時の準備
		//		HomeButtonReceive m_HomeButtonReceive = new HomeButtonReceive();
		//		IntentFilter iFilter = new IntentFilter();
		//		iFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		//		this.registerReceiver(m_HomeButtonReceive, iFilter);

		// 各種パラメータ取得。
		Bundle notificationParam = getIntent().getExtras().getBundle("notificationParam");
		String dialogUrl = notificationParam.getString("dialogUrl");
		startCheckUrl    = notificationParam.getString("startCheckUrl");
		closeCheckUrl    = notificationParam.getString("closeCheckUrl");

		// WEBダイアログを設定。
		setContentView(R.layout.lib_gcm_custom_push_dialog);
		JsObject jsObj = new JsObject(this);

		WebView wv = (WebView)findViewById(R.id.webView1);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE); // キャッシュを消去
		wv.getSettings().setUseWideViewPort(true);                // WebView全表示設定。
		wv.addJavascriptInterface(jsObj, "customPush");           // JavaScriptInterface設定。
		wv.setWebViewClient(new ViewClient());                    // WebViewの挙動管理用WebViewClientを設定。
		wv.setVisibility(View.INVISIBLE);                         // ページ取得ができるかわからないので、初期は非表示で用意。
		wv.loadUrl(dialogUrl);                                    // ページ取得開始。
	}

	/** リッチプッシュのurlのロードに成功したかどうかを判定
	 * @author n01019
	 *
	 */
	public final class ViewClient extends WebViewClient {
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			mIsFailure = false;
			super.onPageStarted(view, url, favicon);
		}

		// 取得終了時。
		@Override
		public void onPageFinished(WebView view , String url){
			//ロード完了時
			if(mIsFailure){
				GencyGCMUtilitiesE.addNotification(getApplicationContext(), getIntent().getExtras().getBundle("notificationParam"));
				// 集計用フラグ。
				GencyGCMUtilitiesE.CUSTOM_PUSH_VIEWED = true;
			} else {
				view.setVisibility(View.VISIBLE);
				// 集計用フラグ。
				GencyGCMUtilitiesE.CUSTOM_PUSH_VIEWED = false;
			}
		}
		// 取得失敗時。
		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			mIsFailure = true;
		}
	}

	/**JavaScriptInterfaceのメソッドクラス
	 * @author n01019
	 *
	 */
	public class JsObject {
		private Activity activity;
		public JsObject(Activity activity) {
			this.activity = activity;
		}

		//
		/** クローズボタンが押されたときにリッチプッシュアクティビティを終了するメソッド
		 *
		 */
		@JavascriptInterface
		public void closeDialog() {
			// 集計用フラグ。
			GencyGCMUtilitiesE.CLOSE_BUTTON_PUSHED = true;
			// GA パラメータ設定用。
			//			TrackerWrapper.init(getApplicationContext());
			//			TrackerWrapper.sendEvent(Const.GA_NOTIFICATION_CATEGORY, Const.GA_NOTIFICATION_ACTION_DISPLAY, Const.GA_NOTIFICATION_DISPLAY_LABEL, 1L);

			// 本アクティビティを終了。
			this.activity.finish();
		}

		/**スタートボタンが押されたときにメインアプリを起動するメソッド
		 *
		 */
		@JavascriptInterface
		public void startActivity() {
			// コンテンツのアクティビティを起動。
			GencyGCMTransfer.action(activity);
			// 集計用フラグ。
			GencyGCMUtilitiesE.START_BUTTON_PUSHED = true;

			// GA パラメータ設定用。
			//			TrackerWrapper.init(getApplicationContext());
			//			TrackerWrapper.sendEvent(Const.GA_NOTIFICATION_CATEGORY, Const.GA_NOTIFICATION_ACTION_DISPLAY, Const.GA_NOTIFICATION_DISPLAY_LABEL, 1L);

			// 本アクティビティを終了。
			this.activity.finish();
		}
	}

	// ホームボタン押下を取得してアクティビティを終了させる。現状では非実装。
	//	public class HomeButtonReceive extends BroadcastReceiver{
	//		@Override
	//		public void onReceive(Context ctx, Intent arg1){
	//			// GA パラメータ設定用。
	//			TrackerWrapper.init(getApplicationContext());
	//			TrackerWrapper.sendEvent(Const.GA_NOTIFICATION_CATEGORY, Const.GA_NOTIFICATION_ACTION_DISPLAY, Const.GA_NOTIFICATION_DISPLAY_LABEL, 1L);
	//			((Activity) ctx).finish();
	//		}
	//	}
}

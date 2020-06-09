package com.gency.gcm;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

/**
 * <h3>PUSH用利用規約ダイアログ</h3>
 *
 * <b>使い方</b><br />
 * AgreementDialog agreementDialog = new AgreementDialog(CyUtilitySample.this, 2013010101, "file:///android_asset/eula.html");<br />
 * agreementDialog.showAgreement();<br />
 */
public class GencyAgreementDialog implements OnCancelListener, OnDismissListener {

	private Dialog mDialog;
	private Context mContext;
	private int mEulaVer;
	private String mEulaUrl;
	private int mDisplayHeight;
	private SharedPreferences mPref;
	private final String PREF_KEY_AGREEMENT = "lib_gcm_agreement_version";
	static private boolean isShown = false;
	private WebView mWebview;
	
	private GencyDismissHooker mDismissHooker;

	/**
	 * @param context Android context
	 * @param eulaVersion 利用規約最終更新日 YYYYMMDDXX(ex.2013010101)
	 * @param eulaUrl 表示する規約のURL
	 */
	@SuppressLint({ "WorldReadableFiles", "WorldWriteableFiles" })
	@SuppressWarnings("deprecation")
	public GencyAgreementDialog(Context context, int eulaVersion, String eulaUrl) {
		mContext = context;
		mEulaVer = eulaVersion;
		mEulaUrl = eulaUrl;

		mPref = context.getSharedPreferences(GencyGCMConst.PREF_FILE_NAME, Context.MODE_WORLD_READABLE|Context.MODE_WORLD_WRITEABLE);
		
		// Get Screen Size
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		mDisplayHeight = display.getHeight();

		createDialog(context);
	}

	/**
	 * @param context
	 */
	private void createDialog(Context context) {
		mDialog = new Dialog(context) {


			private ProgressBar mProgress;

			@Override
			protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				
				
				setTitle(GencyParameterLoader.getString("LIB_GCM_DIALOG_TITLE", mContext));

				// Eula用レイアウトをセット
				setContentView(GencyParameterLoader.getResourceIdForType("lib_gcm_agreement_dialog", "layout", mContext));

				FrameLayout frame = (FrameLayout) findViewById(GencyParameterLoader.getResourceIdForType("lib_gcm_agreement_webview_frame", "id", mContext));
				LinearLayout.LayoutParams layoutParams = 
			              new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, mDisplayHeight / 2);
				frame.setLayoutParams(layoutParams);
				
				mProgress = (ProgressBar) findViewById(GencyParameterLoader.getResourceIdForType("lib_gcm_agreement_progress", "id", mContext));
				mWebview = (WebView) findViewById(GencyParameterLoader.getResourceIdForType("lib_gcm_agreement_webview_agreement", "id", mContext));

				WebSettings webviewSettings = mWebview.getSettings();
				webviewSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
				
				mWebview.setWebViewClient(new LocalClient(mProgress));
				mWebview.loadUrl(mEulaUrl);
				
				// 閉じるボタン
				Button decline_button = (Button) findViewById(GencyParameterLoader.getResourceIdForType("lib_gcm_agreement_decline_button", "id", mContext));
				decline_button.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						saveDecline();
						dismiss();
					}
				});

				Button agree_button = (Button) findViewById(GencyParameterLoader.getResourceIdForType("lib_gcm_agreement_agree_button", "id", mContext));
				agree_button.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						saveAgreement();
						dismiss();
					}
				});
			}
		};
		mDialog.setOnCancelListener(this);
		mDialog.setOnDismissListener(this);
	}

    /**
     * DialogのDissmiss時にカスタム処理を実行する場合に使用
     * @param dh
     */
	public void setDismissHooker(GencyDismissHooker dh) {
		mDismissHooker = dh;
	}

	/**
	 *
	 */
	public void onPause() {
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
			isShown = true;
		}
	}

	/**
	 * 
	 */
	public void onResume() {
		if (isShown) {
			mDialog.show();
		}
	}

	/**
	 * AgreementDialogを表示
	 */
	public void showAgreement() {
		if (mDialog != null 
				&& ! isAgreed()
				){
			mDialog.show();
		}
	}

	private void saveDecline() {
		// 同意したEULAのバージョンを保存
		Editor e = mPref.edit();
		e.putInt(PREF_KEY_AGREEMENT, mEulaVer);
		e.putBoolean(GencyGCMConst.PREF_KEY_WILLSENDNOTIFICATION, false);
		e.commit();
	}
	/**
	 * 
	 */
	private void saveAgreement() {
		// 同意したEULAのバージョンを保存
		Editor e = mPref.edit();
		e.putInt(PREF_KEY_AGREEMENT, mEulaVer);
		e.putBoolean(GencyGCMConst.PREF_KEY_WILLSENDNOTIFICATION, true);
		e.putBoolean(GencyGCMConst.PREF_KEY_WILLPLAYSOUND, true);
		e.putBoolean(GencyGCMConst.PREF_KEY_WILLVIBRATE, true);
		e.commit();
	}
	
	/**
	 * @return boolean 同意済みか否か
	 */
	private boolean isAgreed() {
		// ここで同意済みかどうか確認 ( YYYYMMDDHH )
		return mEulaVer <= mPref.getInt(PREF_KEY_AGREEMENT, 0);
	}

    /**
     * 許諾済みか否か
     * @return true = 許諾済み / false = 未許諾
     */
	public boolean shouldShow() {
		if (mPref.getInt(PREF_KEY_AGREEMENT, 0) == 0) {
			return true;
		}
		return false;
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
//		finish();
		isShown = false;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		isShown = false;
		if (mDismissHooker != null) {
			mDismissHooker.handleDismiss();
		}
	}

//	/**
//	 * 
//	 */
//	private void finish(){
//		((Activity)mContext).finish();
//	}
	
	/**
	 * @author user
	 *
	 */
	private class LocalClient extends WebViewClient{
		private ProgressBar mProgress;

		/**
		 * @param progress
		 */
		public LocalClient(ProgressBar progress) {
			mProgress = progress;
		}

		@Override
		public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
			mProgress.setVisibility(View.VISIBLE);
			view.setVisibility(View.INVISIBLE);
		}

		@Override
		public void onPageFinished(WebView view, String url){
			mProgress.setVisibility(View.INVISIBLE);
			view.setVisibility(View.VISIBLE);
		}
	}
}
package com.gency.commons.dialog;

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
import android.app.Activity;

/**
 * <h3>利用規約を表示するダイアログ</h3>
 *
 * <b>使い方</b><br />
 * @see jp.co.cybird.android.lib.gcm.GCMAgreementDialog
 *
 */
public class GencyBaseAgreementDialog implements OnCancelListener, OnDismissListener {

	private Dialog mDialog;
	private Context mContext;
	private int mEulaVer;
	private String mEulaUrl;
	private int mDisplayHeight;
	/** 同意したかを保存する先 */
	protected SharedPreferences mPref;
	private String PREF_FILE_NAME;
	protected String PREF_KEY_AGREEMENT;
	static private boolean isShown = false;
	private WebView mWebview;
	private String LAYOUT_NAME;
	private String DIALOG_TITLE;

	/**
	 *
	 * @param context Android context
	 * @param eulaVersion 利用規約の最終更新日 YYYYMMDDXX (ex.2013010101)
	 * @param eulaUrl 利用規約のURL
	 * @param prefKey 利用規約表示有無の保存用SharedPreferencesのキー
	 * @param prefFileName 利用規約表示有無の保存用SharedPreferencesのファイル名
	 * @param layoutName レイアウト (Optional)
	 * @param title 利用規約ダイアログに表示するタイトル (Optional)
	 */
	@SuppressWarnings("deprecation")
	public GencyBaseAgreementDialog(Context context, int eulaVersion, String eulaUrl, String prefKey, String prefFileName, String layoutName, String title) {
		mContext = context;
		mEulaVer = eulaVersion;
		mEulaUrl = eulaUrl;
		PREF_KEY_AGREEMENT = prefKey;
		PREF_FILE_NAME = prefFileName == null ? "cy_agreement_dialog" : prefFileName;
		LAYOUT_NAME = layoutName == null ? "lib_gcm_agreement_dialog" : layoutName;
		DIALOG_TITLE = title == null ? GencyParameterLoader.getString("LIB_GCM_DIALOG_TITLE", mContext) : title;
		mPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
		
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
				
				
				setTitle(DIALOG_TITLE);

				// Eula用レイアウトをセット
				setContentView(GencyParameterLoader.getResourceIdForType(LAYOUT_NAME, "layout", mContext));

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
						handleDecline();
						dismiss();
					}
				});

				Button agree_button = (Button) findViewById(GencyParameterLoader.getResourceIdForType("lib_gcm_agreement_agree_button", "id", mContext));
				agree_button.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						saveAgreement();
						handleAgree();
						dismiss();
					}
				});
			}
		};
		mDialog.setOnCancelListener(this);
		mDialog.setOnDismissListener(this);
	}
	
	/**
	 * キャンセルになった場合に呼ばれる
	 */
	protected void handleCancel(){};
	
	/**
	 * 同意されなかった場合に呼ばれる
	 */
	protected void handleDecline(){};
	
	/**
	 * 同意された場合に呼ばれる
	 */
	protected void handleAgree(){};

	/**
	 * {@link Activity}クラスの{@link Activity#onPause()}時の処理
	 */
	public void onPause() {
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
			isShown = true;
		}
	}

	/**
	 * {@link Activity}クラスの{@link Activity#onResume()}時の処理
	 */
	public void onResume() {
		if (isShown) {
			mDialog.show();
		}
	}

	/**
	 * ダイアログを表示.{@link #createDialog(Context)}後に使用可能
	 */
	public void showAgreement() {
		if (mDialog != null 
				&& ! isAgreed()
				){
			mDialog.show();
		}
	}

	/**
	 * 同意したことを保存
	 */
	public void saveAgreement() {
		// 同意したEULAのバージョンを保存
		Editor e = mPref.edit();
		e.putInt(PREF_KEY_AGREEMENT, mEulaVer);
		e.commit();
	}
	
	/**
	 * 同意したかどうかを取得
	 * @return boolean {@code true}:同意済み, {@code false}:同意していない
	 */
	public boolean isAgreed() {
		// ここで同意済みかどうか確認 ( YYYYMMDDHH )
		return mEulaVer <= mPref.getInt(PREF_KEY_AGREEMENT, 0);
	}

	/**
	 * 
	 */
	@Override
	public void onCancel(DialogInterface dialog) {
		isShown = false;
		handleCancel();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		isShown = false;

	}

	/**
	 * 同意ダイアログ内に使用しているWebViewクラスのための{@link WebViewClient}カスタムクラス
	 */
	private class LocalClient extends WebViewClient{
		private ProgressBar mProgress;

		/**
		 * @param progress
		 */
		public LocalClient(ProgressBar progress) {
			mProgress = progress;
		}

		/*
		 * (non-Javadoc)
		 * @see android.webkit.WebViewClient#onPageStarted(android.webkit.WebView, java.lang.String, android.graphics.Bitmap)
		 */
		@Override
		public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
			mProgress.setVisibility(View.VISIBLE);
			view.setVisibility(View.INVISIBLE);
		}

		/*
		 * (non-Javadoc)
		 * @see android.webkit.WebViewClient#onPageFinished(android.webkit.WebView, java.lang.String)
		 */
		@Override
		public void onPageFinished(WebView view, String url){
			mProgress.setVisibility(View.INVISIBLE);
			view.setVisibility(View.VISIBLE);
		}
	}
}
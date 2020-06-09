/**
 * Copyright (c) 2015年 CYBIRD Co., Ltd.
 */

package jp.co.cybird.android.agreement;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import jp.co.cybird.android.compliance.R;
import jp.co.cybird.android.utils.AsyncGetVersionTask;

public class BaseAgreementDialog implements OnCancelListener, OnDismissListener, OnClickListener, AsyncGetVersionTask.OnResponseReceive {

    private Dialog mDialog;
    private Context mContext;
    private int mEulaVer;
    private long mEulaDateVer = 0;
    private String mEulaUrl;
    private int mDisplayHeight;
    protected SharedPreferences mPref;
    private String PREF_FILE_NAME = "lib_cy_agreement_pref";
    private String PREF_KEY_AGREEMENT = "lib_cy_agreement_dialog";
    private String PREF_KEY_VERSION = "lib_cy_agreement_version";
    static private boolean isShown = false;
    private boolean mDisableAgePolicy = false;      ///< 未成年者に対して親権者の許諾を取る旨を表示するTextの表示有無(false:表示する, true:表示しない).

    private String DIALOG_TITLE = null;
    private String AGREE = null;
    private String DECLINE = null;

    private OnAgreeListener mAgreeListener = null;
    private OnNoUpdateListener mNoUpdateListener = null;
    private OnDeclineListener mDeclineListener = null;
    private OnCancelListener mCancelListener = null;
    private OnDissmissListener mDissmissListener = null;
    private OnUrlLinkTappedListener mUrlLinkTappedListener = null;

    public Button mAgreeButton;
    public Button mDeclineButton;

    public Button mBrowserBack;
    public Button mBrowserForward;

    private WebView mWebView;
    private Button mReloadButton;
    private ProgressBar mProgress;
    private boolean isError;

    private int webViewBackgroundResourceId = 0;

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    protected BaseAgreementDialog(Context context, int eulaVersion, String eulaUrl, String title, String agree, String decline) {
        mContext = context;
        mEulaVer = eulaVersion;
        mEulaUrl = eulaUrl;
        if (title != null) {
            DIALOG_TITLE = title;
        } else {
            DIALOG_TITLE = context.getString(R.string.agreement_title);
        }
        if (agree != null) {
            AGREE = agree;
        }
        if (decline != null) {
            DECLINE = decline;
        }
        mPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);

        // Get Screen Size
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) < 13) {
            mDisplayHeight = display.getHeight();
        } else {
            Point size = new Point();
            display.getSize(size);
            mDisplayHeight = size.y;
        }

        createDialog(context);

    }

    /**
     * @param context
     */
    private void createDialog(Context context) {
        mDialog = new Dialog(context) {

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);

                // タイトル
                setTitle(DIALOG_TITLE);

                // LinearLayoutを作成
                LinearLayout linearLayout = new LinearLayout(mContext);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                setContentView(linearLayout);

                // FrameLayoutを作成
                FrameLayout frame = new FrameLayout(mContext);
                LinearLayout.LayoutParams layoutParams =
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, mDisplayHeight / 2, 1);
                frame.setLayoutParams(layoutParams);
                linearLayout.addView(frame);

                // reloadbuttonを作成
                mReloadButton = new Button(mContext);
                mReloadButton.setText("Retry");
                mReloadButton.setVisibility(View.GONE);
                FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params2.gravity = Gravity.CENTER;
                frame.addView(mReloadButton, params2);
                mReloadButton.setOnClickListener(BaseAgreementDialog.this);

                // ProgressBarを作成
                ProgressBar progressBar = new ProgressBar(mContext);

                // WebViewを作成
                mWebView = new WebView(mContext);

                // キャッシュさせない!
                WebSettings webviewSettings = mWebView.getSettings();
                webviewSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

                // ブラウザページ操作用View
                View browser = getLayoutInflater().inflate(R.layout.layout_agreement_browser_button, null);

                if (webViewBackgroundResourceId != 0) {
                    mWebView.setBackgroundColor(Color.TRANSPARENT);
                    frame.setBackgroundResource(webViewBackgroundResourceId);
                    browser.setBackgroundResource(webViewBackgroundResourceId);
                } else {
                    int color = mContext.getResources().getColor(R.color.agreement_dialog_base_background);
                    mWebView.setBackgroundColor(color);
                    browser.setBackgroundColor(color);
                }

                // 読み込み中はProgressBar表示
                mWebView.setWebViewClient(new LocalClient(progressBar));

                frame.addView(mWebView);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER;
                progressBar.setLayoutParams(params);
                frame.addView(progressBar);

                // WebViewの下にボタン置く
                mBrowserBack = (Button) browser.findViewById(R.id.browserBackButton);
                mBrowserBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mWebView.canGoBack()) {
                            mWebView.goBack();
                        }
                    }
                });
                mBrowserForward = (Button) browser.findViewById(R.id.browserForwardButton);
                mBrowserForward.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mWebView.canGoForward()) {
                            mWebView.goForward();
                        }
                    }
                });
                linearLayout.addView(browser);

                View footer = getLayoutInflater().inflate(R.layout.layout_agreement_footer, null);

                if (footer != null && mDisableAgePolicy) {
                    // 未成年者へ親権者の許諾が必要な旨を表示するText文.
                    TextView agePolicy = (TextView) footer.findViewById(R.id.textView_agepolicy);
                    if (agePolicy != null) {
                        // mDisableAgePolicyフラグが有効な場合は表示しない.
                        agePolicy.setVisibility(View.GONE);
                    }
                }

                // 閉じるボタン
                mDeclineButton = (Button) footer.findViewById(R.id.btn_decline);
                if (DECLINE != null) {
                    mDeclineButton.setText(DECLINE);
                }

                mDeclineButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mDeclineListener != null) {
                            mDeclineListener.onDecline();
                        }
                        dismiss();

                    }
                });
                mDeclineButton.setEnabled(false);

                mAgreeButton = (Button) footer.findViewById(R.id.btn_agree);
                if (AGREE != null) {
                    mAgreeButton.setText(AGREE);
                }
                mAgreeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveAgreement();
                        if (mAgreeListener != null) {
                            mAgreeListener.onAgree();
                        }
                        dismiss();
                    }
                });
                mAgreeButton.setEnabled(false);
                linearLayout.addView(footer);

                // ロード開始
                //（LocalClient内でボタンインスタンスを制御しているので、ボタンインスタンス生成後にloadUrl()してください。）
                mWebView.loadUrl(mEulaUrl);

            }
        };
        mDialog.setOnCancelListener(this);
        mDialog.setOnDismissListener(this);
    }

    public void onPause() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            isShown = true;
        }
    }

    public void onResume() {
        if (isShown) {
            mDialog.show();
        }
    }

    public void show() {
        if (mDialog != null ) {
            getEulaPageData();
        }
    }

    private void saveAgreement() {
        // 同意したEULAのバージョンを保存
        Editor e = mPref.edit();
        e.putInt(PREF_KEY_AGREEMENT, mEulaVer);
        e.putLong(PREF_KEY_VERSION, mEulaDateVer);
        e.commit();
    }

    /**
     * @return boolean 同意済みか否か
     */
    public boolean isAgreed() {
        // ここで同意済みかどうか確認 ( YYYYMMDDHH )
        return mEulaVer <= mPref.getInt(PREF_KEY_AGREEMENT, 0);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        isShown = false;
        if (mCancelListener != null) {
            mCancelListener.onCancel();
        }
    }

    public void dismiss() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        isShown = false;
        if (mDissmissListener != null) {
            mDissmissListener.onDissmiss();
        } else {
            dismiss();
        }
    }

    private class LocalClient extends WebViewClient {

        /**
         * @param progress
         */
        public LocalClient(ProgressBar progress) {
            mProgress = progress;
            isError = false;
        }

        @Override
        public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
            mProgress.setVisibility(View.VISIBLE);
            view.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (!isError) {
                mProgress.setVisibility(View.INVISIBLE);
                view.setVisibility(View.VISIBLE);
                mAgreeButton.setEnabled(true);
                mDeclineButton.setEnabled(true);
                setVisibilityBrowserBackOrForwardButton();
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            mReloadButton.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.INVISIBLE);
            isError = true;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (mUrlLinkTappedListener != null) {
                return mUrlLinkTappedListener.onUrlLinkTapped(view, url);
            }
//            return super.shouldOverrideUrlLoading(view, url);
            // 通常はブラウザを開く
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            mContext.startActivity(intent);
            return true;
        }

        private void setVisibilityBrowserBackOrForwardButton() {

            if (mWebView.canGoForward()) {
                mBrowserForward.setVisibility(View.VISIBLE);
            } else {
                mBrowserForward.setVisibility(View.INVISIBLE);
            }

            if (mWebView.canGoBack()) {
                mBrowserBack.setVisibility(View.VISIBLE);
            } else {
                mBrowserBack.setVisibility(View.INVISIBLE);
            }

            if (!mWebView.canGoForward() && !mWebView.canGoBack()) {
                mBrowserForward.setVisibility(View.GONE);
                mBrowserBack.setVisibility(View.GONE);

            }
        }
    }

    public void setCanceledOnTouchOutside(boolean cancel) {
        if (mDialog != null) {
            mDialog.setCanceledOnTouchOutside(cancel);
        }
    }

    interface OnAgreeListener {
        public void onAgree();
    }

    interface OnNoUpdateListener {
        public void onNoUpdate();
    }

    interface OnDeclineListener {
        public void onDecline();
    }

    interface OnCancelListener {
        public void onCancel();
    }

    interface OnDissmissListener {
        public void onDissmiss();
    }

    interface OnUrlLinkTappedListener {
        /**
         * shouldOverrideUrlLoadingで呼ばれます。
         *
         * @return 元処理に戻したい場合はfalseを返す。
         */
        boolean onUrlLinkTapped(WebView view, String url);
    }

    public void setOnAgreeListener(OnAgreeListener agreeListener) {
        mAgreeListener = agreeListener;
    }

    public void setNoUpdateListener(OnNoUpdateListener noUpdateListener) {
        mNoUpdateListener = noUpdateListener;
    }


    public void setOnDeclineListener(OnDeclineListener declineListener) {
        mDeclineListener = declineListener;
    }

    public void setOnCancelListener(OnCancelListener cancelListener) {
        mCancelListener = cancelListener;
    }

    public void setOnDissmissListener(OnDissmissListener dismissListener) {
        mDissmissListener = dismissListener;
    }

    public void setOnUrlLinkTappedLiesener(OnUrlLinkTappedListener listener) {
        mUrlLinkTappedListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (mProgress != null) {
            mProgress.setVisibility(View.VISIBLE);
        }
        if (mWebView != null) {
            mWebView.setVisibility(View.INVISIBLE);
        }
        if (mReloadButton != null) {
            mReloadButton.setVisibility(View.GONE);
        }
        isError = false;
        if (mWebView != null) {
            mWebView.loadUrl(mEulaUrl);
        }

    }

    /**
     * 規約を表示するWebViewの背景画像を指定する
     *
     * @param resourceId
     */
    public void setWebViewBackground(int resourceId) {
        webViewBackgroundResourceId = resourceId;
    }


    /**
     * 利用許諾に表示している未成年者は親権者の許諾が必要な旨のTextの表示/非表示を設定する.
     *
     * @param disable true:非表示にする  false:表示する.
     */
    public void disableAgePolicy(boolean disable) {
        mDisableAgePolicy = disable;
    }

    public boolean getEulaPage() {
        boolean result = false;

        AsyncGetVersionTask task = new AsyncGetVersionTask(mContext);
        task.setOnResponseReceive(this);
        task.execute(mEulaUrl);

        return result;
    }

    /**
     * 規約ページのデータを非同期で取得する
     *
     */
    private void getEulaPageData() {
        AsyncGetVersionTask task = new AsyncGetVersionTask( mContext );
        task.setOnResponseReceive( this );
        task.execute( mEulaUrl );
    }

    @Override
    public void onResponseReceive(String ret) {
        boolean newVersion = false;

        int eulaIndex = -1;

        if ( ret != null ) {
            eulaIndex = ret.indexOf("meta eula-version=", 0);
        }

        if( eulaIndex >= 0 ) {

            int verStartIndex = ret.indexOf("=", eulaIndex) + 1;
            int verEndIndex = ret.indexOf(">", verStartIndex + 1 );
            String tmp = ret.substring(verStartIndex, verEndIndex);
            String[] list = tmp.split("\"");

            String valStr = null;
            for ( String val: list ) {
                if ( val != null && val.length() > 0 ) {
                    valStr = val;
                    break;
                }
            }

            if ( valStr != null && valStr.length() > 0 ) {
                try {
                    mEulaDateVer = Long.parseLong(valStr);
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
            }
        }

        Long lastVersionVal = mPref.getLong(PREF_KEY_VERSION, -1); // 同意済みバージョンを取得

        if (lastVersionVal < mEulaDateVer) {
            newVersion = true;
        }

        if ( newVersion || !isAgreed() ) {
            if ( mDialog != null ) {
                mDialog.show();
            }
        } else {
            mNoUpdateListener.onNoUpdate();
        }
    }
}
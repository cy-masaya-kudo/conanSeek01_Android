/**
 * Copyright (c) 2015年 CYBIRD Co., Ltd.
 */

package jp.co.cybird.android.minors;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jp.co.cybird.android.utils.CustomRelativeLayout;
import jp.co.cybird.android.utils.Util;

/**
 * MinorsDialogControllerのベースクラス
 */
public abstract class ControllerBase implements View.OnClickListener, AdapterView.OnItemSelectedListener, OnWebViewClientEventLinstener {
    protected static final int AGE_CONFIRMATION_DIALOG = 1000;
    protected static final int AGE_REGISTRATION_DIALOG = 1001;
    protected static final int MINOR_CONSENT_DIALOG = 1002;
    protected static final int MINOR_CONSENT_ERROR_DIALOG = 1003;

    protected Context mContext = null;

    protected int mEulaVer = 0;
    protected String mMinorUrl = null;
    protected int mDisplayHeight = 0;
    protected int mDisplayWidth = 0;

    protected WebView mWebView = null;
    protected String mMinorConfUrl = null;
    protected String mMinorErrorUrl = null;
    protected Button mReloadButton = null;
    protected TextView mErrorTextView = null;
    protected Button mPositiveButton = null;
    protected ProgressBar mProgress = null;
    protected boolean isError = false;

    protected static int mSpinnerLayoutResId = 0;
    protected static int mSpinnerDropdownLayoutResId = 0;

    protected int mYear = -1;
    protected int mMonth = -1;

    protected MinorsDialogListener.OnAgreeListener mAgreeListener = null;
    protected MinorsDialogListener.OnDeclineListener mDeclineListener = null;
    protected MinorsDialogListener.OnCancelListener mCancelListener = null;
    protected MinorsDialogListener.OnDismissListener mDismissListener = null;

    protected static boolean isShown = false;
    protected static boolean enableDismiss = false;
    protected static boolean isCanceledOnTouchOutside = true;

    protected Resources mResources = null;

    protected Calendar mCurrentCalendar = null;

    public ControllerBase(Calendar calendar) {
        mCurrentCalendar = calendar;
    }

    /**
     * 年齢登録ダイアログのview作成
     * 見た目を弄りたい場合はこのメソッドを編集すること。
     *
     * @param context
     * @param viewGroup
     * @return 年齢登録ダイアログのview
     */
    protected CustomRelativeLayout getAgeConfDialogView(Context context, ViewGroup viewGroup) {
        LayoutInflater factory = LayoutInflater.from(context);
        View view = factory.inflate(getResourceId("layout_age_conf_dialog", "layout"), viewGroup, false);
        CustomRelativeLayout rootView = (CustomRelativeLayout) view.findViewById(getResourceId("layout_base", "id"));

        // エラー文言表示領域
        mErrorTextView = (TextView) rootView.findViewById(getResourceId("age_conf_text_error", "id"));
        mErrorTextView.setVisibility(View.INVISIBLE);

        // YearSpinnerセッティング　age_conf_year_spinnter
        LinearLayout ll = (LinearLayout) rootView.findViewById(getResourceId("main_content", "id"));
        ArrayAdapter<Integer> yearAdapter = new ArrayAdapter<Integer>
                (context, mSpinnerLayoutResId, createYearList(getInteger("min_year"), getInteger("max_year")));
        yearAdapter.setDropDownViewResource(mSpinnerDropdownLayoutResId);
        Spinner yearSpinner = (Spinner) ll.findViewById(getResourceId("age_conf_year_spinner", "id"));
        yearSpinner.setTag("Year");
        yearSpinner.setAdapter(yearAdapter);
        yearSpinner.setSelection(yearAdapter.getCount() - 1);
        yearSpinner.setOnItemSelectedListener(this);

        // MonthSpinnerセッティング　age_conf_month_spinnter
        ArrayAdapter<Integer> monthAdapter = new ArrayAdapter<Integer>
                (context, mSpinnerLayoutResId, createMonthList());
        monthAdapter.setDropDownViewResource(mSpinnerDropdownLayoutResId);
        Spinner monthSpinner = (Spinner) ll.findViewById(getResourceId("age_conf_month_spinner", "id"));
        monthSpinner.setTag("Month");
        monthSpinner.setAdapter(monthAdapter);
        monthSpinner.setSelection(currentMonth());
        monthSpinner.setOnItemSelectedListener(this);

        return rootView;
    }

    /**
     * 年齢確認ダイアログのview作成
     * 見た目を弄りたい場合はこのメソッドを編集すること。
     *
     * @param context
     * @param viewGroup
     * @return 年齢確認ダイアログのview
     */
    protected CustomRelativeLayout getAgeRegistDialog(Context context, ViewGroup viewGroup) {
        LayoutInflater factory = LayoutInflater.from(context);
        CustomRelativeLayout rootView = (CustomRelativeLayout) factory.inflate(getResourceId("layout_age_regist_dialog", "layout"), viewGroup, false);

        // エラー文言表示領域
        mErrorTextView = (TextView) rootView.findViewById(getResourceId("age_regist_text_error", "id"));

        // 生年月　age_regist_birth_year_month
        String birth = (String) DateFormat.format(getString("age_regist_date_format"), Util.birthCalendar(mYear, mMonth));
        ((TextView) rootView.findViewById(getResourceId("age_regist_birth_year_month", "id"))).setText(birth);

        // 歳　age_regist_age
        String age = getString("age_regist_age", Util.ageCalculation(Util.birthCalendar(mYear, mMonth), mCurrentCalendar));
        ((TextView) rootView.findViewById(getResourceId("age_regist_age", "id"))).setText(age);

        return rootView;
    }

    /**
     * 親権者同意ダイアログのview作成
     * 見た目を弄りたい場合はこのメソッドを編集すること。
     *
     * @param context
     * @param viewGroup
     * @return 親権者同意ダイアログのview
     */
    protected CustomRelativeLayout getMinorConfDialogView(Context context, ViewGroup viewGroup, String url) {
        LayoutInflater factory = LayoutInflater.from(context);
        CustomRelativeLayout rootView = (CustomRelativeLayout) factory.inflate(getResourceId("layout_minor_conf_dialog", "layout"), viewGroup, false);

        // reloadbuttonを作成
        mReloadButton = (Button) rootView.findViewById(getResourceId("retrybutton", "id"));
        if (mReloadButton != null) mReloadButton.setOnClickListener(this);

        // ProgressBarを作成
        ProgressBar progressBar = (ProgressBar) rootView.findViewById(getResourceId("progress_bar", "id"));

        // WebViewを作成
        mWebView = (WebView) rootView.findViewById(getResourceId("webview", "id"));
        if (mWebView != null) {
            // キャッシュさせない!
            WebSettings webviewSettings = mWebView.getSettings();
            webviewSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
            webviewSettings.setAllowFileAccess(true);

            // 読み込み中はProgressBar表示
            LocalClient lc = new LocalClient(progressBar);
            mWebView.setWebViewClient(lc);

            // ロード開始
            //（LocalClient内でボタンインスタンスを制御しているので、ボタンインスタンス生成後にloadUrl()してください。)
            mMinorUrl = url;
            mWebView.loadUrl(mMinorUrl);
        }

        return rootView;
    }

    /**
     * カスタムしたWebViewClient<br>
     * WebViewにて表示がうまくいかない場合の表示調整用。
     */
    protected class LocalClient extends WebViewClient {

        public LocalClient(ProgressBar progress) {
            super();
            mProgress = progress;
            isError = false;
        }

        @Override
        public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
            mProgress.setVisibility(View.VISIBLE);
            view.setVisibility(View.INVISIBLE);
            mOnWVCEventLinstener.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (!isError) {
                mProgress.setVisibility(View.INVISIBLE);
                view.setVisibility(View.VISIBLE);
                mOnWVCEventLinstener.onPageFinished(view, url);
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            mReloadButton.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.INVISIBLE);
            isError = true;
            mOnWVCEventLinstener.onReceivedError(view, errorCode, description, failingUrl);
        }
    }

    /**
     * 小クラスにWebViewの読み込みイベントを通知するためのリスナー用変数
     */
    protected OnWebViewClientEventLinstener mOnWVCEventLinstener = null;

    /**
     * 小クラスにWebViewの読み込みイベントを通知するためのリスナーをセット
     *
     * @param wvcEventLinstener
     */
    protected void setOnWVCEventLinstener(OnWebViewClientEventLinstener wvcEventLinstener) {
        this.mOnWVCEventLinstener = wvcEventLinstener;
    }

    /**
     * 年齢入力時の年Spinnerに表示するListを生成
     *
     * @param minYear 表示最大年 (default 1900）
     * @param maxYear 表示最少年（default 今年）
     * @return minからmaxまでの年リスト
     */
    protected List<Integer> createYearList(int minYear, int maxYear) {
        if (maxYear >= currentYear() || maxYear < minYear) {
            // 不正値と認識した場合は強制的に「今年」
            maxYear = currentYear();
        }
        if (minYear > currentYear() || minYear > maxYear) {
            // 不正値と認識した場合は強制的に1900
            minYear = 1900;
        }
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = minYear; i <= maxYear; i++) {
            list.add(i);
        }
        return list;
    }

    /**
     * 年齢入力時の月Spinnerに表示するListを生成
     *
     * @return 1から12までの月リスト
     */
    protected static List<Integer> createMonthList() {
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 1; i < 13; i++) {
            list.add(i);
        }
        return list;
    }

    /**
     * ライブラリに設定されたCurrentのCalendarクラスから年を取得
     *
     * @return 今年
     */
    protected int currentYear() {
        return mCurrentCalendar.get(Calendar.YEAR);
    }

    /**
     * ライブラリに設定されたCurrentのCalendarクラスから月を取得
     *
     * @return 今月
     */
    protected int currentMonth() {
        return mCurrentCalendar.get(Calendar.MONTH);
    }

    /**
     * Resourcesクラスからリソースintegerを取得できるラッパーメソッド
     *
     * @param name リソースidをStringにしたもの
     * @return リソースに定義されているInt
     */
    protected int getInteger(String name) {
        return mResources != null ? mResources.getInteger(getResourceId(name, "integer")) : -1;
    }

    /**
     * Resourcesクラスからリソースstringを取得できるラッパーメソッド
     *
     * @param name リソースid
     * @return リソースに定義されているString
     */
    protected String getString(String name) {
        return mResources != null ? mResources.getString(getResourceId(name, "string")) : null;
    }

    /**
     * Resourcesクラスからリソースstringを取得できるラッパーメソッド
     *
     * @param name       リソースidをStringにしたもの
     * @param formatArgs
     * @return リソースに定義されているString
     */
    protected String getString(String name, Object... formatArgs) {
        return mResources != null ? mResources.getString(getResourceId(name, "string"), formatArgs) : null;
    }

    /**
     * 文字列からリソースidを取得できるラッパークラス
     *
     * @param name
     * @param defType
     * @return
     */
    protected int getResourceId(String name, String defType) {
        int ret = mResources != null ? mResources.getIdentifier(name, defType, mContext.getPackageName()) : 0;
        if (ret == 0) {
            ret = mResources != null ? mResources.getIdentifier(name, defType, this.getClass().getPackage().getName()) : 0;
        }
        return ret;
    }

    /**
     * リロードボタンを押した際の動作
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        int buttonId = v.getId();
        if (buttonId == getResourceId("retrybutton", "id")) {
            // webview再読み込みボタン
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
                mWebView.loadUrl(mMinorUrl);
            }
        } else {
            // error 何もしない
        }
    }

    /**
     * 表示バージョンをセットできる
     *
     * @param mEulaVer
     */
    public void setEulaVer(int mEulaVer) {
        this.mEulaVer = mEulaVer;
    }

    /**
     * 親権者同意画面の文言用URLを返却する。
     * MinorsDialogManager初期化時にURLを指定していない場合はstrings.xmlに定義したminor_conf_urlを参照する。
     * @return 親権者同意画面の文言用URL
     */
    public String getMinorConfUrl(){
        if(mMinorConfUrl == null){
            mMinorConfUrl = getString("minor_conf_url", Util.getRandomString(1));
        }
        return mMinorConfUrl;
    }

    /**
     * 親権者同意拒否後のエラー画面の文言用URLを返却する。
     * MinorsDialogManager初期化時にURLを指定していない場合はstrings.xmlに定義したminor_error_urlを参照する。
     * @return 親権者同意拒否後のエラー画面の文言用URL
     */
    public String getMinorErrorUrl(){
        if(mMinorErrorUrl == null){
            mMinorErrorUrl = getString("minor_error_url", Util.getRandomString(1));
        }
        return mMinorErrorUrl;
    }
}

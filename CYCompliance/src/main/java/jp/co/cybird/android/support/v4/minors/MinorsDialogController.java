/**
 * Copyright (c) 2015年 CYBIRD Co., Ltd.
 */

package jp.co.cybird.android.support.v4.minors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.v4.app.FragmentManager;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.util.Calendar;

import jp.co.cybird.android.minors.ControllerBase;
import jp.co.cybird.android.minors.MinorsDialogListener;
import jp.co.cybird.android.minors.MinorsPref;
import jp.co.cybird.android.utils.Util;

/**
 * 複数のダイアログを条件に合わせて画面遷移のように表示させるクラス.
 * <p>Before API level 9 (Android 2.3/Gingerbread)</p>
 */
class MinorsDialogController extends ControllerBase implements DialogFragmentListener {

    private FragmentManager mFragmentManager;
    private CustomDialogFragment mCurrentDialog;

    public MinorsDialogController(Context context, int eulaVersion, FragmentManager fragmentManager, Calendar currentCalendar, String minorConfUrl, String minorErrorUrl) {
        this(context, eulaVersion, fragmentManager, -1, -1, currentCalendar, minorConfUrl, minorErrorUrl);
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public MinorsDialogController(Context context, int eulaVersion, FragmentManager fragmentManager,
                                  int spinnerLayoutResId, int dropdownLayoutResId, Calendar currentCalendar, String minorConfUrl, String minorErrorUrl) {
        super(currentCalendar);
        // init
        mFragmentManager = fragmentManager;
        mContext = context;
        mEulaVer = eulaVersion;
        mResources = context.getResources();
        mYear = currentYear();
        mMonth = currentMonth();

        mMinorConfUrl = minorConfUrl;
        mMinorErrorUrl = minorErrorUrl;

        if (currentCalendar == null) {
            mCurrentCalendar = Calendar.getInstance();
        } else {
            mCurrentCalendar = currentCalendar;
        }

        if (spinnerLayoutResId < 0) {
            mSpinnerLayoutResId = android.R.layout.simple_spinner_item;
        } else {
            mSpinnerLayoutResId = spinnerLayoutResId;
        }
        if (dropdownLayoutResId < 0) {
            mSpinnerDropdownLayoutResId = android.R.layout.simple_spinner_dropdown_item;
        } else {
            mSpinnerDropdownLayoutResId = dropdownLayoutResId;
        }

        // Get Screen Size
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) < 13) {
            mDisplayHeight = display.getHeight();
            mDisplayWidth = display.getWidth();
        } else {
            Point size = new Point();
            display.getSize(size);
            mDisplayHeight = size.y;
            mDisplayWidth = size.x;
        }

        setOnWVCEventLinstener(this);
    }

    /**
     * 年齢確認のCustomDialogFragmentインスタンス生成
     *
     * @return CustomDialogFragmentインスタンス
     */
    public CustomDialogFragment createAgeConfDialogFragment() {
        if (mContext == null) return null;
        CustomDialogFragment ageConfDialog = new CustomDialogFragment.Builder()
                .setNegativeButtonTitle(getString("age_conf_cancel"))
                .setPositiveButtonTitle(getString("age_conf_confirmation"))
                .setTitle(getString("age_conf_title"))
                .setView(getAgeConfDialogView(mContext, null))
                .setCancelable(isCanceledOnTouchOutside)
                .setDialogFragmentListener(AGE_CONFIRMATION_DIALOG, this)
                .create();
        mCurrentDialog = ageConfDialog;
        return ageConfDialog;
    }

    /**
     * 年齢確認のCustomDialogFragmentインスタンス生成
     *
     * @return CustomDialogFragmentインスタンス
     */
    public CustomDialogFragment createAgeRegistDialogFragment() {
        if (mContext == null) return null;
        CustomDialogFragment ageRegistDialog = new CustomDialogFragment.Builder()
                .setNegativeButtonTitle(getString("age_conf_cancel"))
                .setPositiveButtonTitle(getString("age_regist_registration"))
                .setTitle(getString("age_regist_title"))
                .setView(getAgeRegistDialog(mContext, null))
                .setCancelable(isCanceledOnTouchOutside)
                .setDialogFragmentListener(AGE_REGISTRATION_DIALOG, this)
                .create();
        mCurrentDialog = ageRegistDialog;
        return ageRegistDialog;
    }

    /**
     * 親権者同意のCustomDialogFragmentインスタンス生成
     *
     * @return CustomDialogFragmentインスタンス
     */
    public CustomDialogFragment createMinorsDialogFragment() {
        if (mContext == null) return null;
        CustomDialogFragment minorConsentDialog = new CustomDialogFragment.Builder()
                .setNegativeButtonTitle(getString("minor_conf_decline"))
                .setPositiveButtonTitle(getString("minor_conf_agree"))
                .setView(getMinorConfDialogView(mContext, null, getMinorConfUrl()))
                .setTitle(getString("minor_conf_title"))
                .setCancelable(isCanceledOnTouchOutside)
                .setFlags(CustomDialogFragment.DISABLE_POSITIVE_BUTTON)
                .setDialogFragmentListener(MINOR_CONSENT_DIALOG, this)
                .create();
        mCurrentDialog = minorConsentDialog;
        return minorConsentDialog;
    }

    /**
     * 親権者同意エラー時のCustomDialogFragmentインスタンス生成
     *
     * @return CustomDialogFragmentインスタンス
     */
    public CustomDialogFragment createMinorsErrorDialogFragment() {
        if (mContext == null) return null;
        CustomDialogFragment minorConsentErrorDialog = new CustomDialogFragment.Builder()
                .setNegativeButtonTitle(getString("minor_error_close"))
                .setView(getMinorConfDialogView(mContext, null, getMinorErrorUrl()))
                .setTitle(getString("minor_error_title"))
                .setCancelable(isCanceledOnTouchOutside)
                .setDialogFragmentListener(MINOR_CONSENT_ERROR_DIALOG, this)
                .createForceCancelDialog();
        mCurrentDialog = minorConsentErrorDialog;
        return minorConsentErrorDialog;
    }

    public void onPause() {
        if (mCurrentDialog != null && mCurrentDialog.isShowing()) {
            mCurrentDialog.dismiss();
            isShown = true;
        }
    }

    public void onResume() {
        if (isShown) {
            mCurrentDialog.show(mFragmentManager);
        }
    }

    public CustomDialogFragment getCurrentDialog() {
        if (!MinorsPref.sharedInstance(mContext).isAgeRegist(mEulaVer)) {
            enableDismiss = false;
            createAgeConfDialogFragment();
        } else if (!MinorsPref.sharedInstance(mContext).isMinorAgreed(mEulaVer)) {
            enableDismiss = false;
            createMinorsDialogFragment();
        }
        return mCurrentDialog;
    }

    /**
     * ダイアログ表示<br>
     * 年齢入力がされていなければ年齢入力ダイアログが表示される。<br>
     * 年齢入力がされていて、未成年者であり、なおかつ親権者同意が得られていない場合は、親権者同意画面が表示される。<br>
     * 年齢入力済み（親権者同意済み）であれば、本メソッドを実行してもダイアログは表示されない。
     *
     * @param fragmentManager
     */
    public void show(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
        if (!MinorsPref.sharedInstance(mContext).isAgeRegist(mEulaVer)) {
            enableDismiss = false;
            createAgeConfDialogFragment();
            mCurrentDialog.show(mFragmentManager);
        } else if (!MinorsPref.sharedInstance(mContext).isMinorAgreed(mEulaVer)) {
            enableDismiss = false;
            createMinorsDialogFragment();
            mCurrentDialog.show(mFragmentManager);
        }
    }

    public void dismiss() {
        if (mCurrentDialog != null) {
            enableDismiss = true;
            mCurrentDialog.dismiss();
        }
    }

    public void setCanceledOnTouchOutside(boolean cancel) {
        isCanceledOnTouchOutside = cancel;
        if (mCurrentDialog != null) {
            mCurrentDialog.setCancelable(cancel);
        }
    }

    public void setOnAgreeListener(MinorsDialogListener.OnAgreeListener agreeListener) {
        mAgreeListener = agreeListener;
    }

    public void setOnDeclineListener(MinorsDialogListener.OnDeclineListener declineListener) {
        mDeclineListener = declineListener;
    }

    public void setOnCancelListener(MinorsDialogListener.OnCancelListener cancelListener) {
        mCancelListener = cancelListener;
    }

    public void setOnDismissListener(MinorsDialogListener.OnDismissListener dismissListener) {
        mDismissListener = dismissListener;
    }

    public boolean isShowing(){
        return getCurrentDialog().isShowing();
    }

    @Override
    public void onEvent(int id, int event, CustomDialogFragment dialogFragment) {

        if (event == DialogFragmentListener.ON_DISMISS) {
            if (enableDismiss) {
                isShown = false;
                if (mDismissListener != null) {
                    mDismissListener.onDismiss();
                } else {
                    mCurrentDialog.dismiss();
                }
            }
        } else if (event == DialogFragmentListener.ON_CANCEL) {
            if (id == AGE_REGISTRATION_DIALOG) {
                // 年齢確認にキャンセルになった場合は年齢登録ダイアログを表示する。
                createAgeConfDialogFragment().show(mFragmentManager);
            } else {
                isShown = false;
            }
            if (mCancelListener != null) {
                mCancelListener.onCancel();
            }
        } else {
            if (id == AGE_CONFIRMATION_DIALOG) {
                if (event == DialogFragmentListener.ON_POSITIVE_BUTTON_CLICKED) {
                    // 年齢登録 登録ボタン
                    dialogFragment.dismiss();
                    createAgeRegistDialogFragment().show(mFragmentManager);
                } else if (event == DialogFragmentListener.ON_NEGATIVE_BUTTON_CLICKED) {
                    // 年齢登録　キャンセルボタン
                    isShown = false;
                    enableDismiss = true;
                    dialogFragment.dismiss();
                    if (mCancelListener != null) {
                        mCancelListener.onCancel();
                    }
                }
            } else if (id == AGE_REGISTRATION_DIALOG) {
                if (event == DialogFragmentListener.ON_POSITIVE_BUTTON_CLICKED) {
                    // 年齢確認 登録ボタン
                    boolean issave = MinorsPref.sharedInstance(mContext).saveBirthYearAndDay(mContext, mYear, mMonth, mEulaVer, mCurrentCalendar);
                    if (!Util.isMinor(mYear, mMonth, mCurrentCalendar)) {
                        // 成年
                        if (issave) {
                            mErrorTextView.setVisibility(View.INVISIBLE);
                            // 成年の場合は親権者の同意を得られたことにする。
                            MinorsPref.sharedInstance(mContext).saveMinorAgreement(mEulaVer, mCurrentCalendar);
                            if (mAgreeListener != null) {
                                mAgreeListener.onAgree();
                            }
                            enableDismiss = true;
                            dialogFragment.dismiss();
                        } else {
                            mErrorTextView.setVisibility(View.VISIBLE);
                            mErrorTextView.setText(getString("age_conf_error02"));
                        }
                    } else {
                        // 未成年
                        dialogFragment.dismiss();
                        createMinorsDialogFragment();
                        mCurrentDialog.show(mFragmentManager);
                    }
                } else if (event == DialogFragmentListener.ON_NEGATIVE_BUTTON_CLICKED) {
                    // 年齢確認 キャンセルボタン
                    dialogFragment.dismiss();
                    createAgeConfDialogFragment().show(mFragmentManager);
                }
            } else if (id == MINOR_CONSENT_DIALOG) {
                if (event == DialogFragmentListener.ON_POSITIVE_BUTTON_CLICKED) {
                    // 親権者同意 同意ボタン
                    enableDismiss = true;
                    MinorsPref.sharedInstance(mContext).saveMinorAgreement(mEulaVer, mCurrentCalendar);
                    if (mAgreeListener != null) {
                        mAgreeListener.onAgree();
                    }
                    dialogFragment.dismiss();
                } else {
                    // 親権者同意 キャンセルボタン
                    dialogFragment.dismiss();
                    createMinorsErrorDialogFragment().show(mFragmentManager);
                }
            } else {
                if (event == DialogFragmentListener.ON_NEGATIVE_BUTTON_CLICKED) {
                    // 親権者同意 エラー画面　閉じるボタン
                    enableDismiss = true;
                    if (mDeclineListener != null) {
                        mDeclineListener.onDecline();
                    }
                    dialogFragment.dismiss();
                }
            }
        }
    }

    /**
     * @param adapterView
     * @param view
     * @param i
     * @param l
     * @see AdapterView.OnItemSelectedListener
     */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Spinner spinner = (Spinner) adapterView;
        // 選択されたアイテムを取得します
        if (spinner.getId() == getResourceId("age_conf_year_spinner", "id")) {
            mYear = (Integer) spinner.getSelectedItem();
        } else {
            mMonth = (Integer) spinner.getSelectedItem();
        }

        if (Util.isFuture(mYear, mMonth, mCurrentCalendar)) {
            if (mErrorTextView != null) {
                mErrorTextView.setVisibility(View.VISIBLE);
                mErrorTextView.setText(getString("age_conf_error01"));
            }
            if (mCurrentDialog != null && mCurrentDialog.getButton(CustomDialogFragment.BUTTON_POSITIVE) != null) {
                mCurrentDialog.getButton(CustomDialogFragment.BUTTON_POSITIVE).setEnabled(false);
            }
        } else {
            if (mErrorTextView != null) {
                mErrorTextView.setVisibility(View.INVISIBLE);
            }
            if (mCurrentDialog != null && mCurrentDialog.getButton(CustomDialogFragment.BUTTON_POSITIVE) != null) {
                mCurrentDialog.getButton(CustomDialogFragment.BUTTON_POSITIVE).setEnabled(true);
            }
        }
    }

    /**
     * @param adapterView
     * @see AdapterView.OnItemSelectedListener
     */
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    /**
     * WebViewClientのイベントを通知する
     *
     * @param view
     * @param url
     * @param favicon
     */
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        if (mCurrentDialog != null && mCurrentDialog.getButton(CustomDialogFragment.BUTTON_POSITIVE) != null) {
            mCurrentDialog.getButton(CustomDialogFragment.BUTTON_POSITIVE).setEnabled(false);
        }
    }

    /**
     * WebViewClientのイベントを通知する
     *
     * @param view
     * @param url
     */
    @Override
    public void onPageFinished(WebView view, String url) {
        if (mCurrentDialog != null && mCurrentDialog.getButton(CustomDialogFragment.BUTTON_POSITIVE) != null) {
            mCurrentDialog.getButton(CustomDialogFragment.BUTTON_POSITIVE).setEnabled(true);
        }
    }

    /**
     * WebViewClientのイベントを通知する
     *
     * @param view
     * @param errorCode
     * @param description
     * @param failingUrl
     */
    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (mCurrentDialog != null
                && mCurrentDialog.getButton(CustomDialogFragment.BUTTON_POSITIVE) != null
                && mCurrentDialog.getButton(CustomDialogFragment.BUTTON_POSITIVE).getVisibility() == View.VISIBLE) {
            mCurrentDialog.getButton(CustomDialogFragment.BUTTON_POSITIVE).setEnabled(false);
        }
    }
}
/**
 * Copyright (c) 2015年 CYBIRD Co., Ltd.
 */

package jp.co.cybird.android.agreement;

import android.app.Activity;
import android.content.Context;
import android.webkit.WebView;

import jp.co.cybird.android.agreement.BaseAgreementDialog.OnAgreeListener;
import jp.co.cybird.android.agreement.BaseAgreementDialog.OnNoUpdateListener;
import jp.co.cybird.android.agreement.BaseAgreementDialog.OnCancelListener;
import jp.co.cybird.android.agreement.BaseAgreementDialog.OnDeclineListener;
import jp.co.cybird.android.agreement.BaseAgreementDialog.OnDissmissListener;
import jp.co.cybird.android.agreement.BaseAgreementDialog.OnUrlLinkTappedListener;

public class AgreementDialog implements OnAgreeListener, OnDeclineListener, OnCancelListener,
        OnDissmissListener, OnUrlLinkTappedListener, OnNoUpdateListener {

    private Activity mActivity;
    private boolean mForce = true;
    private BaseAgreementDialog mAgreementDialog;

    private OnAgreeListener mAgreeListener = null;
    private OnNoUpdateListener mNoUpdateListener = null;
    private OnDeclineListener mDeclineListener = null;
    private OnCancelListener mCancelListener = null;
    private OnDismissListener mDismissListener = null;
    private OnUrlLinkTappedListener mUrlLinkTappedListener = null;


    public AgreementDialog(Context context, int eulaVersion, String eulaUrl, boolean force) {
        this(context, eulaVersion, eulaUrl, force, null, null, null);
    }

    public AgreementDialog(Context context, int eulaVersion, String eulaUrl, boolean force, String title, String agree, String decline) {
        mActivity = (Activity) context;
        mForce = force;
        mAgreementDialog = new BaseAgreementDialog(mActivity, eulaVersion, eulaUrl, title, agree, decline);
        mAgreementDialog.setCanceledOnTouchOutside(force);
        mAgreementDialog.setOnAgreeListener(this);
        mAgreementDialog.setNoUpdateListener(this);
        mAgreementDialog.setOnDeclineListener(this);
        mAgreementDialog.setOnCancelListener(this);
        mAgreementDialog.setOnUrlLinkTappedLiesener(this);
    }

    public void show() {
        if (mAgreementDialog != null) {
            mAgreementDialog.show();
        }
    }

    public boolean isAgreed() {
        return mAgreementDialog != null ? mAgreementDialog.isAgreed() : false;
    }

    @Override
    public void onAgree() {
        if (mAgreeListener != null) {
            mAgreeListener.onAgree();
        }
    }

    @Override
    public void onNoUpdate() {
        if (mNoUpdateListener != null) {
            mNoUpdateListener.onNoUpdate();
        }
    }

    @Override
    public void onCancel() {
        if (mCancelListener != null) {
            mCancelListener.onCancel();
        } else {
            // ここで戻るボタン押下時とDialog以外の部分押下時に対応
            if (mActivity != null) {
                mActivity.finish();
            }
        }
    }

    @Override
    public void onDecline() {
        // リスナーが設定されている場合はそれに従う
        if (mDeclineListener != null) {
            mDeclineListener.onDecline();
        } else {
            // 強制同意させる場合にはアプリを強制終了
            if (mForce && mActivity != null) {
                mActivity.finish();
            }
        }
    }

    @Override
    public void onDissmiss() {
        if (mDismissListener != null) {
            mDismissListener.onDissmiss();
        }
    }

    @Override
    public boolean onUrlLinkTapped(WebView view, String url) {
        if (mUrlLinkTappedListener != null) {
            return mUrlLinkTappedListener.onUrlLinkTapped(view, url);
        }
        return false;
    }

    public interface OnAgreeListener {
        public void onAgree();
    }

    public interface OnNoUpdateListener {
        public void onNoUpdate();
    }

    public interface OnDeclineListener {
        public void onDecline();
    }

    public interface OnCancelListener {
        public void onCancel();
    }

    public interface OnDismissListener {
        public void onDissmiss();
    }

    public interface OnUrlLinkTappedListener {
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

    public void setOnDismissListener(OnDismissListener dismissListener) {
        mDismissListener = dismissListener;
    }

    public void setOnUrlLinkTappedLiesener(OnUrlLinkTappedListener listener) {
        mUrlLinkTappedListener = listener;
    }

    /**
     * 規約を表示するWebViewの背景画像を指定する
     *
     * @param resourceId
     */
    public void setWebViewBackground(int resourceId) {
        mAgreementDialog.setWebViewBackground(resourceId);
    }

    /**
     * 利用許諾に表示している未成年者は親権者の許諾が必要な旨のTextの表示/非表示を設定する.
     *
     * @param disable true:非表示にする  false:表示する.
     */
    public void disableAgePolicy(boolean disable) {
        mAgreementDialog.disableAgePolicy(disable);
    }
}

/**
 * Copyright (c) 2015年 CYBIRD Co., Ltd.
 */

package jp.co.cybird.android.support.v4.minors;


import android.content.Context;
import android.support.v4.app.FragmentManager;

import java.util.Calendar;

import jp.co.cybird.android.minors.ManagerBase;
import jp.co.cybird.android.minors.MinorsDialogListener;
import jp.co.cybird.android.utils.Util;

/**
 * 年齢確認ダイアログを表示するクラス.
 * <p>Before API level 9 (Android 2.3/Gingerbread)</p>
 */
public class MinorsDialogManager extends ManagerBase {

    private MinorsDialogController mMinorsDialogController;
    private FragmentManager mFragmentManager;

    /**
     * 年齢確認ダイアログを表示、データ管理を行う
     * @param context
     * @param eulaVersion 表示バージョン(規約が変更されたなどの再表示したい場合はバージョンをインクリメントすること）
     * @param fragmentManager DaialogFragmentコントロール用
     */
    public MinorsDialogManager(Context context, int eulaVersion, FragmentManager fragmentManager){
        this(context, eulaVersion, fragmentManager, Calendar.getInstance(), null, null);
    }

    /**
     * 年齢確認ダイアログを表示、データ管理を行う
     * @param context
     * @param eulaVersion 表示バージョン(規約が変更されたなどの再表示したい場合はバージョンをインクリメントすること）
     * @param fragmentManager DaialogFragmentコントロール用
     * @param minorConfUrl 親権者同意画面の文言用URL
     * @param minorErrorUrl 親権者同意拒否後のエラー画面の文言用URL
     */
    public MinorsDialogManager(Context context, int eulaVersion, FragmentManager fragmentManager, String minorConfUrl, String minorErrorUrl){
        this(context, eulaVersion, fragmentManager, Calendar.getInstance(), minorConfUrl, minorErrorUrl);
    }

    /**
     * 年齢確認ダイアログを表示、データ管理を行う
     * @param context
     * @param eulaVersion 表示バージョン(規約が変更されたなどの再表示したい場合はバージョンをインクリメントすること）
     * @param fragmentManager DaialogFragmentコントロール用
     * @param currentCalendar 年齢計算用に基準にしたいカレンダークラス
     */
    public MinorsDialogManager(Context context, int eulaVersion, FragmentManager fragmentManager, Calendar currentCalendar){
        this(context, eulaVersion, fragmentManager, currentCalendar, null, null);
    }

    /**
     * 年齢確認ダイアログを表示、データ管理を行う
     * @param context
     * @param eulaVersion 表示時期の設定
     * @param fragmentManager DaialogFragmentコントロール用
     * @param currentCalendar 年齢計算用に基準にしたいカレンダークラス
     * @param minorConfUrl 親権者同意画面の文言用URL
     * @param minorErrorUrl 親権者同意拒否後のエラー画面の文言用URL
     */
    public MinorsDialogManager(Context context, int eulaVersion, FragmentManager fragmentManager, Calendar currentCalendar, String minorConfUrl, String minorErrorUrl){
        mContext = context;
        mEulaVer = eulaVersion;
        mFragmentManager = fragmentManager;
        mCurrentCalendar = currentCalendar;
        mMinorsDialogController = new MinorsDialogController(context, eulaVersion, fragmentManager, currentCalendar, minorConfUrl, minorErrorUrl);
        mMinorsDialogController.setOnAgreeListener(this);
        mMinorsDialogController.setOnDeclineListener(this);
        mMinorsDialogController.setOnCancelListener(this);
        mMinorsDialogController.setOnDismissListener(this);
    }

    /**
     * ダイアログ表示<br>
     * 年齢入力がされていなければ年齢入力ダイアログが表示される。<br>
     * 年齢入力がされていて、未成年者であり、なおかつ親権者同意が得られていない場合は、親権者同意画面が表示される。<br>
     * 年齢入力済み（親権者同意済み）であれば、本メソッドを実行してもダイアログは表示されない。
     */
    public void show(){
        if(mFragmentManager == null ) return;
        mMinorsDialogController.show(mFragmentManager);
    }

    /**
     * ダイアログの外側をタップしてダイアログを閉じれるかどうか
     * @param enabledTouchOutside
     */
    public void setCanceledOnTouchOutside(boolean enabledTouchOutside){
        if(mMinorsDialogController != null) {
            mMinorsDialogController.setCanceledOnTouchOutside(enabledTouchOutside);
        }
    }

    /**
     * （成年：年齢登録時、未成年：親権者同意時）のイベントを取得できるリスナー
     * @param agreeListener
     */
    public void setOnAgreeListener(MinorsDialogListener.OnAgreeListener agreeListener) {
        mOnAgreeListener = agreeListener;
    }

    /**
     * 親権者同意しなかったイベントを取得できるリスナー
     * @param declineListener
     */
    public void setOnDeclineListener(MinorsDialogListener.OnDeclineListener declineListener) {
        mOnDeclineListener = declineListener;
    }

    /**
     * ダイアログにてcancelされたイベントを取得できるリスナー
     * @param cancelListener
     */
    public void setOnCancelListener(MinorsDialogListener.OnCancelListener cancelListener) {
        mOnCancelListener = cancelListener;
    }

    /**
     * ダイアログにてdismissされたイベントを取得できるリスナー
     * @param dismissListener
     */
    public void setOnDismissListener(MinorsDialogListener.OnDismissListener dismissListener) {
        mOnDismissListener = dismissListener;
    }

    @Override
    public void onAgree() {
        if(mOnAgreeListener != null){
            mOnAgreeListener.onAgree();
        }
    }

    @Override
    public void onCancel() {
        if(mOnCancelListener != null){
            mOnCancelListener.onCancel();
        }
    }

    @Override
    public void onDecline() {
        // リスナーが設定されている場合はそれに従う
        if(mOnDeclineListener != null){
            mOnDeclineListener.onDecline();
        }
    }

    @Override
    public void onDismiss() {
        if(mOnDismissListener != null){
            mOnDismissListener.onDismiss();
        }
    }

    /**
     * 未成年かどうか
     * @param context
     * @param currentCalendar
     * @return true:未成年である
     */
    @Override
    public boolean isMinor(Context context, Calendar currentCalendar) {
        boolean ret = true;
        if (!isAgreement()) return ret;
        int year = getBirthYear();
        int month = getBirthMonth();
        if(year > 0 && month > 0) {
            ret = Util.isMinor(year, month, currentCalendar);
        }
        return ret;
    }

    /**
     * @return ダイアログが表示中か
     */
    public boolean isShowing(){
        if(mMinorsDialogController != null){
            return mMinorsDialogController.isShowing();
        }
        return false;
    }

    /**
     * ダイアログの表示有無を決めるバージョン
     * @param eulaVer 表示バージョン(規約が変更されたなどの再表示したい場合はバージョンをインクリメントすること）
     */
    @Override
    public void setEulaVer(int eulaVer) {
        this.mEulaVer = eulaVer;
        if(mMinorsDialogController != null){
            mMinorsDialogController.setEulaVer(this.mEulaVer);
        }
    }
}

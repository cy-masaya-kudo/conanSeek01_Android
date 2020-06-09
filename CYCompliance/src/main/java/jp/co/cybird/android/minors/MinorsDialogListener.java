/**
 * Copyright (c) 2015年 CYBIRD Co., Ltd.
 */

package jp.co.cybird.android.minors;

/**
 * ダイアログの表示や同意イベントを通知するリスナーをまとめたクラス
 */
public class MinorsDialogListener {
    /**
     * 同意したかを通知
     */
    public interface OnAgreeListener{
        void onAgree();
    }

    /**
     * 同意せずにダイアログを閉じたかを通知
     */
    public interface OnDeclineListener{
        void onDecline();
    }

    /**
     * ダイアログクラスのCancelイベントを通知
     */
    public interface OnCancelListener{
        void onCancel();
    }

    /**
     * ダイアログクラスのDismissイベントを通知
     */
    public interface OnDismissListener{
        void onDismiss();
    }
}

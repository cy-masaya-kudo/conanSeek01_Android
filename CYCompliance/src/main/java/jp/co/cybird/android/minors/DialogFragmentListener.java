/**
 * http://qiita.com/GeneralD/items/89278fc0f5513abe0bb3
 * Copyright (c) 2015年 CYBIRD Co., Ltd.
 */

package jp.co.cybird.android.minors;

import android.annotation.TargetApi;
import android.os.Build;

/**
 * FragmentDialogからのイベント通知用Listener
 * <p>Before API level 11 (Android 3.0/Honeycomb)</p>
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public interface DialogFragmentListener {

    public static final int ON_DISMISS = 0;
    public static final int ON_CANCEL = 1;
    public static final int ON_POSITIVE_BUTTON_CLICKED = 2;
    public static final int ON_NEGATIVE_BUTTON_CLICKED = 3;
    public static final int ON_NEUTRAL_BUTTON_CLICKED = 4;
    public static final int ON_CLOSE_BUTTON_CLICKED = 5;

    /**
     * On event.
     *
     * @param id
     *            リスナーインスタンスに対してのid
     * @param event
     *            以下のイベントが通知されます。 {@link #ON_DISMISS} {@link #ON_CANCEL},
     *            {@link #ON_POSITIVE_BUTTON_CLICKED}, {@link #ON_NEGATIVE_BUTTON_CLICKED},
     *            {@link #ON_CLOSE_BUTTON_CLICKED}.
     */
    void onEvent(int id, int event, CustomDialogFragment dialogFragment);
}
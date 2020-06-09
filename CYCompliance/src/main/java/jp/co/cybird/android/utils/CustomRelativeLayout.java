/**
 * Copyright (c) 2015年 CYBIRD Co., Ltd.
 */

package jp.co.cybird.android.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import java.io.Serializable;

import jp.co.cybird.android.minors.CustomDialogFragment;

/**
 * CustomDialogFragmentのdialog_main_content領域に表示するLayout部分をSerializable化したもの
 *
 * @see CustomDialogFragment
 * @see jp.co.cybird.android.support.v4.minors.CustomDialogFragment
 */
public class CustomRelativeLayout extends RelativeLayout implements Serializable {

    private static final long serialVersionUID = 1L;

    public CustomRelativeLayout(Context context) {
        super(context);
    }

    public CustomRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}

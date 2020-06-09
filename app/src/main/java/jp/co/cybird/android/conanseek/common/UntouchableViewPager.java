package jp.co.cybird.android.conanseek.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import jp.co.cybird.android.conanseek.manager.Common;

/**
 * 自分でスクロールできないViewpager
 */
public class UntouchableViewPager extends android.support.v4.view.ViewPager {

    private boolean enabled;

    public UntouchableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.enabled = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.enabled) {
            return super.onTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.enabled) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }

    public void setPagingEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
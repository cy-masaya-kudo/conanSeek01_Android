package jp.co.cybird.android.conanseek.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;

import jp.souling.android.conanseek01.R;

/**
 * 矢印
 */
public class ArrowButton extends BaseButton {

    private boolean blink;

    public ArrowButton(Context context) {
        super(context);
    }

    public ArrowButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ArrowButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setBlink(boolean blink) {

        if (blink) {
            if (this.blink != blink) {
                this.blink = blink;
                setVisibility(View.VISIBLE);
                setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.blink));
            }
        } else {
            this.blink = blink;
            setAnimation(null);
            setVisibility(View.GONE);
        }
    }
}
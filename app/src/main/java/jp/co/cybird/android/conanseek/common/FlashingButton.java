package jp.co.cybird.android.conanseek.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;

/**
 * 点滅するボタン
 */
public class FlashingButton extends ImageButton {

    public FlashingButton(Context context) {
        super(context);
    }

    public FlashingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlashingButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 通常点滅開始
     */
    public void startFlashing() {
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(400);
        anim.setStartOffset(0);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        this.startAnimation(anim);
    }

    /**
     * 早い点滅開始
     */
    public void startFastFlashing() {
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(70);
        anim.setStartOffset(0);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        //this.startAnimation(anim);
    }

    /**
     * 点滅終わり
     */
    public void stopFlashing() {
        this.clearAnimation();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
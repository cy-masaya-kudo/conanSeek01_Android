package jp.co.cybird.android.conanseek.common;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageButton;

/**
 * 触ると暗くなるボタン
 */
public class BaseButton extends ImageButton {

    public boolean enableAlpha = false;


    public BaseButton(Context context) {
        super(context);
    }

    public BaseButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEnabled()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    this.setColorFilter(new LightingColorFilter(Color.DKGRAY, 0));
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_OUTSIDE:
                    this.clearColorFilter();
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enableAlpha) {
            if (enabled) {
                this.setAlpha(1f);
            } else {
                this.setAlpha(0.5f);
            }
        }
    }
}
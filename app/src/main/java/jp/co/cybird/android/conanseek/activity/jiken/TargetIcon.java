package jp.co.cybird.android.conanseek.activity.jiken;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import jp.souling.android.conanseek01.R;

/**
 * エフェクトのターゲットアイコン
 */
public class TargetIcon extends ImageView {

    //ターゲットID
    public int location_id;

    public Point size;

    public TargetIcon(Context context) {
        super(context);
        init();
    }

    public TargetIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TargetIcon(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        this.setImageResource(R.mipmap.rader);


        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager)  getContext().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        size = new Point();
        size.x = (int)(100 * displayMetrics.density);
        size.y = (int)(100 * displayMetrics.density);
        /*
        FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(
                size.x, size.y
        );*/

        this.setLayoutParams(new ViewGroup.LayoutParams(
                size.x,
                size.y
        ));

    }

    public void startAnimation() {


        setScaleX(8f);
        setScaleY(8f);

        ViewCompat.animate(this)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(View view) {
                        blinkAnimation();
                    }
                })
                .start();

    }


    private void blinkAnimation() {
        if (getAlpha() < 1.0) {

            setAlpha(0f);
            ViewCompat.animate(this)
                    .alpha(1f)
                    .setDuration(600)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(View view) {
                            blinkAnimation();
                        }
                    })
                    .start();
        } else {

            setAlpha(1f);
            ViewCompat.animate(this)
                    .alpha(0f)
                    .setDuration(600)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(View view) {
                            blinkAnimation();
                        }
                    })
                    .start();
        }

    }
}

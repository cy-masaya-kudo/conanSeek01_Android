package jp.co.cybird.android.conanseek.activity.effect;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import jp.souling.android.conanseek01.R;

/**
 * 捜査シーン
 */
public class SagashiSceneEffect extends BaseEffect implements View.OnClickListener {

    FrameLayout backgroundView;
    ImageView fadeInObj;
    ImageView fadeOutObj;

    Style style;

    public enum Style {
        START,
        WIN,
        LOSE
    }

    boolean buttonTypeFlag;
    private boolean animated = false;


    public static SagashiSceneEffect newInstance(Style style) {

        Bundle args = new Bundle();
        String styleString = style.toString();

        args.putString("style", styleString);

        SagashiSceneEffect fragment = new SagashiSceneEffect();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.effect_sagashi_start, container, false);

        Bundle arg = getArguments();
        String styleString = "";
        if (arg != null) {
            styleString = arg.getString("style", "");
        }
        switch (styleString) {
            case "START":
                this.style = Style.START;
                break;
            case "WIN":
                this.style = Style.WIN;
                break;
            default:
                this.style = Style.LOSE;
                break;
        }


        backgroundView = (FrameLayout) view.findViewById(R.id.wrapper);

        fadeInObj = (ImageView) view.findViewById(R.id.obj_fadein);
        fadeOutObj = (ImageView) view.findViewById(R.id.obj_fadeout);

        switch (style) {

            case START:
                fadeInObj.setImageResource(R.mipmap.text_start);
                fadeOutObj.setImageResource(R.mipmap.text_start);
                break;
            case WIN:
                fadeInObj.setImageResource(R.mipmap.text_clear);
                fadeOutObj.setImageResource(R.mipmap.text_clear);
                buttonTypeFlag = true;
                break;
            case LOSE:
                fadeInObj.setImageResource(R.mipmap.text_mistake);
                fadeOutObj.setImageResource(R.mipmap.text_mistake);
                buttonTypeFlag = true;
                break;
        }


        fadeInObj.setAlpha(0f);
        fadeInObj.setScaleX(4f);
        fadeInObj.setScaleY(4f);

        fadeOutObj.setAlpha(0f);


        return view;
    }

    @Override
    protected void startAnimation() {
        super.startAnimation();

        ViewTreeObserver observer = backgroundView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                if (animated) return;
                animated = true;

                ViewCompat.animate(fadeInObj)
                        .alpha(1f)
                        .setDuration(300)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .setListener(new ViewPropertyAnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(View view) {

                                fadeOutObj.setAlpha(1f);
                                ViewCompat.animate(fadeOutObj)
                                        .alpha(0f)
                                        .setDuration(300)
                                        .scaleX(4f)
                                        .scaleY(4f)
                                        .setInterpolator(new OvershootInterpolator())
                                        .setListener(new ViewPropertyAnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(View view) {

                                                if (buttonTypeFlag) {
                                                    backgroundView.setOnClickListener(SagashiSceneEffect.this);
                                                    //addPushClose();
                                                } else {
                                                    if (eListener != null)
                                                        eListener.effectFinished();
                                                    removeMe();
                                                }

                                            }
                                        })
                                        .start();

                            }
                        })
                        .start();


                // Once data has been obtained, this listener is no longer needed, so remove it...
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    backgroundView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    backgroundView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {

        if (eListener != null)
            eListener.effectFinished();
        removeMe();
    }
}

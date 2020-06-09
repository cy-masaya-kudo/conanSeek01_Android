package jp.co.cybird.android.conanseek.activity.effect;

import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import jp.co.cybird.android.conanseek.manager.Common;
import jp.souling.android.conanseek01.R;

/**
 * カード出現
 */
public class CardEffect extends BaseEffect {

    FrameLayout backgroundView;
    ImageView lineView;
    FrameLayout cardFrame;
    ImageView cardView;
    ImageView shineView;

    int cardID;

    private ImageView lineWork;
    private AnimationDrawable lineAnim;

    public static CardEffect newInstance(int cardID) {

        Bundle args = new Bundle();

        args.putInt("cardID", cardID);

        CardEffect fragment = new CardEffect();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.effect_card, container, false);

        Bundle arg = getArguments();
        if (arg != null) {
            cardID = arg.getInt("cardID");
        }

        backgroundView = (FrameLayout) view.findViewById(R.id.wrapper);

        lineView = (ImageView) view.findViewById(R.id.anim_line);
        cardFrame = (FrameLayout) view.findViewById(R.id.card_wrapper);
        cardView = (ImageView) view.findViewById(R.id.card_frame_0);
        shineView = (ImageView) view.findViewById(R.id.card_shine);

        lineWork = (ImageView) view.findViewById(R.id.anim_line);
        lineWork.setBackgroundResource(R.drawable.shuuchuusen);
        lineAnim = (AnimationDrawable) lineWork.getBackground();

        cardView.setImageBitmap(Common.decodedAssetBitmap("egara/426x600/" + cardID + ".jpg", 220, 300));

        cardFrame.setAlpha(0f);

        return view;
    }

    private boolean animated = false;

    @Override
    protected void startAnimation() {
        super.startAnimation();


        ViewTreeObserver observer = backgroundView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                if (animated) return;
                animated = true;

                shineView.setX(-1000);
                shineView.setAlpha(0f);
                cardFrame.setY(3000);

                ViewCompat.animate(cardFrame)
                        .alpha(1f)
                        .setDuration(800)
                        .y(getView().getHeight() / 2.0f - cardFrame.getHeight() / 2.0f)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .setListener(new ViewPropertyAnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(View view) {

                                ViewCompat.animate(shineView)
                                        .alpha(1f)
                                        .setDuration(300)
                                        .x(2000)
                                        .setInterpolator(new LinearInterpolator());
                                //.start();

                                ViewCompat.animate(cardFrame)
                                        .alpha(0f)
                                        .setDuration(800)
                                        .setStartDelay(200)
                                        .y(-cardFrame.getHeight())
                                        .setInterpolator(new AccelerateInterpolator())
                                        .setListener(new ViewPropertyAnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(View view) {
                                                new Handler().post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        finished();
                                                    }
                                                });

                                            }
                                        }).start();

                            }
                        }).start();

                shuuchuusenAnimation(true);


                // Once data has been obtained, this listener is no longer needed, so remove it...
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    backgroundView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    backgroundView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

    }

    private void finished () {

        if (eListener != null)
            eListener.effectFinished();
        removeMe();
    }


    private void shuuchuusenAnimation(boolean enable) {

        if (enable && lineAnim != null) {
            lineWork.setVisibility(View.VISIBLE);
            lineAnim.start();
        }
        if (!enable && lineAnim != null) {
            lineAnim.stop();
            lineWork.setVisibility(View.GONE);
        }
    }
}

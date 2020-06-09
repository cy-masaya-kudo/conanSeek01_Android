package jp.co.cybird.android.conanseek.activity.gacha;

import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;

import jp.co.cybird.android.conanseek.activity.card.CardFragment;
import jp.co.cybird.android.conanseek.activity.card.CardShojiDetailFragment;
import jp.co.cybird.android.conanseek.common.BaseActivity;
import jp.co.cybird.android.conanseek.common.BaseFragment;
import jp.co.cybird.android.conanseek.manager.BgmManager;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.CsvManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.manager.UserInfoManager;
import jp.co.cybird.android.conanseek.param.APIResponseParam;
import jp.co.cybird.android.conanseek.param.CardParam;
import jp.souling.android.conanseek01.R;

/**
 * ガチャエフェクト
 */
public class GachaEffectFragment extends BaseFragment {

    private ImageView bgView;

    private SeManager.SeName rareVoice;
    private Bitmap ballBitmap;
    private Bitmap doorBgBitmaps[] = new Bitmap[3];
    private Bitmap cardImages[] = new Bitmap[10];
    private boolean large = false;

    ImageView ballImage;

    FrameLayout cardFrame;

    private ImageView lineWork;
    private AnimationDrawable lineAnim;

    private ArrayList<APIResponseParam.Item.Card> cardList;

    public static GachaEffectFragment newInstance(ArrayList<APIResponseParam.Item.Card> cardSerialList) {

        Bundle args = new Bundle();

        args.putSerializable("cardSerialList", cardSerialList);

        GachaEffectFragment fragment = new GachaEffectFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_gacha_effect, container, false);

        Bundle arg = getArguments();
        if (arg != null) {
            this.cardList = (ArrayList<APIResponseParam.Item.Card>) arg.getSerializable("cardSerialList");
        }

        bgmName = BgmManager.BgmName.GACHA;

        bgView = (ImageView) view.findViewById(R.id.gacha_anim_bg);

        doorBgBitmaps[0] = Common.decodedResource(R.mipmap.anim_door1_1, 160, 90);
        doorBgBitmaps[1] = Common.decodedResource(R.mipmap.anim_door2_1, 100, 50);
        doorBgBitmaps[2] = Common.decodedResource(R.mipmap.anim_door3_1, 320, 180);

        ArrayList<APIResponseParam.Item.Card> myCardList = UserInfoManager.myCardList();

        int index = 0;
        int rareValue = 0;
        large = cardList.size() == 1;
        for (APIResponseParam.Item.Card resultCard : cardList) {
            for (APIResponseParam.Item.Card card : myCardList) {
                if (card.id == resultCard.id) {
                    CardParam cardParam = CsvManager.cardByCardID(card.card_id);
                    if (cardParam.rareInt > rareValue) rareValue = cardParam.rareInt;
                    if (!large)
                        cardImages[index] = Common.decodedAssetBitmap("egara/326x460/" + card.card_id + ".jpg", 100, 140, 0.5f);
                    else
                        cardImages[index] = Common.decodedAssetBitmap("egara/426x600/" + card.card_id + ".jpg", 220, 300, 0.5f);
                    index++;
                    break;
                }
            }
        }

        if (rareValue <= 1) {
            ballBitmap = Common.decodedResource(R.mipmap.anim_ball_normal, 120, 120);
            rareVoice = SeManager.SeName.GACHA_RARITY_HN;
        } else if (rareValue == 2) {
            ballBitmap = Common.decodedResource(R.mipmap.anim_ball_silver, 120, 120);
            rareVoice = SeManager.SeName.GACHA_RARITY_R;
        } else if (rareValue == 3) {
            ballBitmap = Common.decodedResource(R.mipmap.anim_ball_gold, 120, 120);
            rareVoice = SeManager.SeName.GACHA_RARITY_SR;
        } else if (rareValue == 4) {
            ballBitmap = Common.decodedResource(R.mipmap.anim_ball_rainbow, 120, 120);
            rareVoice = SeManager.SeName.GACHA_RARITY_SSR;
        } else {
            rareVoice = SeManager.SeName.GACHA_RARITY_N;
        }

        //音事前準備
        SeManager.resetPrepareCount();
        SeManager.onlyPrepare(rareVoice);
        SeManager.onlyPrepare(SeManager.SeName.GACHA_KICK);
        SeManager.onlyPrepare(SeManager.SeName.GACHA_DOOR_OPEN);


        view.findViewById(R.id.gacha_anim_scene_1).setVisibility(View.VISIBLE);
        view.findViewById(R.id.gacha_anim_scene_2).setVisibility(View.GONE);
        view.findViewById(R.id.gacha_anim_scene_3).setVisibility(View.GONE);

        view.findViewById(R.id.gacha_anim_ball_1).setX(trueDp(240));
        view.findViewById(R.id.gacha_anim_ball_1).setY(trueDp(360));
        view.findViewById(R.id.gacha_anim_conan_1).setX(trueDp(600));

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ballBitmap = null;
        doorBgBitmaps = null;
        cardImages = null;
    }

    @Override
    protected void fragmentDidAppear() {
        super.fragmentDidAppear();

        startScene1();
    }

    private int trueDp(int val) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, val, Common.myAppContext.getResources().getDisplayMetrics());
    }


    private void shuuchuusenAnimation(boolean enable) {

        if (lineWork == null) {
            lineWork = (ImageView) getView().findViewById(R.id.gacha_anim_line);
            lineWork.setBackgroundResource(R.drawable.shuuchuusen);
            lineAnim = (AnimationDrawable) lineWork.getBackground();
        }

        if (enable && lineAnim != null) {
            lineWork.setVisibility(View.VISIBLE);
            lineAnim.start();
        }
        if (!enable && lineAnim != null) {
            lineAnim.stop();
            lineWork.setVisibility(View.GONE);
        }
    }

    private void startScene1() {

        ImageView conan = (ImageView) getView().findViewById(R.id.gacha_anim_conan_1);
        ImageView ball = (ImageView) getView().findViewById(R.id.gacha_anim_ball_1);
        ball.setImageBitmap(ballBitmap);

        ViewPropertyAnimatorListenerAdapter listener = new ViewPropertyAnimatorListenerAdapter() {
            private int counter = 0;

            @Override
            public void onAnimationEnd(View view) {
                super.onAnimationEnd(view);
                counter++;
                if (counter == 2) {
                    startScene2();
                }
            }
        };

        //右からコナン登場
        ViewCompat.animate(conan)
                .x(trueDp(60))
                .setDuration(400)
                .setStartDelay(1000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(listener)
                .start();

        //ボール登場
        ViewCompat.animate(ball)
                .x(trueDp(130))
                .y(trueDp(110))
                .setStartDelay(1000)
                .setDuration(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(listener)
                .start();
    }


    private void startScene2() {
        getView().findViewById(R.id.gacha_anim_scene_1).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.gacha_anim_scene_2).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.gacha_anim_scene_3).setVisibility(View.GONE);

        FrameLayout frame = (FrameLayout) getView().findViewById(R.id.gacha_anim_cat);
        frame.setX(trueDp(640));

        ImageView ball = (ImageView) getView().findViewById(R.id.gacha_anim_ball_2);
        ball.setImageBitmap(ballBitmap);
        ImageView white = (ImageView) getView().findViewById(R.id.gacha_anim_ball_white);
        ImageView shine = (ImageView) getView().findViewById(R.id.gacha_anim_shine_2);

        //カット登場
        ViewCompat.animate(frame)
                .x(0)
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        //ボール回転
        ViewCompat.animate(ball)
                .rotation(-4800)
                .setDuration(2700)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(View view) {
                        super.onAnimationEnd(view);
                        startScene3();
                    }
                })
                .start();

        //ボール発光
        white.setAlpha(0f);
        ViewCompat.animate(white)
                .alpha(0.95f)
                .setStartDelay(700)
                .setDuration(2000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        //ピカピカ
        shine.setAlpha(0f);
        ViewCompat.animate(shine)
                .alpha(0.95f)
                .rotation(60)
                .setStartDelay(1200)
                .setDuration(2000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        SeManager.play(SeManager.SeName.GACHA_KICK);
        shuuchuusenAnimation(true);

    }

    private void startScene3() {

        getView().findViewById(R.id.gacha_anim_scene_1).setVisibility(View.GONE);
        getView().findViewById(R.id.gacha_anim_scene_2).setVisibility(View.GONE);
        getView().findViewById(R.id.gacha_anim_scene_3).setVisibility(View.VISIBLE);

        getView().findViewById(R.id.gacha_anim_card_1).setVisibility(View.GONE);
        getView().findViewById(R.id.gacha_anim_card_10).setVisibility(View.GONE);

        bgView.setImageBitmap(doorBgBitmaps[0]);

        ballImage = (ImageView) getView().findViewById(R.id.gacha_anim_ball_3);
        ballImage.setImageBitmap(ballBitmap);

        //ボール下から右
        ballImage.setX(trueDp(140));
        ballImage.setY(trueDp(360));
        ViewCompat.animate(ballImage)
                .rotation(60)
                .setDuration(140)
                .x(trueDp(640))
                .y(trueDp(140))
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                                 @Override
                                 public void onAnimationEnd(View view) {
                                     super.onAnimationEnd(view);

                                     //ボール右から上
                                     ballImage.setScaleX(0.8f);
                                     ballImage.setScaleY(0.8f);
                                     ballImage.setY(trueDp(80));
                                     ViewCompat.animate(ballImage)
                                             .rotation(60)
                                             .setDuration(140)
                                             .setStartDelay(200)
                                             .x(trueDp(380))
                                             .y(trueDp(-160))
                                             .setInterpolator(new AccelerateDecelerateInterpolator())
                                             .setListener(new ViewPropertyAnimatorListenerAdapter() {
                                                              @Override
                                                              public void onAnimationEnd(View view) {
                                                                  super.onAnimationEnd(view);

                                                                  //ボール中央へ
                                                                  ballImage.setScaleX(0.6f);
                                                                  ballImage.setScaleY(0.6f);
                                                                  ballImage.setX(trueDp(300));

                                                                  ViewCompat.animate(ballImage)
                                                                          .rotation(300)
                                                                          .setDuration(400)
                                                                          .scaleX(0.2f)
                                                                          .scaleY(0.2f)
                                                                          .x(trueDp(240))
                                                                          .y(trueDp(110))
                                                                          .setInterpolator(new AccelerateDecelerateInterpolator())
                                                                          .setListener(new ViewPropertyAnimatorListenerAdapter() {
                                                                                           @Override
                                                                                           public void onAnimationEnd(View view) {
                                                                                               super.onAnimationEnd(view);

                                                                                               //集中線終了
                                                                                               shuuchuusenAnimation(false);

                                                                                               //ボール終了
                                                                                               ballImage.setVisibility(View.GONE);

                                                                                               //画面揺れる
                                                                                               ViewCompat.animate(bgView)
                                                                                                       .scaleX(1.1f)
                                                                                                       .scaleY(1.1f)
                                                                                                       .setDuration(80)
                                                                                                       .setInterpolator(new AccelerateDecelerateInterpolator())
                                                                                                       .setListener(new ViewPropertyAnimatorListenerAdapter() {
                                                                                                           @Override
                                                                                                           public void onAnimationEnd(View view) {
                                                                                                               super.onAnimationEnd(view);

                                                                                                               bgView.setImageBitmap(doorBgBitmaps[1]);

                                                                                                               ViewCompat.animate(bgView)
                                                                                                                       .scaleX(1.0f)
                                                                                                                       .scaleY(1.0f)
                                                                                                                       .setDuration(80)
                                                                                                                       .setInterpolator(new AccelerateDecelerateInterpolator())
                                                                                                                       .setListener(new ViewPropertyAnimatorListenerAdapter() {

                                                                                                                           @Override
                                                                                                                           public void onAnimationEnd(View view) {
                                                                                                                               super.onAnimationEnd(view);
                                                                                                                               bgView.setImageBitmap(doorBgBitmaps[2]);

                                                                                                                               //カード出てくる
                                                                                                                               pushCards();
                                                                                                                           }
                                                                                                                       })
                                                                                                                       .start();
                                                                                                           }
                                                                                                       })
                                                                                                       .start();

                                                                                           }
                                                                                       }

                                                                          )
                                                                          .start();
                                                              }
                                                          }

                                             )
                                             .start();
                                 }
                             }

                )
                .start();

        ImageView shine = (ImageView) getView().findViewById(R.id.gacha_anim_shine_3);
        shine.setAlpha(0f);


    }

    private void pushCards() {

        SeManager.play(SeManager.SeName.GACHA_DOOR_OPEN);

        if (large) {
            getView().findViewById(R.id.gacha_anim_card_1).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.gacha_anim_card_10).setVisibility(View.GONE);

            ((ImageView) getView().findViewById(R.id.card_frame_large)).setImageBitmap(cardImages[0]);

            cardFrame = (FrameLayout) getView().findViewById(R.id.gacha_anim_card_1);

        } else {
            getView().findViewById(R.id.gacha_anim_card_10).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.gacha_anim_card_1).setVisibility(View.GONE);

            ((ImageView) getView().findViewById(R.id.card_frame_0)).setImageBitmap(cardImages[0]);
            ((ImageView) getView().findViewById(R.id.card_frame_1)).setImageBitmap(cardImages[1]);
            ((ImageView) getView().findViewById(R.id.card_frame_2)).setImageBitmap(cardImages[2]);
            ((ImageView) getView().findViewById(R.id.card_frame_3)).setImageBitmap(cardImages[3]);
            ((ImageView) getView().findViewById(R.id.card_frame_4)).setImageBitmap(cardImages[4]);
            ((ImageView) getView().findViewById(R.id.card_frame_5)).setImageBitmap(cardImages[5]);
            ((ImageView) getView().findViewById(R.id.card_frame_6)).setImageBitmap(cardImages[6]);
            ((ImageView) getView().findViewById(R.id.card_frame_7)).setImageBitmap(cardImages[7]);
            ((ImageView) getView().findViewById(R.id.card_frame_8)).setImageBitmap(cardImages[8]);
            ((ImageView) getView().findViewById(R.id.card_frame_9)).setImageBitmap(cardImages[9]);

            cardFrame = (FrameLayout) getView().findViewById(R.id.gacha_anim_card_10);
        }

        //拡大登場
        cardFrame.setScaleY(0.2f);
        cardFrame.setScaleX(0.2f);
        cardFrame.setAlpha(0f);
        ViewCompat.animate(cardFrame)
                .alpha(1.0f)
                .scaleY(1.0f)
                .scaleX(1.0f)
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(View view) {
                        super.onAnimationEnd(view);

                        SeManager.play(rareVoice);

                        cardFrame.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                ((BaseActivity) getActivity()).replaceViewController(
                                        CardFragment.newInstance(CardShojiDetailFragment.newInstance(0, cardList), "gacha", null)
                                );

                            }
                        });

                    }
                })
                .start();

        //ピカピカ
        ImageView shine = (ImageView) getView().findViewById(R.id.gacha_anim_shine_3);
        shine.setAlpha(1f);
        ViewCompat.animate(shine)
                .alpha(0.95f)
                .rotation(60 * 1000 * 3)
                .setDuration(2000 * 1000)
                .setInterpolator(new LinearInterpolator())
                .start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ballBitmap = null;
        doorBgBitmaps[0] = null;
        doorBgBitmaps[1] = null;
        doorBgBitmaps[2] = null;
        doorBgBitmaps = null;
        cardImages[0] = null;
        cardImages[1] = null;
        cardImages[2] = null;
        cardImages[3] = null;
        cardImages[4] = null;
        cardImages[5] = null;
        cardImages[6] = null;
        cardImages[7] = null;
        cardImages[8] = null;
        cardImages[9] = null;
        cardImages = null;
    }
}

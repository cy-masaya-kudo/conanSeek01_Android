package jp.co.cybird.android.conanseek.common;


import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.EventListener;

import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.CsvManager;
import jp.co.cybird.android.conanseek.manager.SaveManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.param.TutorialParam;
import jp.souling.android.conanseek01.R;

/**
 * チュートリアルフラグメントクラス
 */
public class BaseTutorial extends Fragment {

    private String tutorialCode;

    private TutorialCover tapCover;
    private ImageView tachieImage;
    private TextView fukidashiText;

    private ArrayList<TutorialParam> tutorialData;
    private int step = 0;

    private boolean characterSideLeft = true;

    private ImageView pointerImage;
    private ImageView pinchImage;

    public BaseTutorial(String tutorialCode) {
        this.tutorialCode = tutorialCode;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tutorial, container, false);

        tapCover = (TutorialCover) view.findViewById(R.id.tap_cover);
        tachieImage = (ImageView) view.findViewById(R.id.tachie_image);
        fukidashiText = (TextView) view.findViewById(R.id.fukidashi_text);

        tapCover.setTutoriaCoverlListener(coverlListener);

        pointerImage = (ImageView) view.findViewById(R.id.pointer);
        pinchImage = (ImageView) view.findViewById(R.id.pinch);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tutorialData = CsvManager.tutorialData(tutorialCode);
    }

    private boolean resumed = false;

    @Override
    public void onResume() {
        super.onResume();

        if (!resumed) {
            resumed = true;
            stepNext();
        }
    }

    //------

    public interface TutoriaSteplListener extends EventListener {
        void didEndTutorial();

        void pushedTarget(TutorialParam param);
    }

    protected TutoriaSteplListener tutorialSteplListener = null;

    public void setTutoriaSteplListener(TutoriaSteplListener l) {
        tutorialSteplListener = l;
    }
    //-- event

    private TutorialCover.TutoriaCoverlListener coverlListener = new TutorialCover.TutoriaCoverlListener() {
        @Override
        public void onClickStepNext() {

            if (hasSerifu) {
                SeManager.play(SeManager.SeName.PUSH_CONTENT);
            }

            stepNext();
        }

        @Override
        public void onClickTarget() {
            if (hasSerifu) {
                if (!detectShowFullSefifu()) return;
            }
            TutorialParam param = tutorialData.get(step - 1);
            if (tutorialSteplListener != null) {
                param.extra_tutorialCode = tutorialCode;
                tutorialSteplListener.pushedTarget(param);
            }
            stepNext();
        }
    };

    //-- update

    //次へ
    protected void stepNext() {

        if (step >= tutorialData.size()) {
            // 終了
            SaveManager.updateStringArray(SaveManager.KEY.TUTORIAL__stringList, tutorialCode, true);

            if (tutorialSteplListener != null)
                tutorialSteplListener.didEndTutorial();

            ((BaseFragment) getParentFragment()).stopTutorial(BaseTutorial.this);
            return;
        }

        final TutorialParam param = tutorialData.get(step);

        if (!canTapNext) return;

        if (hasSerifu) {
            if (!detectShowFullSefifu()) return;
        }

        canTapNext = false;

        if (param.wait > 0) {

            tapCover.setState(TutorialCover.TutorialState.NOTOUCH);

            if (getView() != null) {
                getView().setAlpha(0f);
            }

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    stepFire(param, true);
                }
            }, param.wait * 1000);

        } else {
            stepFire(param, false);
        }
    }

    @SuppressWarnings("deprecation")
    public Drawable getDrawableResource(int id){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            return getResources().getDrawable(id, getContext().getTheme());
        }
        else{
            return getResources().getDrawable(id);
        }
    }

    private void stepFire(TutorialParam param, boolean alphaBack) {

        canTapNext = true;

        if (alphaBack) {
            if (getView() != null) {
                getView().setAlpha(1.0f);
            }
        }

        //自由操作
        if (param.characterName != null && param.characterName.equals("自由操作")) {
            tapCover.setState(TutorialCover.TutorialState.FREE);
            pointerImage.setImageDrawable(null);
            pinchImage.setImageDrawable(null);
            pointerImage.setAnimation(null);
            pinchImage.setAnimation(null);
            pointerImage.setVisibility(View.GONE);
            pinchImage.setVisibility(View.GONE);
            fukidashiText.setVisibility(View.GONE);
            tachieImage.setVisibility(View.GONE);
            step++;

            return;
        }

        //特殊操作
        if (param.sousaCode != null && param.sousaCode.equals("アイテム名とアイテム以外マスク")) {
        }
        //pinch
        if ((param.sousaCode != null && param.sousaCode.equals("ピンチアウト"))
                || (param.sousaCode != null && param.sousaCode.equals("ピンチイン"))) {
            tapCover.setState(TutorialCover.TutorialState.FREE);
            pointerImage.setImageDrawable(null);
            pointerImage.setAnimation(null);
            pointerImage.setVisibility(View.GONE);
            fukidashiText.setVisibility(View.GONE);
            tachieImage.setVisibility(View.GONE);
            step++;

            pinchImage.setVisibility(View.VISIBLE);

            AnimationDrawable anim = new AnimationDrawable();


            if (param.sousaCode != null && param.sousaCode.equals("ピンチアウト")) {
                anim.addFrame(getDrawableResource(R.mipmap.pinch_out2), 500);
                anim.addFrame(getDrawableResource(R.mipmap.pinch_out), 500);
            } else {
                anim.addFrame(getDrawableResource(R.mipmap.pinch_in), 500);
                anim.addFrame(getDrawableResource(R.mipmap.pinch_in2), 500);
            }

            anim.setOneShot(false);

            pinchImage.setImageDrawable(anim);

            // アニメーション開始
            anim.start();

            tutorialSteplListener.pushedTarget(param);

            return;
        }


        pinchImage.setImageDrawable(null);
        pinchImage.setAnimation(null);
        pinchImage.setVisibility(View.GONE);

        //キャラ絵
        if (param.characterIdentifier > 0) {

            //bitmap
            Bitmap bitmap = Common.decodedBitmap(
                    CsvManager.bitmapImagePath("chara", String.valueOf(param.characterIdentifier), String.valueOf(param.characterHyoujou), "png"),
                    500, 300
            );
            tachieImage.setImageBitmap(bitmap);

            tachieImage.setVisibility(View.VISIBLE);

            //side
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) tachieImage.getLayoutParams();
            if (param.characterPos == 0) {
                //left
                layoutParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
                characterSideLeft = true;
            } else {
                //right
                layoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                characterSideLeft = false;
            }
            tachieImage.setLayoutParams(layoutParams);
        }

        //吹き出し
        if (param.serifuText.length() == 0) {

            hasSerifu = false;
            fukidashiText.setVisibility(View.GONE);
            tachieImage.setVisibility(View.GONE);

        } else {

            hasSerifu = true;

            fukidashiText.setVisibility(View.VISIBLE);

            updateSerifu(param.serifuText);

            //背景
            int resourceId = 0;
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) fukidashiText.getLayoutParams();

            if (param.serifuPos == 1) {
                if (!characterSideLeft) {
                    resourceId = R.mipmap.fukidashi_up_left;
                    layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
                } else {
                    resourceId = R.mipmap.fukidashi_up_right;
                    layoutParams.gravity = Gravity.RIGHT | Gravity.TOP;
                }
            } else if (param.serifuPos == 2) {
                if (!characterSideLeft) {
                    resourceId = R.mipmap.fukidashi_bottom_left;
                    layoutParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
                } else {
                    resourceId = R.mipmap.fukidashi_bottom_right;
                    layoutParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
                }
            }

            fukidashiText.setLayoutParams(layoutParams);

            if (resourceId > 0)
                fukidashiText.setBackgroundResource(resourceId);
        }


        tapCover.clearCanvas();

        //操作
        if (param.sousaCode.length() > 0 || param.effectCode.length() > 0) {


            FrameLayout.LayoutParams pointerLayout = (FrameLayout.LayoutParams) pointerImage.getLayoutParams();

            if (tutorialCode.indexOf("jiken") != -1) {
                //top-jiken
                if (tutorialCode.indexOf("jiken_1") != -1) {
                    if (param.sousaCode.indexOf("事件ボタン") != -1) {
                        tapCover.addRect(0, 160, 90, 230);
                        pointerLayout.leftMargin = tapCover.dp2Px(45 - 60);
                        pointerLayout.topMargin = tapCover.dp2Px(210 - 60);
                        tapCover.setState(TutorialCover.TutorialState.HOLE_CLICK);
                    }
                } else if (tutorialCode.indexOf("jiken_2") != -1) {
                    if (param.sousaCode.indexOf("マップ事件1ボタン") != -1) {
                        tapCover.addRect(110, 250, 154, 285);
                        pointerLayout.leftMargin = tapCover.dp2Px(70);
                        pointerLayout.topMargin = tapCover.dp2Px(230);
                        tapCover.setState(TutorialCover.TutorialState.HOLE_CLICK);
                    } else if (param.sousaCode.indexOf("ストーリーボタン") != -1) {
                        tapCover.addRect(130, 270, 235, 330);
                        pointerLayout.leftMargin = tapCover.dp2Px(140);
                        pointerLayout.topMargin = tapCover.dp2Px(260);
                        tapCover.setState(TutorialCover.TutorialState.HOLE_CLICK);
                    } else if (param.sousaCode.indexOf("捜査ボタン") != -1) {
                        tapCover.addRect(270, 270, 375, 330);
                        pointerLayout.leftMargin = tapCover.dp2Px(280);
                        pointerLayout.topMargin = tapCover.dp2Px(260);
                        tapCover.setState(TutorialCover.TutorialState.HOLE_CLICK);
                    } else if (param.sousaCode.indexOf("デッキ編集ボタン") != -1) {
                        tapCover.addRect(360, 270, 465, 330);
                        pointerLayout.leftMargin = tapCover.dp2Px(370);
                        pointerLayout.topMargin = tapCover.dp2Px(260);
                        tapCover.setState(TutorialCover.TutorialState.HOLE_CLICK);
                    } else if (param.effectCode.indexOf("捜査条件以外マスク") != -1) {
                        tapCover.addRect(94, 150, 220, 280);
                        tapCover.setState(TutorialCover.TutorialState.KAIWA_STEP);
                    } else if (param.effectCode.indexOf("デッキ以外マスク") != -1) {
                        tapCover.addRect(225, 90, 550, 260);
                        tapCover.setState(TutorialCover.TutorialState.KAIWA_STEP);
                    }
                } else if (tutorialCode.indexOf("jiken_3") != -1) {
                    if (param.sousaCode.indexOf("プラスボタン") != -1) {
                        tapCover.addRect(177, 164, 305, 319);
                        pointerLayout.leftMargin = tapCover.dp2Px(183 + 30);
                        pointerLayout.topMargin = tapCover.dp2Px(164 + 30);
                        tapCover.setState(TutorialCover.TutorialState.HOLE_CLICK);
                    } else if (param.sousaCode.indexOf("所持カード") != -1) {
                        tapCover.addRect(102, 160, 176, 252);
                        pointerLayout.leftMargin = tapCover.dp2Px(104);
                        pointerLayout.topMargin = tapCover.dp2Px(170 + 30);
                        tapCover.setState(TutorialCover.TutorialState.HOLE_CLICK);
                    } else if (param.sousaCode.indexOf("戻るボタン") != -1) {
                        tapCover.addRect(90, 51, 90 + 36, 51 + 36);
                        pointerLayout.leftMargin = tapCover.dp2Px(90 - 30);
                        pointerLayout.topMargin = tapCover.dp2Px(51 - 30);
                        tapCover.setState(TutorialCover.TutorialState.HOLE_CLICK);
                    } else if (param.effectCode.indexOf("一番左端") != -1) {
                        tapCover.addRect(189, 286, 214, 306);
                        tapCover.setState(TutorialCover.TutorialState.KAIWA_STEP);
                    } else if (param.effectCode.indexOf("スキル名と簡単") != -1) {
                        tapCover.addRect(187, 305, 298, 350);
                        tapCover.setState(TutorialCover.TutorialState.KAIWA_STEP);
                    }
                } else if (tutorialCode.indexOf("jiken_4") != -1) {
                    if (param.sousaCode.indexOf("スタートボタン") != -1) {
                        tapCover.addRect(172, 283, 275, 323);
                        pointerLayout.leftMargin = tapCover.dp2Px(172 + 30);
                        pointerLayout.topMargin = tapCover.dp2Px(275 - 30);
                        tapCover.setState(TutorialCover.TutorialState.HOLE_CLICK);
                    } else if (param.sousaCode.indexOf("はいボタン") != -1) {
                        tapCover.addRect(342, 244, 450, 285);
                        pointerLayout.leftMargin = tapCover.dp2Px(347 + 10);
                        pointerLayout.topMargin = tapCover.dp2Px(250 - 30);
                        tapCover.setState(TutorialCover.TutorialState.HOLE_CLICK);
                    }
                } else if (tutorialCode.indexOf("jiken_5") != -1) {
                    if (param.effectCode.indexOf("アイテム名部分以外マスク") != -1) {
                        tapCover.addRect(232, 0, 630, 41);
                        tapCover.setState(TutorialCover.TutorialState.KAIWA_STEP);
                    } else if (param.effectCode.indexOf("残り時間部分以外マスク") != -1) {
                        tapCover.addRect(18, 0, 234, 37);
                        tapCover.setState(TutorialCover.TutorialState.KAIWA_STEP);
                    } else if (param.effectCode.indexOf("一時中断ボタン以外マスク") != -1) {
                        tapCover.addRect(18, 0, 52, 37);
                        tapCover.setState(TutorialCover.TutorialState.KAIWA_STEP);
                    } else if (param.effectCode.indexOf("カード部分以外マスク") != -1) {
                        tapCover.addRect(3, 45, 79, 340);
                        tapCover.setState(TutorialCover.TutorialState.KAIWA_STEP);
                    }
                } else if (tutorialCode.indexOf("jiken_6") != -1) {
                    if (param.sousaCode.indexOf("物証") != -1) {
                        tapCover.addRect(292, 328, 350, 353);
                        tapCover.addRect(423, 0, 526, 18);
                        pointerLayout.leftMargin = tapCover.dp2Px(292);
                        pointerLayout.topMargin = tapCover.dp2Px(328);
                        tapCover.setState(TutorialCover.TutorialState.HOLE_CLICK);
                    } else if (param.effectCode.indexOf("残り時間部分以外マスク") != -1) {
                        tapCover.addRect(18, 0, 234, 37);
                        tapCover.setState(TutorialCover.TutorialState.KAIWA_STEP);
                    } else if (param.effectCode.indexOf("一時中断ボタン以外マスク") != -1) {
                        tapCover.addRect(18, 0, 52, 37);
                        tapCover.setState(TutorialCover.TutorialState.KAIWA_STEP);
                    } else if (param.effectCode.indexOf("カード部分以外マスク") != -1) {
                        tapCover.addRect(3, 45, 79, 340);
                        tapCover.setState(TutorialCover.TutorialState.KAIWA_STEP);
                    }
                } else if (tutorialCode.indexOf("jiken_8") != -1) {
                    if (param.sousaCode.indexOf("推理ボタン") != -1) {
                        tapCover.addRect(408, 270, 504, 330);
                        pointerLayout.leftMargin = tapCover.dp2Px(414 + 10);
                        pointerLayout.topMargin = tapCover.dp2Px(260);
                        tapCover.setState(TutorialCover.TutorialState.HOLE_CLICK);
                    } else if (param.sousaCode.indexOf("注射器") != -1) {
                        tapCover.addRect(387, 140, 494, 250);
                        pointerLayout.leftMargin = tapCover.dp2Px(387 + 20);
                        pointerLayout.topMargin = tapCover.dp2Px(140 + 20);
                        tapCover.setState(TutorialCover.TutorialState.HOLE_CLICK);
                    } else if (param.sousaCode.indexOf("次へ") != -1) {
                        tapCover.addRect(338, 280, 451, 323);
                        pointerLayout.leftMargin = tapCover.dp2Px(338);
                        pointerLayout.topMargin = tapCover.dp2Px(280 - 15);
                        tapCover.setState(TutorialCover.TutorialState.HOLE_CLICK);
                    } else if (param.sousaCode.indexOf("はい") != -1) {
                        tapCover.addRect(338, 244, 451, 287);
                        pointerLayout.leftMargin = tapCover.dp2Px(338);
                        pointerLayout.topMargin = tapCover.dp2Px(244 - 10);
                        tapCover.setState(TutorialCover.TutorialState.HOLE_CLICK);
                    } else if (param.sousaCode.indexOf("エンディング") != -1) {
                        tapCover.addRect(311, 240, 434, 288);
                        pointerLayout.leftMargin = tapCover.dp2Px(311 + 10);
                        pointerLayout.topMargin = tapCover.dp2Px(240 - 30);
                        tapCover.setState(TutorialCover.TutorialState.HOLE_CLICK);
                    } else if (param.sousaCode.indexOf("ガチャメニュー") != -1) {
                        tapCover.addRect(6, 225, 73, 286);
                        pointerLayout.leftMargin = tapCover.dp2Px(6 + 10);
                        pointerLayout.topMargin = tapCover.dp2Px(225 + 10);
                        tapCover.setState(TutorialCover.TutorialState.HOLE_CLICK);
                    } else if (param.effectCode.indexOf("物証部分") != -1) {
                        tapCover.addRect(106, 79, 548, 283);
                        tapCover.setState(TutorialCover.TutorialState.KAIWA_STEP);
                    } else if (param.effectCode.indexOf("依頼報酬以外") != -1) {
                        tapCover.addRect(220, 201, 512, 250);
                        tapCover.setState(TutorialCover.TutorialState.KAIWA_STEP);
                    } else if (param.effectCode.indexOf("事件1以外") != -1) {
                        tapCover.addRect(110, 250, 154, 285);
                        tapCover.setState(TutorialCover.TutorialState.KAIWA_STEP);
                    } else if (param.effectCode.indexOf("事件2以外") != -1) {
                        tapCover.addRect(172, 222, 218, 257);
                        tapCover.setState(TutorialCover.TutorialState.KAIWA_STEP);
                    } else if (param.effectCode.indexOf("？ボタン以外") != -1) {
                        tapCover.addRect(91, 323, 155, 353);
                        tapCover.setState(TutorialCover.TutorialState.KAIWA_STEP);
                    }
                }
            }

            //shop
            else if (tutorialCode.indexOf("shop") != -1) {
                if (param.effectCode.indexOf("プラスボタン以外") != -1) {
                    tapCover.addRect(150, 4, 150 + 26, 30);
                    tapCover.addRect(394, 4, 394 + 26, 30);
                    tapCover.addRect(587, 4, 587 + 26, 30);
                    tapCover.setState(TutorialCover.TutorialState.KAIWA_STEP);
                }
            }

            //gacha
            else if (tutorialCode.indexOf("gacha") != -1) {
                if (param.sousaCode.indexOf("ガチャボタン") != -1) {
                    tapCover.addRect(260, 290, 360, 330);
                    pointerLayout.leftMargin = tapCover.dp2Px(280);
                    pointerLayout.topMargin = tapCover.dp2Px(340);
                    tapCover.setState(TutorialCover.TutorialState.HOLE_CLICK);
                } else if (param.sousaCode.indexOf("TOPボタン") != -1) {
                    tapCover.addRect(566, 35, 640, 80);
                    pointerLayout.leftMargin = tapCover.dp2Px(540);
                    pointerLayout.topMargin = tapCover.dp2Px(20);
                    tapCover.setState(TutorialCover.TutorialState.HOLE_CLICK);
                } else if (param.sousaCode.indexOf("戻るボタン") != -1) {
                    tapCover.addRect(90, 51, 90 + 36, 51 + 36);
                    pointerLayout.leftMargin = tapCover.dp2Px(90 - 30);
                    pointerLayout.topMargin = tapCover.dp2Px(51 - 30);
                    tapCover.setState(TutorialCover.TutorialState.HOLE_CLICK);
                } else if (param.effectCode.indexOf("出現率以外マスク") != -1) {
                    tapCover.addRect(528, 294, 616, 334);
                    tapCover.setState(TutorialCover.TutorialState.KAIWA_STEP);
                }
            }

            //card
            else if (tutorialCode.indexOf("card") != -1) {
                if (param.sousaCode.indexOf("デッキ") != -1) {
                    tapCover.addRect(354, 108, 610, 100 + 86);
                    pointerLayout.leftMargin = tapCover.dp2Px(540 - 60);
                    pointerLayout.topMargin = tapCover.dp2Px(160 - 60);
                    tapCover.setState(TutorialCover.TutorialState.HOLE_CLICK);
                } else if (param.sousaCode.indexOf("所持") != -1) {
                    tapCover.addRect(354, 190, 610, 190 + 86);
                    pointerLayout.leftMargin = tapCover.dp2Px(540 - 60);
                    pointerLayout.topMargin = tapCover.dp2Px(240 - 60);
                    tapCover.setState(TutorialCover.TutorialState.HOLE_CLICK);
                } else if (param.sousaCode.indexOf("図鑑") != -1) {
                    tapCover.addRect(354, 265, 610, 265 + 86);
                    pointerLayout.leftMargin = tapCover.dp2Px(540 - 60);
                    pointerLayout.topMargin = tapCover.dp2Px(340 - 60);
                    tapCover.setState(TutorialCover.TutorialState.HOLE_CLICK);
                } else if (param.sousaCode.indexOf("戻るボタン") != -1) {
                    tapCover.addRect(90, 51, 90 + 36, 51 + 36);
                    pointerLayout.leftMargin = tapCover.dp2Px(90 - 30);
                    pointerLayout.topMargin = tapCover.dp2Px(51 - 30);
                    tapCover.setState(TutorialCover.TutorialState.HOLE_CLICK);
                } else if (param.effectCode.indexOf("フィルタ以外") != -1) {
                    tapCover.addRect(530, 128, 620, 158);
                    tapCover.setState(TutorialCover.TutorialState.KAIWA_STEP);
                }
            }

            //kunren
            else if (tutorialCode.indexOf("kunren") != -1) {
                if (param.sousaCode.indexOf("研究所") != -1) {
                    tapCover.addRect(160, 105, 560, 185);
                    pointerLayout.leftMargin = tapCover.dp2Px(440 - 60);
                    pointerLayout.topMargin = tapCover.dp2Px(150 - 60);
                    tapCover.setState(TutorialCover.TutorialState.HOLE_CLICK);
                } else if (param.sousaCode.indexOf("戻るボタン") != -1) {
                    tapCover.addRect(80, 46, 80 + 36, 46 + 36);
                    pointerLayout.leftMargin = tapCover.dp2Px(96 - 60);
                    pointerLayout.topMargin = tapCover.dp2Px(70 - 60);
                    tapCover.setState(TutorialCover.TutorialState.HOLE_CLICK);
                } else if (param.sousaCode.indexOf("ミッションボタン") != -1) {
                    tapCover.addRect(536, 310, 640, 360);
                    pointerLayout.leftMargin = tapCover.dp2Px(580 - 60);
                    pointerLayout.topMargin = tapCover.dp2Px(345 - 60);
                    tapCover.setState(TutorialCover.TutorialState.HOLE_CLICK);
                } else if (param.effectCode.indexOf("レベル選択の") != -1) {
                    tapCover.addRect(75, 80, 570, 185);
                    tapCover.setState(TutorialCover.TutorialState.KAIWA_STEP);
                } else if (param.effectCode.indexOf("ハート消費個数") != -1) {
                    tapCover.addRect(334, 96, 384, 126);
                    tapCover.setState(TutorialCover.TutorialState.KAIWA_STEP);
                } else if (param.effectCode.indexOf("訓練報酬以外") != -1) {
                    tapCover.addRect(486, 90, 540, 160);
                    tapCover.setState(TutorialCover.TutorialState.KAIWA_STEP);
                } else if (param.effectCode.indexOf("ミッションポップアップ") != -1) {
                    tapCover.addRect(40, 14, 640 - 40, 360 - 14);
                    tapCover.setState(TutorialCover.TutorialState.KAIWA_STEP);
                }
            }

            pointerImage.setLayoutParams(pointerLayout);


        } else {
            tapCover.setState(TutorialCover.TutorialState.KAIWA_STEP);
        }


        //指
        if (param.pointer == 0) {
            pointerImage.setImageDrawable(null);
            pointerImage.setVisibility(View.GONE);
            pointerImage.setAnimation(null);
        } else {
            pointerImage.setVisibility(View.VISIBLE);
            if (param.pointer == 1)
                pointerImage.setImageResource(R.mipmap.pointer_up);
            else
                pointerImage.setImageResource(R.mipmap.pointer_bottom);
            pointerImage.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.pointer_zoom));
        }


        tapCover.invalidate();

        step++;
    }


    //------------------ 文字タイピング

    private boolean hasSerifu;
    private boolean canTapNext = true;
    private String serifuFullString;
    private boolean typingDone;
    private int serifuIndex = 0;
    private String putSerifuString;

    private void updateSerifu(String serifu) {
        serifuFullString = serifu;
        serifuIndex = 0;
        typingDone = false;
        putSerifuString = "";

        typingHandler.sendEmptyMessage(1);
    }

    private boolean detectShowFullSefifu() {

        if (typingDone) return true;

        fukidashiText.setText(serifuFullString);
        serifuIndex = serifuFullString.length();
        typingDone = true;

        canTapNext = true;

        return false;
    }

    private Handler typingHandler = new Handler() {


        @Override
        public void dispatchMessage(Message message) {

            if (canTapNext) {


                char data[] = serifuFullString.toCharArray();

                int arr_num = data.length;

                if (serifuIndex < arr_num) {
                    if (message.what == 1) {
                        String word = String.valueOf(data[serifuIndex]);
                        putSerifuString += word;

                        fukidashiText.setText(putSerifuString);
                        typingHandler.sendEmptyMessageDelayed(1, 10);
                        serifuIndex++;
                    } else {
                        super.dispatchMessage(message);
                    }
                } else {
                    typingDone = true;
                }
            } else {
                typingHandler.sendEmptyMessageDelayed(1, 10);
            }
        }
    };

}

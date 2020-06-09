package jp.co.cybird.android.conanseek.activity.jiken;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;

import jp.co.cybird.android.conanseek.activity.card.CardDeckFragment;
import jp.co.cybird.android.conanseek.activity.card.CardFragment;
import jp.co.cybird.android.conanseek.activity.gacha.GachaFragment;
import jp.co.cybird.android.conanseek.activity.sagashi.KakuninPopup;
import jp.co.cybird.android.conanseek.activity.sagashi.SagashiFragment;
import jp.co.cybird.android.conanseek.common.BaseActivity;
import jp.co.cybird.android.conanseek.common.BaseButton;
import jp.co.cybird.android.conanseek.common.BaseCell;
import jp.co.cybird.android.conanseek.common.BaseFragment;
import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.common.MessagePopup;
import jp.co.cybird.android.conanseek.common.UntouchableViewPager;
import jp.co.cybird.android.conanseek.common.WebViewPopup;
import jp.co.cybird.android.conanseek.manager.APIDialogFragment;
import jp.co.cybird.android.conanseek.manager.APIRequest;
import jp.co.cybird.android.conanseek.manager.BgmManager;
import jp.co.cybird.android.conanseek.manager.CacheManager;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.CsvManager;
import jp.co.cybird.android.conanseek.manager.SaveManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.manager.UserInfoManager;
import jp.co.cybird.android.conanseek.param.JikenParam;
import jp.co.cybird.android.conanseek.param.NanidoParam;
import jp.co.cybird.android.conanseek.param.TutorialParam;
import jp.souling.android.conanseek01.R;
import jp.souling.android.conanseek01.Settings;

public class JikenFragment extends BaseFragment implements View.OnClickListener {

    private UntouchableViewPager viewPager;
    private int totalPages;
    private int currentPage;
    private boolean noSoundSwipe = false;

    private BaseButton leftArrow;
    private BaseButton rightArrow;

    private static final int SWIPE_MIN_DISTANCE = 50;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private static final int SWIPE_MAX_OFF_PATH = 250;

    private ArrayList<JikenParam> jikenMaster;

    //初期状態で表示しておく事件情報
    private JikenParam firstShownJikenParam;


    private GestureDetector gesture;

    public static JikenFragment newInstance(JikenParam jikenParam) {

        Bundle args = new Bundle();
        args.putSerializable("jikenParam", jikenParam);


        JikenFragment fragment = new JikenFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_jiken, container, false);


        Bundle arg = getArguments();
        if (arg != null) {
            this.firstShownJikenParam = (JikenParam) arg.getSerializable("jikenParam");
        }

        bgmName = BgmManager.BgmName.JIKEN;

        leftArrow = (BaseButton) view.findViewById(R.id.blink_arrow_left);
        rightArrow = (BaseButton) view.findViewById(R.id.blink_arrow_right);
        leftArrow.setOnClickListener(this);
        rightArrow.setOnClickListener(this);
        leftArrow.setVisibility(View.GONE);

        //viewpager
        viewPager = (UntouchableViewPager) view.findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(1);
        setMyPageAdapter();

        jikenMaster = CsvManager.jikenMaster();

        view.findViewById(R.id.btn_asobikata).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(WebViewPopup.newInstance(
                        R.mipmap.title_asobikata,
                        getString(R.string.howto_jiken)
                ));
            }
        });


        ArrayList<String> tutorialList = SaveManager.stringArray(SaveManager.KEY.TUTORIAL__stringList);
        if (tutorialList.contains("jiken_1")
                && tutorialList.contains("jiken_2")
                && tutorialList.contains("jiken_3")
                && tutorialList.contains("jiken_4")
                && tutorialList.contains("jiken_5")
                && tutorialList.contains("jiken_6")
                && tutorialList.contains("jiken_sousa")
                && !tutorialList.contains("jiken_8")) {

            //事件1推理表示
            showJikenDetail(CsvManager.jikenMaster().get(0), false);

        } else if (firstShownJikenParam != null) {

            //事件詳細
            showJikenDetail(firstShownJikenParam, false);
            firstShownJikenParam = null;

        }


        //スワイプ移動
        gesture = new GestureDetector(getActivity(),
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }

                    @Override
                    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX,
                                           float velocityY) {
                        try {

                            if (Math.abs(event1.getY() - event2.getY()) > SWIPE_MAX_OFF_PATH) {
                                Common.logD("SWIPE_MAX_OFF_PATH");
                            } else if (event1.getX() - event2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                if (currentPage < totalPages - 1)
                                    viewPager.setCurrentItem(currentPage + 1, true);
                            } else if (event2.getX() - event1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                if (currentPage > 0)
                                    viewPager.setCurrentItem(currentPage - 1, true);
                            }

                        } catch (Exception e) {
                            Common.logD("Exception:" + e.toString());
                        }

                        return false;
                    }
                });

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gesture.onTouchEvent(event);
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        leftArrow.setAnimation(null);
        rightArrow.setAnimation(null);
        leftArrow.setImageBitmap(null);
        leftArrow.setImageDrawable(null);
        rightArrow.setImageBitmap(null);
        rightArrow.setImageDrawable(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewPager != null)
            updateMap();
    }

    @Override
    protected void popupsDidDisappear() {
        super.popupsDidDisappear();

        updateMap();

        //ポップアップ全部消えて次の事件のお知らせがある
        if (CacheManager.instance().jikenNewAlert) {
            CacheManager.instance().jikenNewAlert = false;
            //最終問題クリア後は新しい事件を伝えない
            if (!CacheManager.instance().jikenClearedID.equals("172")) {
                SeManager.play(SeManager.SeName.NEW_JIKEN);
            }
            //それぞれのマップの最終事件クリアで次のマップへスクロール
            if (CacheManager.instance().jikenClearedID.equals("003")) {
                viewPager.setCurrentItem(1);
            } else if (CacheManager.instance().jikenClearedID.equals("157")) {
                viewPager.setCurrentItem(2);
            }
        }
    }

    //-----

    @Override
    protected void fragmentDidAppear() {
        super.fragmentDidAppear();

        ArrayList<String> tutorialList = SaveManager.stringArray(SaveManager.KEY.TUTORIAL__stringList);
        if (tutorialList.contains("jiken_1") && !tutorialList.contains("jiken_2")) {
            startTutorial("jiken_2");
        } else if (tutorialList.contains("jiken_3") && !tutorialList.contains("jiken_4")) {
            startTutorial("jiken_4");
        } else if (tutorialList.contains("jiken_sousa") && !tutorialList.contains("jiken_8")) {
            startTutorial("jiken_8");
        }
    }


    @Override
    public void didEndTutorial() {
        super.didEndTutorial();
    }

    private JikenParam tutorialJikenParam = null;

    @Override
    public void pushedTarget(TutorialParam param) {
        super.pushedTarget(param);

        Common.logD("param.extra_tutorialCode:" + param.extra_tutorialCode);

        if (param.extra_tutorialCode.contains("jiken_2")) {

            if (param.sousaCode.indexOf("マップ事件1ボタン") != -1) {

                SeManager.play(SeManager.SeName.PUSH_BUTTON);

                tutorialJikenParam = jikenMaster.get(0);
                tutorialJikenParam = CsvManager.addJikenDetail(tutorialJikenParam);
                tutorialJikenParam.chouhenFlag = false;

                showJikenDetail(tutorialJikenParam, true);

            } else if (param.sousaCode.indexOf("ストーリーボタン") != -1) {

                SeManager.play(SeManager.SeName.PUSH_BUTTON);

                KaiwaPopup kaiwaPopup = KaiwaPopup.newInstance("csv/jiken/1/openning.csv", "お醤油の穴事件");
                kaiwaPopup.setPopupDisplayListener(new BasePopup.PopupDisplayListener() {
                    @Override
                    public void didShowPopup(BasePopup popup) {

                    }

                    @Override
                    public void didClosePopup(BasePopup popup) {
                        stepNextTutorial();
                        SaveManager.updateJikenShinchoku("001", 1);

                        for (Fragment fragment : getChildFragmentManager().getFragments()) {
                            if (fragment instanceof JikenDetailPopup) {
                                JikenDetailPopup jikenDetailPopup = (JikenDetailPopup) fragment;
                                jikenDetailPopup.updateShinchoku();
                                break;
                            }
                        }
                    }
                });
                showPopup(kaiwaPopup);
            } else if (param.sousaCode.indexOf("捜査ボタン") != -1) {

                SeManager.play(SeManager.SeName.PUSH_BUTTON);
                KakuninPopup kakuninPopup = KakuninPopup.newinstance(1, 1, "お醤油の穴事件", false, null);
                showPopup(kakuninPopup);

                CacheManager.instance().jikenSousaParam = tutorialJikenParam;
            } else if (param.sousaCode.indexOf("デッキ編集ボタン") != -1) {

                SeManager.play(SeManager.SeName.PUSH_BUTTON);

                JikenParam tutorialJikenParam = jikenMaster.get(0);
                tutorialJikenParam = CsvManager.addJikenDetail(tutorialJikenParam);
                tutorialJikenParam.chouhenFlag = false;

                ((BaseActivity) getActivity()).replaceViewController(CardFragment.newInstance(CardDeckFragment.newInstance(), "jiken", tutorialJikenParam));
            }
        } else if (param.extra_tutorialCode.indexOf("jiken_4") != -1) {

            if (param.sousaCode.indexOf("スタートボタン") != -1) {

                SeManager.play(SeManager.SeName.PUSH_BUTTON);

                int currentMegane = UserInfoManager.meganeCount();
                showPopup(MessagePopup.newInstance(
                        "虫眼鏡を1つ使用します。<br>"
                                + "<img src=\"icon_megane\">" + currentMegane + " → <img src=\"icon_megane\">" + (currentMegane - 1) + "<br>"
                                + "よろしいですか？",
                        "いいえ", "はい"
                ));
            } else if (param.sousaCode.indexOf("はいボタン") != -1) {

                SeManager.play(SeManager.SeName.PUSH_BUTTON);

                //難易度パラメーター
                ArrayList<NanidoParam> nanidoList = CsvManager.nanidoList();
                if (nanidoList != null)
                for (NanidoParam jikenNanidoParam : nanidoList) {
                    if (jikenNanidoParam.getArea() == 1 && jikenNanidoParam.getLevel() == 1) {

                        jikenNanidoParam.isKunren = false;

                        ((BaseActivity) getActivity()).replaceViewController(SagashiFragment.newInstance(jikenNanidoParam, tutorialJikenParam));

                        break;
                    }
                }

            }

        } else if (param.extra_tutorialCode.indexOf("jiken_8") != -1) {

            if (param.sousaCode.indexOf("推理ボタン") != -1) {

                SeManager.play(SeManager.SeName.PUSH_BUTTON);

                JikenParam jikenParam = CsvManager.jikenMaster().get(0);
                jikenParam = CsvManager.addJikenDetail(jikenParam);

                SuiriBusshouPopup suiriBusshouPopup = SuiriBusshouPopup.newInstance(
                        jikenParam.hazureItemList,
                        jikenParam.shoukohin,
                        true
                );
                suiriBusshouPopup.setPopupDisplayListener(new BasePopup.PopupDisplayListener() {
                    @Override
                    public void didShowPopup(BasePopup popup) {
                        stepNextTutorial();
                    }

                    @Override
                    public void didClosePopup(BasePopup popup) {

                    }
                });
                showPopup(suiriBusshouPopup);
            } else if (param.sousaCode.indexOf("注射器") != -1) {

                SeManager.play(SeManager.SeName.PUSH_CONTENT);

                for (Fragment fragment : getChildFragmentManager().getFragments()) {
                    if (fragment instanceof SuiriBusshouPopup) {
                        SuiriBusshouPopup suiriBusshouPopup = (SuiriBusshouPopup) fragment;
                        suiriBusshouPopup.tutorialChoice();
                        break;
                    }
                }
            } else if (param.sousaCode.indexOf("次へ") != -1) {

                SeManager.play(SeManager.SeName.PUSH_BUTTON);

                int currentMegane = UserInfoManager.meganeCount();

                MessagePopup messagePopup = MessagePopup.newInstance(
                        "虫眼鏡を1つ使用します。<br>"
                                + "<img src=\"icon_megane\">" + currentMegane + " → <img src=\"icon_megane\">" + (currentMegane - 1) + "<br>"
                                + "よろしいですか？",
                        "いいえ", "はい"
                );
                showPopup(messagePopup);

            } else if (param.sousaCode.indexOf("はい") != -1) {

                SeManager.play(SeManager.SeName.PUSH_BUTTON);

                JikenParam jikenParam = CsvManager.jikenMaster().get(0);
                jikenParam = CsvManager.addJikenDetail(jikenParam);

                //マップ更新
                updateMap();

                //容疑者
                String yougisha = jikenParam.hanninName;

                CacheManager.instance().jikenNewAlert = true;
                CacheManager.instance().jikenClearedID = jikenParam.jikenID;

                //結果
                SuiriResultPopup suiriResultPopup = SuiriResultPopup.newInstance(
                        jikenParam.jikenID,
                        true,
                        yougisha,
                        "注射器",
                        null,
                        "チケット1",
                        true
                );
                showPopup(suiriResultPopup);
            } else if (param.sousaCode.indexOf("エンディング") != -1) {

                SeManager.play(SeManager.SeName.PUSH_BUTTON);

                KaiwaPopup kaiwaPopup = KaiwaPopup.newInstance(
                        "csv/jiken/1/ending.csv",
                        "お醤油の穴事件"
                );
                kaiwaPopup.setPopupButtonListener(new BasePopup.PopupButtonListener() {
                    @Override
                    public void pushedPositiveClick(BasePopup popup) {

                    }

                    @Override
                    public void pushedNegativeClick(BasePopup popup) {

                        for (Fragment fragment : getChildFragmentManager().getFragments()) {
                            if (fragment instanceof BasePopup) {
                                BasePopup p = (BasePopup) fragment;
                                if (p != popup) {
                                    p.hideSound = null;
                                    p.removeMe();
                                }
                            }
                        }

                        popup.removeMe();

                        stepNextTutorial();
                    }
                });
                showPopup(kaiwaPopup);

                SaveManager.updateStringArray(SaveManager.KEY.TUTORIAL__stringList, "jiken_suiri", true);
                updateMap();

            } else if (param.sousaCode.indexOf("ガチャメニューボタン") != -1) {

                SeManager.play(SeManager.SeName.PUSH_BUTTON);

                ((BaseActivity) getActivity()).replaceViewController(GachaFragment.newInstance());
            }
        }

    }


    //-----


    ///ページアダプター

    public void updateMap() {
        setMyPageAdapter();
    }

    private void setMyPageAdapter() {

        //マップ最後の事件が開放済なら次のマップへ進める
        if (SaveManager.boolValue(SaveManager.KEY.DEBUG_JIKEN_ALL_SHOW__boolean, Settings.isDebug)) {
            totalPages = 3;
        } else if (UserInfoManager.jikenCleared("157", true)) {
            totalPages = 3;
        } else if (UserInfoManager.jikenCleared("003", true)) {
            totalPages = 2;
        } else {
            totalPages = 1;
            rightArrow.setVisibility(View.GONE);
        }

        viewPager.setAdapter(new MyPagerAdapter(this.getChildFragmentManager()));
        viewPager.clearOnPageChangeListeners();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {


            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                CacheManager.instance().jikenMapPage = currentPage;

                //矢印表示更新
                if (position == 0) {
                    leftArrow.setAnimation(null);
                    leftArrow.setVisibility(View.GONE);
                } else {
                    leftArrow.setAnimation(AnimationUtils.loadAnimation(Common.myAppContext, R.anim.blink));
                    leftArrow.setVisibility(View.VISIBLE);
                }
                if (position == totalPages - 1) {
                    rightArrow.setAnimation(null);
                    rightArrow.setVisibility(View.GONE);
                } else {
                    rightArrow.setAnimation(AnimationUtils.loadAnimation(Common.myAppContext, R.anim.blink));
                    rightArrow.setVisibility(View.VISIBLE);
                }
                if (!noSoundSwipe)
                    SeManager.play(SeManager.SeName.SWIPE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        currentPage = CacheManager.instance().jikenMapPage;
        noSoundSwipe = true;
        viewPager.setCurrentItem(currentPage);
        noSoundSwipe = false;
    }

    @Override
    public void onClick(View v) {

        if (v.equals(leftArrow)) {
            if (currentPage > 0) {
                viewPager.setCurrentItem(currentPage - 1);
            }
        } else if (v.equals(rightArrow)) {
            if (currentPage < totalPages - 1) {
                viewPager.setCurrentItem(currentPage + 1);
            }
        }
    }


    private class MyPagerAdapter extends FragmentStatePagerAdapter {

        public MyPagerAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            MyPageCell f = new MyPageCell();

            Bundle bundle = new Bundle();
            bundle.putInt("position", position);
            f.setArguments(bundle);

            return f;
        }

        @Override
        public int getCount() {
            return totalPages;
        }

    }

    private class MyPageCell extends BaseCell {

        private ImageView backgroundImage;
        private int pos;
        private View cellView;

        @Override
        public void onDestroy() {
            super.onDestroy();
            backgroundImage.setImageDrawable(null);

            for (int i = 0; i < ((FrameLayout) cellView).getChildCount(); i++ ) {
                View view = ((FrameLayout) cellView).getChildAt(i);
                if (view instanceof BaseButton) {

                    BaseButton button = (BaseButton)view;
                    button.setImageBitmap(null);
                    button.setImageDrawable(null);
                    button.setOnClickListener(null);
                } else if (view instanceof ImageView) {
                    ImageView button = (ImageView)view;
                    button.setImageBitmap(null);
                    button.setImageDrawable(null);
                }

            }
            ((FrameLayout) cellView).removeAllViews();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_jiken_map_cell, container, false);
        }



        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            cellView = view;
            backgroundImage = (ImageView) view.findViewById(R.id.cell_bg);

            Bundle arg = getArguments();
            if (arg != null) {
                pos = arg.getInt("position", 0);
            }

            //背景
            backgroundImage.setImageBitmap(Common.decodedResource(
                    Common.myAppContext.getResources().getIdentifier("map" + (pos + 1), "mipmap", getActivity().getPackageName()),
                    560, 315, 1f
            ));


            ViewTreeObserver observer = backgroundImage.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                boolean flag = false;
                @Override
                public void onGlobalLayout() {
                    if (!flag) {
                        if (backgroundImage != null && backgroundImage.getWidth() > 0) {
                            flag = true;


                            //ボタン群

                            float height = backgroundImage.getHeight();//viewPager.getHeight();// TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 350, Common.myAppContext.getResources().getDisplayMetrics());
                            float ratio = height / 1080.0F;
                            int buttonSize = (int) (100 * ratio);



                            //事件クリア済み
                            ArrayList<String> clearedJikenList = UserInfoManager.responseParam().item.jiken;

                            for (final JikenParam jikenParam : jikenMaster) {
                                if (jikenParam.map_number == pos + 1 && (!jikenParam.chouhenFlag || jikenParam.chouhenFirstFlag)) {

                                    BaseButton button = new BaseButton (Common.myAppContext);
                                    button.setLayoutParams(new ViewGroup.LayoutParams(buttonSize, buttonSize));
                                    button.setX(jikenParam.map_x * ratio - buttonSize / 2);
                                    button.setY(jikenParam.map_y * ratio - buttonSize / 2);

                                    //button.setImageBitmap(Common.decodedResource(R.drawable.btn_map, 30, 30));
                                    button.setImageResource(R.drawable.btn_map);
                                    button.setBackgroundColor(0);

                                    //status
                                    if (SaveManager.boolValue(SaveManager.KEY.DEBUG_JIKEN_ALL_SHOW__boolean, Settings.isDebug)) {
                                        //全表示
                                        button.setSelected(true);

                                    } else if (jikenParam.jikenID.equals("001")) {

                                        ArrayList<String> tutorialList = SaveManager.stringArray(SaveManager.KEY.TUTORIAL__stringList);
                                        if (tutorialList.contains("jiken_sousa")) {
                                            button.setSelected(true);
                                        } else {
                                            button.setSelected(false);
                                            button.setEnabled(true);
                                        }
                                    } else if (jikenParam.jikenID.equals("056")) {
                                        if (clearedJikenList.contains(jikenParam.jikenID)) {
                                            button.setSelected(true);
                                        } else {

                                            ArrayList<String> tutorialList = SaveManager.stringArray(SaveManager.KEY.TUTORIAL__stringList);
                                            if (tutorialList.contains("jiken_sousa")) {
                                                button.setSelected(false);
                                            } else {
                                                button.setSelected(false);
                                                button.setEnabled(false);
                                            }
                                        }

                                    } else if (clearedJikenList.contains(jikenParam.jikenID)) {
                                        //クリア済
                                        button.setSelected(true);
                                    } else {

                                        boolean chouhenCleared = false;

                                        if (jikenParam.chouhenFlag) {

                                            //この長編のラストエピソードをクリアしているか
                                            JikenParam lastEpisodeJikenParam = null;
                                            JikenParam endingEpisodeJikenParam = null;
                                            for (JikenParam tempJikenParam : jikenMaster) {
                                                if (tempJikenParam.chouhenSeriesNumber == jikenParam.chouhenSeriesNumber) {
                                                    lastEpisodeJikenParam = endingEpisodeJikenParam;
                                                    endingEpisodeJikenParam = tempJikenParam;
                                                } else if (lastEpisodeJikenParam != null) {
                                                    break;
                                                }
                                            }
                                            if (lastEpisodeJikenParam != null) {
                                                chouhenCleared = clearedJikenList.contains(lastEpisodeJikenParam.jikenID);
                                            }
                                        }

                                        if (chouhenCleared) {
                                            //クリア済
                                            button.setSelected(true);
                                        } else if (jikenParam.kaihouJoukenJikenID == null) {
                                            //開放条件なし
                                            button.setEnabled(true);
                                        } else if (clearedJikenList.contains(jikenParam.kaihouJoukenJikenID)) {
                                            //開放条件クリア
                                            //カード枚数による条件クリアは押下後に判断
                                            button.setEnabled(true);
                                        } else {
                                            //未開放
                                            button.setEnabled(false);
                                        }
                                    }

                                    if (button.isEnabled()) {

                                        button.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                SeManager.play(SeManager.SeName.PUSH_BUTTON);

                                                //コンテンツダウンロード済み確認
                                                Bitmap bitmap = Common.decodedBitmap(
                                                        CsvManager.bitmapImagePath("zipflag", "", String.valueOf(jikenParam.map_number), "png"),
                                                        1, 1
                                                );

                                                if (bitmap == null) {

                                                    MessagePopup downloadMessage = MessagePopup.newInstance(
                                                            "データのダウンロードを開始します。<br/>よろしいでしょうか？",
                                                            "いいえ", "はい"
                                                    );
                                                    downloadMessage.setPopupButtonListener(new BasePopup.PopupButtonListener() {

                                                        @Override
                                                        public void pushedPositiveClick(BasePopup messagePopup) {

                                                            APIRequest request = new APIRequest();
                                                            request.name = APIDialogFragment.APIName.FILE_DOWNLOAD;
                                                            request.params.put("zip", String.valueOf(jikenParam.map_number));

                                                            APIDialogFragment f = APIDialogFragment.newInstance(request);
                                                            f.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
                                                                @Override
                                                                public void getAPIResult(APIRequest request, Object object) {

                                                                    showJikenDetail(jikenParam, true);

                                                                }
                                                            });
                                                            fireApi(f);
                                                        }

                                                        @Override
                                                        public void pushedNegativeClick(BasePopup messagePopup) {

                                                        }
                                                    });
                                                    showPopup(downloadMessage);


                                                } else {

                                                    showJikenDetail(jikenParam, true);
                                                }
                                            }
                                        });
                                    }


                                    ((FrameLayout) cellView).addView(button);

                                }
                            }


                            // Once data has been obtained, this listener is no longer needed, so remove it...
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                backgroundImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            }
                            else {
                                backgroundImage.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            }

                        }
                    }
                }
            });

        }
    }

    //--------- 事件確認ポップアップ

    private void showJikenDetail(final JikenParam jikenParam, boolean animate) {

        if (jikenParam.chouhenFlag) {

            //長編未アンロック確認
            ArrayList<String> stringArray = SaveManager.stringArray(SaveManager.KEY.JIKEN_UNLOCKED__stringList);
            boolean unlocked = (stringArray.contains(jikenParam.jikenID));

            if (!unlocked && animate) {
                //ロック状態

                LockedPopup lockedPopup = LockedPopup.newInstance(jikenParam.jikenID);
                lockedPopup.setPopupButtonListener(new BasePopup.PopupButtonListener() {
                    @Override
                    public void pushedPositiveClick(BasePopup popup) {

                        //アンロック
                        SaveManager.updateStringArray(SaveManager.KEY.JIKEN_UNLOCKED__stringList, jikenParam.jikenID, true);

                        //事件詳細
                        showPopup(JikenDetailPopup.newInstance(jikenParam));

                        popup.removeMe();
                    }

                    @Override
                    public void pushedNegativeClick(BasePopup popup) {

                    }
                });
                lockedPopup.setPopupDisplayListener(new BasePopup.PopupDisplayListener() {
                    @Override
                    public void didShowPopup(BasePopup popup) {
                        //CacheManager.instance().jikenShownDetailParam = jikenParam;
                    }

                    @Override
                    public void didClosePopup(BasePopup popup) {
                        //CacheManager.instance().jikenShownDetailParam = null;
                    }
                });
                showPopup(lockedPopup);
                return;
            }
        }

        JikenDetailPopup jikenDetailPopup = JikenDetailPopup.newInstance(jikenParam);
        jikenDetailPopup.setPopupDisplayListener(new BasePopup.PopupDisplayListener() {
            @Override
            public void didShowPopup(BasePopup popup) {

                if (CacheManager.instance().jikenSousaParam != null) {
                    ((JikenDetailPopup) popup).pushSousaDialog(jikenParam, false);
                }
            }

            @Override
            public void didClosePopup(BasePopup popup) {
            }
        });
        if (!animate) {
            jikenDetailPopup.noShowAnimation = true;
            jikenDetailPopup.showSound = null;
        }
        showPopup(jikenDetailPopup);
    }

}
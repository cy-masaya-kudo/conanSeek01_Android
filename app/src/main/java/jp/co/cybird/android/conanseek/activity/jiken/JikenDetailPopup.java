package jp.co.cybird.android.conanseek.activity.jiken;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Map;

import jp.co.cybird.android.conanseek.activity.sagashi.KakuninPopup;
import jp.co.cybird.android.conanseek.activity.sagashi.SagashiFragment;
import jp.co.cybird.android.conanseek.activity.shop.MeganePopup;
import jp.co.cybird.android.conanseek.common.BaseActivity;
import jp.co.cybird.android.conanseek.common.BaseButton;
import jp.co.cybird.android.conanseek.common.BaseCell;
import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.common.MessagePopup;
import jp.co.cybird.android.conanseek.common.UntouchableViewPager;
import jp.co.cybird.android.conanseek.common.WebViewPopup;
import jp.co.cybird.android.conanseek.manager.APIDialogFragment;
import jp.co.cybird.android.conanseek.manager.APIRequest;
import jp.co.cybird.android.conanseek.manager.CacheManager;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.CsvManager;
import jp.co.cybird.android.conanseek.manager.SaveManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.manager.UserInfoManager;
import jp.co.cybird.android.conanseek.param.APIResponseParam;
import jp.co.cybird.android.conanseek.param.JikenParam;
import jp.co.cybird.android.conanseek.param.KaiwaParam;
import jp.co.cybird.android.conanseek.param.KunrenParam;
import jp.co.cybird.android.conanseek.param.NanidoParam;
import jp.co.cybird.android.conanseek.param.ShougenParam;
import jp.souling.android.conanseek01.R;

/**
 * 事件詳細
 */
public class JikenDetailPopup extends BasePopup implements View.OnClickListener {

    private UntouchableViewPager viewPager;
    private int totalPages;
    private int currentPage;
    private boolean noSoundSwipe = false;

    private JikenParam motoJikenParam;
    private ArrayList<JikenParam> chouhenEpisodeList;

    private static final int SWIPE_MIN_DISTANCE = 50;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private GestureDetector mGestureDetector;

    private BaseButton leftArrow;
    private BaseButton rightArrow;

    public static JikenDetailPopup newInstance(JikenParam jikenParam) {

        Bundle args = new Bundle();

        args.putSerializable("jikenParam", jikenParam);

        JikenDetailPopup fragment = new JikenDetailPopup();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.popup_jiken_detail, container, false);

        Bundle arg = getArguments();
        if (arg != null) {
            motoJikenParam = (JikenParam) arg.getSerializable("jikenParam");
        }

        leftArrow = (BaseButton) view.findViewById(R.id.blink_arrow_left);
        rightArrow = (BaseButton) view.findViewById(R.id.blink_arrow_right);
        leftArrow.setOnClickListener(this);
        rightArrow.setOnClickListener(this);


        BaseButton asobikataButton = (BaseButton) view.findViewById(R.id.btn_asobikata);
        if (!motoJikenParam.chouhenFlag) {
            asobikataButton.setVisibility(View.GONE);

        } else {

            asobikataButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SeManager.play(SeManager.SeName.PUSH_BUTTON);

                    String howtoURL = Common.myAppContext.getString(R.string.howto_jiken);
                    if (motoJikenParam.chouhenSeriesNumber == 3 || motoJikenParam.chouhenSeriesNumber == 8) {
                        howtoURL = Common.myAppContext.getString(R.string.howto_shougen);
                    } else if (motoJikenParam.chouhenSeriesNumber == 5) {
                        howtoURL = Common.myAppContext.getString(R.string.howto_jiken);
                    } else {
                        howtoURL = Common.myAppContext.getString(R.string.howto_angou);
                    }

                    showPopupFromPopup(WebViewPopup.newInstance(
                            R.mipmap.title_asobikata,
                            howtoURL
                    ));
                }
            });
            mGestureDetector = new GestureDetector(getActivity(), mOnGestureListener);
        }


        if (motoJikenParam.chouhenFlag) {
            //長編全エピソード
            chouhenEpisodeList = new ArrayList<>();
            ArrayList<JikenParam> jikenMasterList = CsvManager.jikenMaster();
            for (JikenParam param : jikenMasterList) {
                if (param.chouhenJikenID != null && param.chouhenJikenID.equals(motoJikenParam.chouhenJikenID)) {
                    chouhenEpisodeList.add(param);
                }
            }
        }


        //ページビュー
        viewPager = (UntouchableViewPager) view.findViewById(R.id.card_view_pager);
        viewPager.setPagingEnabled(true);

        currentPage = 0;
        if (motoJikenParam.chouhenFlag) {
            //長編の場合は前回閉じた場所を初期表示

            Map<String, Integer> map = SaveManager.map(SaveManager.KEY.CHOUHEN_SCROLL_INDEX__map);
            String key = String.valueOf(motoJikenParam.chouhenSeriesNumber);
            if (map.containsKey(key))
                currentPage = map.get(key);
        }
        setMyPageAdapter();


        view.findViewById(R.id.dialog_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeManager.play(SeManager.SeName.PUSH_BACK);
                CacheManager.instance().jikenSousaParam = null;
                removeMe();
            }
        });


        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Common.logD("ondestroy detail");
    }

    @Override
    public void onResume() {
        super.onResume();
        setMyPageAdapter();
    }

    private void updateMyHeaderStatus() {
        ((JikenFragment) getParentFragment()).updateMyHeaderStatus();
    }

    private void updateMap() {
        ((JikenFragment) getParentFragment()).updateMap();
    }

    public void updateShinchoku() {
        setMyPageAdapter();
    }

    private void setMyPageAdapter() {

        if (!motoJikenParam.chouhenFlag) {
            totalPages = 1;
            rightArrow.setVisibility(View.GONE);
            leftArrow.setVisibility(View.GONE);
        } else {
            totalPages = chouhenEpisodeList.size() - 2;
        }

        viewPager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));
        viewPager.clearOnPageChangeListeners();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                currentPage = position;

                SaveManager.updateMapValue(
                        SaveManager.KEY.CHOUHEN_SCROLL_INDEX__map,
                        String.valueOf(motoJikenParam.chouhenSeriesNumber),
                        currentPage
                );

                if (currentPage <= 0) {
                    leftArrow.setVisibility(View.GONE);
                    leftArrow.setAnimation(null);
                } else {
                    leftArrow.setVisibility(View.VISIBLE);
                    leftArrow.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.blink));
                }
                if (currentPage >= totalPages - 1) {
                    rightArrow.setVisibility(View.GONE);
                    rightArrow.setAnimation(null);
                } else {
                    rightArrow.setVisibility(View.VISIBLE);
                    rightArrow.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.blink));
                }

                if (!noSoundSwipe)
                    SeManager.play(SeManager.SeName.SWIPE);


            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        if (currentPage <= 0) {
            leftArrow.setVisibility(View.GONE);
            leftArrow.setAnimation(null);
        } else {
            leftArrow.setVisibility(View.VISIBLE);
            leftArrow.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.blink));
        }
        if (currentPage >= totalPages - 1) {
            rightArrow.setVisibility(View.GONE);
            rightArrow.setAnimation(null);
        } else {
            rightArrow.setVisibility(View.VISIBLE);
            rightArrow.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.blink));
        }

        noSoundSwipe = true;
        viewPager.setCurrentItem(currentPage, false);
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


    /**
     * セル
     */
    private class MyPageCell extends BaseCell {

        private JikenParam jikenParam;
        private boolean winFlag = false;
        private String selectedBusshou = null;

        private int cellPosition;

        private String areaID;

        private ImageView dialogBgImage;
        private ImageView thumbImage;


        BaseButton shougenButton;
        BaseButton angouButton;
        BaseButton busshouButton;
        TextView shougenText;
        TextView angouText;
        TextView busshouText;

        //下ボタン群
        BaseButton storyButton;
        BaseButton sousaButton;
        BaseButton kikikomiButton;
        BaseButton suiriButton;
        ImageView storyFrame;
        ImageView sousaFrame;
        ImageView kikikomiFrame;
        ImageView suiriFrame;
        ImageView suiriArrow;
        ImageView kikikomiArrow;
        FrameLayout kikikomiContainer;
        FrameLayout suiriContainer;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.table_cell_jiken_detail, container, false);

            Bundle arg = getArguments();
            if (arg != null) {
                cellPosition = arg.getInt("position");
            }

            jikenParam = null;

            if (!motoJikenParam.chouhenFlag) {
                jikenParam = motoJikenParam;
            } else {
                jikenParam = chouhenEpisodeList.get(cellPosition + 1);
            }

            //事件マスタに事件の詳細情報付加
            jikenParam = CsvManager.addJikenDetail(jikenParam);

            //立ち絵
            String tachieID = CsvManager.tachieIdFromCharacterName(jikenParam.iraishaName);
            ((ImageView) view.findViewById(R.id.tachie_image)).setImageBitmap(Common.decodedBitmap(
                    CsvManager.bitmapImagePath("chara", tachieID, "1", "png"),
                    300, 240,
                    0.5f
            ));


            //依頼者・出題者
            if (jikenParam.angouChouhenFlag) {
                ((TextView) view.findViewById(R.id.iraisha_text)).setText("出題者 / " + jikenParam.iraishaName);
            } else {
                ((TextView) view.findViewById(R.id.iraisha_text)).setText("依頼者 / " + jikenParam.iraishaName);
            }

            //背景・サムネイル
            areaID = CsvManager.areaIdFromAreaName(jikenParam.sagashiStage);
            String largeImagePath = CsvManager.areaImageFileFromAreaID(Common.parseInt(areaID), false);

            dialogBgImage = (ImageView) view.findViewById(R.id.image_dialog_bg);
            thumbImage = (ImageView) view.findViewById(R.id.thumb_mini);


            dialogBgImage.setImageBitmap(Common.decodedAssetBitmap(largeImagePath, 80, 80));
            if (!jikenParam.chouhenFlag) {
                thumbImage.setImageBitmap(Common.decodedAssetBitmap(largeImagePath, 202, 113, 0.4f));//202, 113
            } else {
                int identifier = Common.myAppContext.getResources().getIdentifier("thumb_" + jikenParam.chouhenSeriesNumber, "mipmap", getActivity().getPackageName());
                thumbImage.setImageBitmap(Common.decodedResource(identifier, 202, 113, 0.4f));//202, 113
            }

            //タイトル
            ((TextView) view.findViewById(R.id.dialog_title_text)).setText(jikenParam.jikenName);

            //右ボタン群
            shougenButton = (BaseButton) view.findViewById(R.id.btn_shougen);
            angouButton = (BaseButton) view.findViewById(R.id.btn_angou);
            busshouButton = (BaseButton) view.findViewById(R.id.btn_busshou);
            shougenText = (TextView) view.findViewById(R.id.text_shougen);
            angouText = (TextView) view.findViewById(R.id.text_angou);
            busshouText = (TextView) view.findViewById(R.id.text_busshou);

            //下ボタン群
            storyButton = (BaseButton) view.findViewById(R.id.btn_story);
            sousaButton = (BaseButton) view.findViewById(R.id.btn_sousa);
            kikikomiButton = (BaseButton) view.findViewById(R.id.btn_kikikomi);
            suiriButton = (BaseButton) view.findViewById(R.id.btn_suiri);
            storyFrame = (ImageView) view.findViewById(R.id.btn_story_frame);
            sousaFrame = (ImageView) view.findViewById(R.id.btn_sousa_frame);
            kikikomiFrame = (ImageView) view.findViewById(R.id.btn_kikikomi_frame);
            suiriFrame = (ImageView) view.findViewById(R.id.btn_suiri_frame);
            suiriArrow = (ImageView) view.findViewById(R.id.jiken_arrow_suiri);
            kikikomiArrow = (ImageView) view.findViewById(R.id.jiken_arrow_kikikomi);
            kikikomiContainer = (FrameLayout) view.findViewById(R.id.btn_kikikomi_container);
            suiriContainer = (FrameLayout) view.findViewById(R.id.btn_suiri_container);

            int shinchoku = SaveManager.jikenShinchoku(jikenParam.jikenID);

            //普通事件
            if (!jikenParam.chouhenFlag) {
                shougenButton.setVisibility(View.GONE);
                shougenText.setVisibility(View.GONE);
                angouText.setVisibility(View.GONE);
                angouButton.setVisibility(View.GONE);
                busshouButton.setVisibility(View.GONE);
                busshouText.setVisibility(View.GONE);

                kikikomiArrow.setVisibility(View.GONE);
                kikikomiContainer.setVisibility(View.GONE);

                storyButton.setEnabled(true);

                if (shinchoku >= 2) {
                    sousaButton.setEnabled(true);
                    suiriButton.setEnabled(true);
                    if (shinchoku == 2) {
                        storyFrame.setVisibility(View.GONE);
                        sousaFrame.setVisibility(View.GONE);
                        suiriFrame.setVisibility(View.VISIBLE);
                        suiriFrame.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.blink));
                    } else {
                        storyFrame.setVisibility(View.GONE);
                        sousaFrame.setVisibility(View.GONE);
                        suiriFrame.setVisibility(View.GONE);
                    }
                } else if (shinchoku >= 1) {
                    sousaButton.setEnabled(true);
                    suiriButton.setEnabled(false);
                    storyFrame.setVisibility(View.GONE);
                    sousaFrame.setVisibility(View.VISIBLE);
                    suiriFrame.setVisibility(View.GONE);
                    sousaFrame.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.blink));
                } else if (shinchoku >= 0) {
                    sousaButton.setEnabled(false);
                    suiriButton.setEnabled(false);
                    storyFrame.setVisibility(View.VISIBLE);
                    sousaFrame.setVisibility(View.GONE);
                    suiriFrame.setVisibility(View.GONE);
                    storyFrame.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.blink));
                }
            }
            //暗号長編
            else if (jikenParam.angouChouhenFlag) {

                shougenButton.setVisibility(View.GONE);
                shougenText.setVisibility(View.GONE);
                //angouText.setVisibility(View.GONE);
                //angouButton.setVisibility(View.GONE);
                busshouButton.setVisibility(View.GONE);
                busshouText.setVisibility(View.GONE);

                kikikomiArrow.setVisibility(View.GONE);
                kikikomiContainer.setVisibility(View.GONE);

                int totalAngouCount = jikenParam.angouString.length();
                ArrayList<Integer> gottenAngouList = SaveManager.gottenAngouIndexList(jikenParam.jikenID);

                int kakureCount = jikenParam.kakushiAngouString == null ? 0 : jikenParam.kakushiAngouString.length();

                angouText.setText(String.valueOf(gottenAngouList.size() + kakureCount) + "/" + String.valueOf(totalAngouCount));

                boolean kaihouFlag = UserInfoManager.jikenCleared(jikenParam.kaihouJoukenJikenID, true);
                Common.logD("kaihouFulag" + kaihouFlag + " jikenParam.kaihouJoukenJikenID:" + jikenParam.kaihouJoukenJikenID);

                //長編事件最初のエピソードは無条件開放
                if (jikenParam.chouhenEpisodeNumber == 1) {
                    kaihouFlag = true;
                }

                if (!kaihouFlag) {
                    storyButton.setEnabled(false);
                    sousaButton.setEnabled(false);
                    suiriButton.setEnabled(false);
                    storyFrame.setVisibility(View.GONE);
                    sousaFrame.setVisibility(View.GONE);
                    suiriFrame.setVisibility(View.GONE);
                } else {
                    storyButton.setEnabled(true);
                    if (shinchoku >= 2) {
                        sousaButton.setEnabled(true);
                        suiriButton.setEnabled(true);
                        if (shinchoku == 2) {
                            storyFrame.setVisibility(View.GONE);
                            sousaFrame.setVisibility(View.GONE);
                            suiriFrame.setVisibility(View.VISIBLE);
                            suiriFrame.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.blink));
                        } else {
                            storyFrame.setVisibility(View.GONE);
                            sousaFrame.setVisibility(View.GONE);
                            suiriFrame.setVisibility(View.GONE);
                        }
                    } else if (shinchoku >= 1) {
                        sousaButton.setEnabled(true);
                        suiriButton.setEnabled(false);
                        storyFrame.setVisibility(View.GONE);
                        sousaFrame.setVisibility(View.VISIBLE);
                        suiriFrame.setVisibility(View.GONE);
                        sousaFrame.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.blink));
                    } else if (shinchoku >= 0) {
                        sousaButton.setEnabled(false);
                        suiriButton.setEnabled(false);
                        storyFrame.setVisibility(View.VISIBLE);
                        sousaFrame.setVisibility(View.GONE);
                        suiriFrame.setVisibility(View.GONE);
                        storyFrame.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.blink));
                    }
                }

            }
            //推理
            else if (jikenParam.suiriChouhenFlag) {

                //shougenButton.setVisibility(View.GONE);
                //shougenText.setVisibility(View.GONE);
                //angouText.setVisibility(View.GONE);
                //angouButton.setVisibility(View.GONE);
                busshouButton.setVisibility(View.GONE);
                busshouText.setVisibility(View.GONE);

                if (cellPosition == totalPages - 1) {
                    suiriArrow.setVisibility(View.VISIBLE);
                    suiriContainer.setVisibility(View.VISIBLE);
                } else {
                    suiriArrow.setVisibility(View.GONE);
                    suiriContainer.setVisibility(View.GONE);
                }

                int totalShougenCount = jikenParam.shougenList.size();
                int currentShougenCount = SaveManager.kikikomizumiShougenIndexList(jikenParam.jikenID).size();
                shougenText.setText(String.valueOf(currentShougenCount) + "/" + String.valueOf(totalShougenCount));
                shougenButton.setEnabled(false);

                angouText.setText("1");

                boolean kaihouFlag = UserInfoManager.jikenCleared(jikenParam.kaihouJoukenJikenID, false);
                //長編事件最初のエピソードは無条件開放
                if (jikenParam.chouhenEpisodeNumber == 1) {
                    kaihouFlag = true;
                }

                if (!kaihouFlag) {
                    storyButton.setEnabled(false);
                    sousaButton.setEnabled(false);
                    kikikomiButton.setEnabled(false);
                    suiriButton.setEnabled(false);
                    storyFrame.setVisibility(View.GONE);
                    sousaFrame.setVisibility(View.GONE);
                    kikikomiFrame.setVisibility(View.GONE);
                    suiriFrame.setVisibility(View.GONE);
                } else {
                    storyButton.setEnabled(true);
                    if (shinchoku >= 2) {

                        //聞き込みフラグ
                        boolean canKikikomi = SaveManager.canKikikomi(jikenParam.jikenID);

                        //そもそも全部聞き込み済み
                        if (canKikikomi) {
                            ArrayList<Integer> indexList = SaveManager.kikikomizumiShougenIndexList(jikenParam.jikenID);
                            canKikikomi = jikenParam.shougenList.size() > indexList.size();
                        }

                        sousaButton.setEnabled(true);
                        suiriButton.setEnabled(true);
                        kikikomiButton.setEnabled(canKikikomi);

                        if (shinchoku == 2) {
                            storyFrame.setVisibility(View.GONE);
                            sousaFrame.setVisibility(View.GONE);
                            kikikomiFrame.setVisibility(View.VISIBLE);
                            kikikomiFrame.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.blink));
                            suiriFrame.setVisibility(View.GONE);
                        } else if (shinchoku == 3) {
                            storyFrame.setVisibility(View.GONE);
                            sousaFrame.setVisibility(View.GONE);
                            kikikomiFrame.setVisibility(View.GONE);
                            suiriFrame.setVisibility(View.VISIBLE);
                            suiriFrame.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.blink));
                        } else {
                            storyFrame.setVisibility(View.GONE);
                            sousaFrame.setVisibility(View.GONE);
                            kikikomiFrame.setVisibility(View.GONE);
                            suiriFrame.setVisibility(View.GONE);
                        }
                    } else if (shinchoku >= 1) {
                        sousaButton.setEnabled(true);
                        kikikomiButton.setEnabled(false);
                        suiriButton.setEnabled(false);
                        storyFrame.setVisibility(View.GONE);
                        sousaFrame.setVisibility(View.VISIBLE);
                        kikikomiFrame.setVisibility(View.GONE);
                        suiriFrame.setVisibility(View.GONE);
                        sousaFrame.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.blink));
                    } else if (shinchoku >= 0) {
                        sousaButton.setEnabled(false);
                        kikikomiButton.setEnabled(false);
                        suiriButton.setEnabled(false);
                        storyFrame.setVisibility(View.VISIBLE);
                        sousaFrame.setVisibility(View.GONE);
                        kikikomiFrame.setVisibility(View.GONE);
                        suiriFrame.setVisibility(View.GONE);
                        storyFrame.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.blink));
                    }
                }

            }
            //証言型
            else if (jikenParam.shougenChouhenFlag) {

                //shougenButton.setVisibility(View.GONE);
                //shougenText.setVisibility(View.GONE);
                angouText.setVisibility(View.GONE);
                angouButton.setVisibility(View.GONE);
                busshouButton.setVisibility(View.GONE);
                busshouText.setVisibility(View.GONE);

                if (cellPosition == totalPages - 1) {
                    suiriArrow.setVisibility(View.VISIBLE);
                    suiriContainer.setVisibility(View.VISIBLE);
                } else {
                    suiriArrow.setVisibility(View.GONE);
                    suiriContainer.setVisibility(View.GONE);
                }

                int totalShougenCount = jikenParam.shougenList.size();
                int currentShougenCount = SaveManager.kikikomizumiShougenIndexList(jikenParam.jikenID).size();
                shougenText.setText(String.valueOf(currentShougenCount) + "/" + String.valueOf(totalShougenCount));
                shougenButton.setEnabled(false);


                boolean kaihouFlag = UserInfoManager.jikenCleared(jikenParam.kaihouJoukenJikenID, false);
                //長編事件最初のエピソードは無条件開放
                if (jikenParam.chouhenEpisodeNumber == 1) {
                    kaihouFlag = true;
                }

                if (!kaihouFlag) {
                    storyButton.setEnabled(false);
                    sousaButton.setEnabled(false);
                    kikikomiButton.setEnabled(false);
                    suiriButton.setEnabled(false);
                    storyFrame.setVisibility(View.GONE);
                    sousaFrame.setVisibility(View.GONE);
                    kikikomiFrame.setVisibility(View.GONE);
                    suiriFrame.setVisibility(View.GONE);
                } else {
                    storyButton.setEnabled(true);
                    if (shinchoku >= 2) {

                        //聞き込み可能か
                        boolean canKikikomi = SaveManager.canKikikomi(jikenParam.jikenID);

                        sousaButton.setEnabled(true);
                        suiriButton.setEnabled(true);
                        kikikomiButton.setEnabled(canKikikomi);

                        if (shinchoku == 2) {
                            storyFrame.setVisibility(View.GONE);
                            sousaFrame.setVisibility(View.GONE);
                            kikikomiFrame.setVisibility(View.VISIBLE);
                            kikikomiFrame.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.blink));
                            suiriFrame.setVisibility(View.GONE);
                        } else if (shinchoku == 3) {
                            storyFrame.setVisibility(View.GONE);
                            sousaFrame.setVisibility(View.GONE);
                            kikikomiFrame.setVisibility(View.GONE);
                            suiriFrame.setVisibility(View.VISIBLE);
                            suiriFrame.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.blink));
                        } else {
                            storyFrame.setVisibility(View.GONE);
                            sousaFrame.setVisibility(View.GONE);
                            kikikomiFrame.setVisibility(View.GONE);
                            suiriFrame.setVisibility(View.GONE);
                        }
                    } else if (shinchoku >= 1) {
                        sousaButton.setEnabled(true);
                        kikikomiButton.setEnabled(false);
                        suiriButton.setEnabled(false);
                        storyFrame.setVisibility(View.GONE);
                        sousaFrame.setVisibility(View.VISIBLE);
                        kikikomiFrame.setVisibility(View.GONE);
                        suiriFrame.setVisibility(View.GONE);
                        sousaFrame.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.blink));
                    } else if (shinchoku >= 0) {
                        sousaButton.setEnabled(false);
                        kikikomiButton.setEnabled(false);
                        suiriButton.setEnabled(false);
                        storyFrame.setVisibility(View.VISIBLE);
                        sousaFrame.setVisibility(View.GONE);
                        kikikomiFrame.setVisibility(View.GONE);
                        suiriFrame.setVisibility(View.GONE);
                        storyFrame.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.blink));
                    }
                }


            }

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    SeManager.play(SeManager.SeName.PUSH_BUTTON);

                    //ストーリー
                    if (v.equals(storyButton)) {

                        //長編
                        if (jikenParam.chouhenFlag) {

                            //初回エピソードのみopeningをまず見せる
                            if (jikenParam.chouhenEpisodeNumber == 1) {

                                KaiwaPopup preKaiwaPopup = KaiwaPopup.newInstance(
                                        "csv/chouhen/" + jikenParam.chouhenSeriesNumber + "/openning.csv",
                                        jikenParam.jikenName
                                );
                                preKaiwaPopup.setPopupButtonListener(new PopupButtonListener() {
                                    @Override
                                    public void pushedPositiveClick(BasePopup popup) {

                                    }

                                    @Override
                                    public void pushedNegativeClick(BasePopup popup) {

                                        KaiwaPopup kaiwa = KaiwaPopup.newInstance(
                                                "csv/chouhen/" + jikenParam.chouhenSeriesNumber + "/" + jikenParam.chouhenEpisodeNumber + "/openning.csv",
                                                jikenParam.jikenName
                                        );
                                        kaiwa.setPopupButtonListener(new PopupButtonListener() {
                                            @Override
                                            public void pushedPositiveClick(BasePopup popup) {

                                            }

                                            @Override
                                            public void pushedNegativeClick(BasePopup popup) {
                                                setMyPageAdapter();
                                                popup.removeMe();
                                            }
                                        });
                                        showPopupFromPopup(kaiwa);
                                        SaveManager.updateJikenShinchoku(jikenParam.jikenID, 1);

                                        popup.removeMe();
                                    }
                                });
                                showPopupFromPopup(preKaiwaPopup);

                            } else {

                                KaiwaPopup kaiwaPopup = KaiwaPopup.newInstance(
                                        "csv/chouhen/" + jikenParam.chouhenSeriesNumber + "/" + jikenParam.chouhenEpisodeNumber + "/openning.csv",
                                        jikenParam.jikenName
                                );
                                kaiwaPopup.setPopupButtonListener(new PopupButtonListener() {
                                    @Override
                                    public void pushedPositiveClick(BasePopup popup) {

                                    }

                                    @Override
                                    public void pushedNegativeClick(BasePopup popup) {
                                        setMyPageAdapter();
                                        popup.removeMe();
                                    }
                                });
                                showPopupFromPopup(kaiwaPopup);
                                SaveManager.updateJikenShinchoku(jikenParam.jikenID, 1);
                            }

                        }
                        //短編
                        else {

                            KaiwaPopup kaiwaPopup = KaiwaPopup.newInstance(
                                    "csv/jiken/" + jikenParam.id + "/openning.csv",
                                    jikenParam.jikenName
                            );
                            kaiwaPopup.setPopupButtonListener(new PopupButtonListener() {
                                @Override
                                public void pushedPositiveClick(BasePopup popup) {

                                }

                                @Override
                                public void pushedNegativeClick(BasePopup popup) {
                                    setMyPageAdapter();
                                    popup.removeMe();
                                }
                            });
                            showPopupFromPopup(kaiwaPopup);
                            SaveManager.updateJikenShinchoku(jikenParam.jikenID, 1);
                        }
                    }
                    //捜査
                    else if (v.equals(sousaButton)) {

                        pushSousaDialog(jikenParam, true);

                    }
                    //聞き込み
                    else if (v.equals(kikikomiButton)) {

                        ArrayList<Integer> kikikomizumiList = SaveManager.kikikomizumiShougenIndexList(jikenParam.jikenID);
                        ArrayList<String> kikikomiAiteList = new ArrayList<>();
                        int shougenIndex = 0;

                        for (ShougenParam shougenParam : jikenParam.shougenList) {
                            if (!kikikomizumiList.contains(shougenIndex)) {
                                if (!kikikomiAiteList.contains(shougenParam.hito)) {
                                    kikikomiAiteList.add(shougenParam.hito);
                                }
                            }
                            shougenIndex++;
                        }

                        Common.logD("kikikomizumiList:" + kikikomizumiList);
                        Common.logD("kikikomiAiteList:" + kikikomiAiteList);

                        KikikomiPopup kikikomiPopup = KikikomiPopup.newInstance(
                                kikikomiAiteList,
                                jikenParam.jikenID
                        );
                        kikikomiPopup.setPopupButtonListener(new PopupButtonListener() {
                            @Override
                            public void pushedPositiveClick(BasePopup popup) {

                                ArrayList<Integer> kikikomizumiList = SaveManager.kikikomizumiShougenIndexList(jikenParam.jikenID);
                                String kikikomiAite = ((KikikomiPopup) popup).selectedItem;

                                //聞き込み証言獲得
                                int shougenIndex = 0;
                                for (ShougenParam shougenParam : jikenParam.shougenList) {

                                    if (!kikikomizumiList.contains(shougenIndex) && shougenParam.hito.equals(kikikomiAite)) {

                                        SaveManager.updateKikikomizumiShougen(jikenParam.jikenID, shougenIndex);
                                        SaveManager.updateCanKikikomi(jikenParam.jikenID, false);
                                        SaveManager.updateJikenShinchoku(jikenParam.jikenID, 3);

                                        //表示更新
                                        JikenDetailPopup.this.setMyPageAdapter();

                                        break;
                                    }

                                    shougenIndex++;

                                }

                                final int shougenIndexFinal = shougenIndex;

                                Common.logD("selected shougen index:"+shougenIndexFinal);

                                //最終問題以外
                                if (cellPosition != totalPages) {

                                    //事件未クリア
                                    if (!UserInfoManager.jikenCleared(jikenParam.jikenID, false)) {

                                        //事件をクリアを通知
                                        //選んだ証言表示
                                        //リワードがあれば表示

                                        APIRequest clearRequest = new APIRequest();
                                        clearRequest.name = APIDialogFragment.APIName.JIKEN_CLEAR;
                                        clearRequest.params.put("jiken_id", jikenParam.jikenID);

                                        APIDialogFragment clearAPI = APIDialogFragment.newInstance(clearRequest);
                                        clearAPI.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
                                            @Override
                                            public void getAPIResult(APIRequest request, final Object object) {

                                                //証言一覧
                                                ShougenListPopup shougenListPopup = ShougenListPopup.newInstance(jikenParam.jikenID, shougenIndexFinal);
                                                shougenListPopup.setPopupDisplayListener(new PopupDisplayListener() {
                                                    @Override
                                                    public void didShowPopup(BasePopup popup) {
                                                    }

                                                    @Override
                                                    public void didClosePopup(BasePopup popup) {

                                                        //マップ更新
                                                        //updateMap();

                                                        //ダイアログ更新
                                                        setMyPageAdapter();

                                                        //報酬
                                                        APIResponseParam resultParam = (APIResponseParam) object;

                                                        //報酬あり
                                                        if (resultParam.item.clear_jiken != null && resultParam.item.clear_jiken.toString().indexOf("0") != 0) {

                                                            APIResponseParam.Item.JikenClear param = new Gson().fromJson(resultParam.item.clear_jiken.toString(), new TypeToken<APIResponseParam.Item.JikenClear>() {
                                                            }.getType());


                                                            String rewardString = param.reward;

                                                            if (rewardString.length() > 2) {

                                                                String message = "";
                                                                String imageHTML = "";

                                                                if (rewardString.indexOf("チケット") == 0) {
                                                                    String amount = rewardString.substring(4);
                                                                    imageHTML = "<img src=\"icon_ticket\">";
                                                                    message = "ガチャチケット x " + amount;
                                                                } else if (rewardString.indexOf("虫眼鏡") == 0) {
                                                                    String amount = rewardString.substring(3);
                                                                    imageHTML = "<img src=\"icon_megane\">";
                                                                    message = "虫眼鏡 x " + amount;
                                                                }

                                                                String myRewardString = imageHTML + message + "<br>" + "をプレゼントボックスに送りました。";

                                                                showPopupFromPopup(MessagePopup.newInstance(myRewardString));

                                                            }
                                                        }

                                                    }
                                                });
                                                showPopupFromPopup(shougenListPopup);


                                            }
                                        });
                                        fireApiFromPopup(clearAPI);
                                    }
                                    //クリア済
                                    else {
                                        //証言表示のみ
                                        showPopupFromPopup(ShougenListPopup.newInstance(jikenParam.jikenID, shougenIndexFinal));
                                    }

                                }
                                //最終問題の証言
                                else {
                                    //証言表示のみ
                                    showPopupFromPopup(ShougenListPopup.newInstance(jikenParam.jikenID, shougenIndexFinal));
                                }

                                popup.removeMe();
                            }

                            @Override
                            public void pushedNegativeClick(BasePopup popup) {

                            }
                        });
                        showPopupFromPopup(kikikomiPopup);

                    }
                    //推理
                    else if (v.equals(suiriButton)) {

                        //推理前会話
                        pushPreSuiriKaiwa(new PopupDisplayListener() {

                            @Override
                            public void didShowPopup(BasePopup popup) {
                            }


                            @Override
                            public void didClosePopup(BasePopup popup) {

                                //暗号:suiri_angou
                                if (jikenParam.angouChouhenFlag) {

                                    pushSuiriBusshou(
                                            jikenParam.fuseikaiBusshouList,
                                            jikenParam.seikaiBusshou,
                                            new APIDialogFragment.APIDialogListener() {
                                                @Override
                                                public void getAPIResult(APIRequest request, Object object) {

                                                    //マップ更新
                                                    //updateMap();

                                                    //ダイアログ更新
                                                    setMyPageAdapter();

                                                    APIResponseParam resultParam = (APIResponseParam) object;

                                                    //結果
                                                    SuiriResultPopup suiriResultPopup = SuiriResultPopup.newInstance(
                                                            jikenParam.jikenID,
                                                            winFlag,
                                                            jikenParam.iraishaName,
                                                            selectedBusshou,
                                                            null,
                                                            resultParam.item.clear_jiken
                                                    );
                                                    suiriResultPopup.setPopupButtonListener(new PopupButtonListener() {
                                                        @Override
                                                        public void pushedPositiveClick(BasePopup myPopup) {

                                                            String filePath;
                                                            if (!winFlag)
                                                                filePath = "csv/chouhen/" + jikenParam.chouhenSeriesNumber + "/" + jikenParam.chouhenEpisodeNumber + "/lose.csv";
                                                            else
                                                                filePath = "csv/chouhen/" + jikenParam.chouhenSeriesNumber + "/" + jikenParam.chouhenEpisodeNumber + "/win.csv";


                                                            KaiwaPopup preKaiwa = KaiwaPopup.newInstance(
                                                                    filePath,
                                                                    jikenParam.jikenName
                                                            );
                                                            if (winFlag && jikenParam.chouhenEpisodeNumber == totalPages)
                                                                preKaiwa.setPopupButtonListener(new PopupButtonListener() {
                                                                    @Override
                                                                    public void pushedPositiveClick(BasePopup popup) {

                                                                    }

                                                                    @Override
                                                                    public void pushedNegativeClick(BasePopup popup) {

                                                                        //エンディング
                                                                        KaiwaPopup endingPopup = KaiwaPopup.newInstance(
                                                                                "csv/chouhen/" + jikenParam.chouhenSeriesNumber + "/ending.csv",
                                                                                jikenParam.jikenName
                                                                        );
                                                                        endingPopup.setPopupButtonListener(new PopupButtonListener() {
                                                                            @Override
                                                                            public void pushedPositiveClick(BasePopup popup) {

                                                                            }

                                                                            @Override
                                                                            public void pushedNegativeClick(BasePopup popup) {

                                                                                JikenDetailPopup.this.hideSound = null;
                                                                                JikenDetailPopup.this.removeMe();

                                                                                popup.removeMe();
                                                                            }
                                                                        });
                                                                        showPopupFromPopup(endingPopup);

                                                                        popup.removeMe();

                                                                    }
                                                                });

                                                            showPopupFromPopup(preKaiwa);

                                                            myPopup.removeMe();
                                                        }

                                                        @Override
                                                        public void pushedNegativeClick(BasePopup myPoup) {

                                                        }
                                                    });
                                                    showPopupFromPopup(suiriResultPopup);

                                                }
                                            }
                                    );


                                }
                                //推理:suiri_suiri
                                else if (jikenParam.suiriChouhenFlag) {


                                    pushSuiriBusshou(
                                            jikenParam.suiriMachigaiItem,
                                            jikenParam.suiriSeikaiItem,
                                            new APIDialogFragment.APIDialogListener() {
                                                @Override
                                                public void getAPIResult(APIRequest request, Object object) {


                                                    //マップ更新
                                                    //updateMap();

                                                    //ダイアログ更新
                                                    setMyPageAdapter();

                                                    APIResponseParam resultParam = (APIResponseParam) object;

                                                    //結果
                                                    SuiriResultPopup suiriResultPopup = SuiriResultPopup.newInstance(
                                                            jikenParam.jikenID,
                                                            winFlag,
                                                            null,
                                                            selectedBusshou,
                                                            null,
                                                            resultParam.item.clear_jiken
                                                    );
                                                    suiriResultPopup.setPopupButtonListener(new PopupButtonListener() {
                                                        @Override
                                                        public void pushedPositiveClick(BasePopup myPopup) {

                                                            String filePath;
                                                            if (!winFlag)
                                                                filePath = "csv/jiken/0/lose_" + jikenParam.chouhenSeriesNumber + ".csv";
                                                            else
                                                                filePath = "csv/chouhen/" + jikenParam.chouhenSeriesNumber + "/ending0.csv";


                                                            KaiwaPopup preKaiwa = KaiwaPopup.newInstance(
                                                                    filePath,
                                                                    jikenParam.jikenName
                                                            );
                                                            if (winFlag)
                                                                preKaiwa.setPopupButtonListener(new PopupButtonListener() {
                                                                    @Override
                                                                    public void pushedPositiveClick(BasePopup popup) {

                                                                    }

                                                                    @Override
                                                                    public void pushedNegativeClick(BasePopup popup) {

                                                                        //エンディング
                                                                        KaiwaPopup endingPopup = KaiwaPopup.newInstance(
                                                                                "csv/chouhen/" + jikenParam.chouhenSeriesNumber + "/ending.csv",
                                                                                jikenParam.jikenName
                                                                        );
                                                                        endingPopup.setPopupButtonListener(new PopupButtonListener() {
                                                                            @Override
                                                                            public void pushedPositiveClick(BasePopup popup) {

                                                                            }

                                                                            @Override
                                                                            public void pushedNegativeClick(BasePopup popup) {

                                                                                JikenDetailPopup.this.hideSound = null;
                                                                                JikenDetailPopup.this.removeMe();

                                                                                popup.removeMe();
                                                                            }
                                                                        });
                                                                        showPopupFromPopup(endingPopup);

                                                                        popup.removeMe();

                                                                    }
                                                                });

                                                            showPopupFromPopup(preKaiwa);

                                                            myPopup.removeMe();
                                                        }

                                                        @Override
                                                        public void pushedNegativeClick(BasePopup myPoup) {

                                                        }
                                                    });
                                                    showPopupFromPopup(suiriResultPopup);

                                                }
                                            }
                                    );
                                }
                                //証言:suiri_shougen
                                else if (jikenParam.shougenChouhenFlag) {

                                    final SuiriYougishaPopup yougishaPopup = SuiriYougishaPopup.newInstance(
                                            jikenParam.hazureYougishaList,
                                            jikenParam.hanninName,
                                            jikenParam.jikenID
                                    );
                                    final SuiriDoukiPopup doukiPopup = SuiriDoukiPopup.newInstance(
                                            jikenParam.hazureDoukiList,
                                            jikenParam.seikaiDouki
                                    );

                                    final SuiriBusshouPopup busshouPopup = SuiriBusshouPopup.newInstance(
                                            jikenParam.suiriMachigaiItem,
                                            jikenParam.suiriSeikaiItem,
                                            false
                                    );

                                    PopupButtonListener suiriListener = new PopupButtonListener() {

                                        private String yougishaName;
                                        private String doukiText;
                                        private String busshouName;

                                        private boolean winFlag = false;

                                        @Override
                                        public void pushedPositiveClick(BasePopup mPopup) {

                                            if (mPopup == yougishaPopup) {
                                                yougishaName = ((SuiriYougishaPopup) mPopup).selectedItem;
                                                showPopupFromPopup(doukiPopup);
                                            } else if (mPopup == doukiPopup) {
                                                doukiText = ((SuiriDoukiPopup) mPopup).selectedDoukiText;
                                                showPopupFromPopup(busshouPopup);
                                            } else if (mPopup == busshouPopup) {
                                                busshouName = ((SuiriBusshouPopup) mPopup).selectedItem;

                                                winFlag = (yougishaName.equals(jikenParam.hanninName)
                                                        && doukiText.equals(jikenParam.seikaiDouki)
                                                        && busshouName.equals(jikenParam.suiriSeikaiItem));

                                                //虫眼鏡消費

                                                if (UserInfoManager.meganeCount() < 1) {
                                                    //メガネたりない

                                                    MessagePopup messagePopup = MessagePopup.newInstance(
                                                            "<img src=\"icon_megane\">虫眼鏡が足りません。<br>"
                                                                    + "<img src=\"icon_megane\">虫眼鏡を購入しますか？",
                                                            "やめる", "購入"
                                                    );
                                                    messagePopup.setPopupButtonListener(new PopupButtonListener() {

                                                        @Override
                                                        public void pushedPositiveClick(BasePopup popup) {
                                                            //メガネ購入
                                                            showPopupFromPopup(MeganePopup.newInstance());
                                                        }

                                                        @Override
                                                        public void pushedNegativeClick(BasePopup popup) {
                                                        }
                                                    });
                                                    showPopupFromPopup(messagePopup);

                                                } else {

                                                    //コンテンツトレード

                                                    SuiriBusshouConfirmPopup confirmPopup = SuiriBusshouConfirmPopup.newInstance(busshouName);
                                                    confirmPopup.setPopupButtonListener(new PopupButtonListener() {
                                                        @Override
                                                        public void pushedPositiveClick(BasePopup cp) {

                                                            APIRequest tradeRequest = new APIRequest();
                                                            tradeRequest.name = APIDialogFragment.APIName.CONTENT_TRADE;
                                                            tradeRequest.params.put("proc_id", Common.myAppContext.getString(R.string.api_proc_trade_jiken));

                                                            APIDialogFragment tradeAPI = APIDialogFragment.newInstance(tradeRequest);
                                                            tradeAPI.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
                                                                @Override
                                                                public void getAPIResult(APIRequest request, Object object) {

                                                                    //トランザクション
                                                                    APIResponseParam param = (APIResponseParam) object;
                                                                    String transaction = new Gson().toJson(param.item.transaction);
                                                                    String procID = (String) request.params.get("proc_id");

                                                                    //トランザクション消費
                                                                    APIRequest transactionRequest = new APIRequest();
                                                                    transactionRequest.name = APIDialogFragment.APIName.TRANSACTION_COMMIT;
                                                                    transactionRequest.transactionString = transaction;
                                                                    transactionRequest.params.put("proc_id", procID);

                                                                    APIDialogFragment transactionAPI = APIDialogFragment.newInstance(transactionRequest);
                                                                    transactionAPI.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
                                                                        @Override
                                                                        public void getAPIResult(APIRequest request, Object object) {

                                                                            yougishaPopup.noShowAnimation = true;
                                                                            yougishaPopup.hideSound = null;
                                                                            yougishaPopup.removeMe();
                                                                            doukiPopup.noShowAnimation = true;
                                                                            doukiPopup.hideSound = null;
                                                                            doukiPopup.removeMe();
                                                                            busshouPopup.noShowAnimation = true;
                                                                            busshouPopup.hideSound = null;
                                                                            busshouPopup.removeMe();

                                                                            APIRequest judgeRequest = new APIRequest();

                                                                            //事件クリア
                                                                            if (winFlag) {
                                                                                judgeRequest.name = APIDialogFragment.APIName.JIKEN_CLEAR;
                                                                                judgeRequest.params.put("jiken_id", jikenParam.jikenID);
                                                                            }
                                                                            //不正解
                                                                            else {
                                                                                judgeRequest.name = APIDialogFragment.APIName.USER_INFO;
                                                                            }

                                                                            APIDialogFragment judgeAPI = APIDialogFragment.newInstance(judgeRequest);
                                                                            judgeAPI.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
                                                                                @Override
                                                                                public void getAPIResult(APIRequest request, Object object) {

                                                                                    //マップ更新
                                                                                    //updateMap();

                                                                                    //ダイアログ更新
                                                                                    setMyPageAdapter();

                                                                                    APIResponseParam resultParam = (APIResponseParam) object;

                                                                                    //結果
                                                                                    SuiriResultPopup suiriResultPopup = SuiriResultPopup.newInstance(
                                                                                            jikenParam.jikenID,
                                                                                            winFlag,
                                                                                            yougishaName,
                                                                                            busshouName,
                                                                                            doukiText,
                                                                                            resultParam.item.clear_jiken
                                                                                    );
                                                                                    suiriResultPopup.setPopupButtonListener(new PopupButtonListener() {
                                                                                        @Override
                                                                                        public void pushedPositiveClick(BasePopup myPopup) {

                                                                                            String filePath;
                                                                                            if (!winFlag)
                                                                                                filePath = "csv/jiken/0/lose_" + jikenParam.chouhenSeriesNumber + ".csv";
                                                                                            else
                                                                                                filePath = "csv/chouhen/" + jikenParam.chouhenSeriesNumber + "/ending0.csv";


                                                                                            KaiwaPopup preKaiwa = KaiwaPopup.newInstance(
                                                                                                    filePath,
                                                                                                    jikenParam.jikenName
                                                                                            );
                                                                                            if (winFlag)
                                                                                                preKaiwa.setPopupButtonListener(new PopupButtonListener() {
                                                                                                    @Override
                                                                                                    public void pushedPositiveClick(BasePopup popup) {

                                                                                                    }

                                                                                                    @Override
                                                                                                    public void pushedNegativeClick(BasePopup popup) {

                                                                                                        //エンディング
                                                                                                        KaiwaPopup endingPopup = KaiwaPopup.newInstance(
                                                                                                                "csv/chouhen/" + jikenParam.chouhenSeriesNumber + "/ending.csv",
                                                                                                                jikenParam.jikenName
                                                                                                        );
                                                                                                        endingPopup.setPopupButtonListener(new PopupButtonListener() {
                                                                                                            @Override
                                                                                                            public void pushedPositiveClick(BasePopup popup) {

                                                                                                            }

                                                                                                            @Override
                                                                                                            public void pushedNegativeClick(BasePopup popup) {

                                                                                                                JikenDetailPopup.this.hideSound = null;
                                                                                                                JikenDetailPopup.this.removeMe();

                                                                                                                popup.removeMe();
                                                                                                            }
                                                                                                        });
                                                                                                        showPopupFromPopup(endingPopup);

                                                                                                        popup.removeMe();

                                                                                                    }
                                                                                                });

                                                                                            showPopupFromPopup(preKaiwa);

                                                                                            myPopup.removeMe();
                                                                                        }

                                                                                        @Override
                                                                                        public void pushedNegativeClick(BasePopup myPoup) {

                                                                                        }
                                                                                    });
                                                                                    showPopupFromPopup(suiriResultPopup);


                                                                                }
                                                                            });
                                                                            fireApiFromPopup(judgeAPI);

                                                                        }
                                                                    });
                                                                    fireApiFromPopup(transactionAPI);


                                                                }
                                                            });
                                                            fireApiFromPopup(tradeAPI);
                                                            cp.removeMe();
                                                        }

                                                        @Override
                                                        public void pushedNegativeClick(BasePopup cp) {
                                                            cp.removeMe();
                                                        }
                                                    });
                                                    showPopupFromPopup(confirmPopup);
                                                }
                                            }
                                        }

                                        @Override
                                        public void pushedNegativeClick(BasePopup myPopup) {

                                        }
                                    };

                                    yougishaPopup.setPopupButtonListener(suiriListener);
                                    doukiPopup.setPopupButtonListener(suiriListener);
                                    busshouPopup.setPopupButtonListener(suiriListener);

                                    showPopupFromPopup(yougishaPopup);


                                }
                                //通常:suiri_normal
                                else {

                                    final boolean firstClear = !UserInfoManager.jikenCleared(jikenParam.jikenID, false);

                                    pushSuiriBusshou(
                                            jikenParam.hazureItemList,
                                            jikenParam.shoukohin,
                                            new APIDialogFragment.APIDialogListener() {
                                                @Override
                                                public void getAPIResult(APIRequest request, Object object) {

                                                    APIResponseParam resultParam = (APIResponseParam) object;

                                                    //容疑者
                                                    String yougisha = jikenParam.hanninName;
                                                    int loseIndex = 0;

                                                    if (!winFlag) {
                                                        for (int i = 0; i < jikenParam.hazureYougishaList.size(); i++) {
                                                            if (jikenParam.hazureItemList.get(i).equals(selectedBusshou)) {
                                                                yougisha = jikenParam.hazureYougishaList.get(i);
                                                                loseIndex = i;
                                                                break;
                                                            }
                                                        }
                                                    }

                                                    CacheManager.instance().jikenNewAlert = winFlag ? firstClear : false;
                                                    CacheManager.instance().jikenClearedID = winFlag ? jikenParam.jikenID : null;


                                                    //マップ更新
                                                    //updateMap();

                                                    //ダイアログ更新
                                                    setMyPageAdapter();


                                                    //結果
                                                    SuiriResultPopup suiriResultPopup = SuiriResultPopup.newInstance(
                                                            jikenParam.jikenID,
                                                            winFlag,
                                                            yougisha,
                                                            selectedBusshou,
                                                            null,
                                                            resultParam.item.clear_jiken
                                                    );

                                                    final int finalLoseIndex = loseIndex;
                                                    suiriResultPopup.setPopupButtonListener(new PopupButtonListener() {
                                                        @Override
                                                        public void pushedPositiveClick(BasePopup mPopup) {

                                                            String filePath;
                                                            if (winFlag)
                                                                filePath = "csv/jiken/" + jikenParam.id + "/ending.csv";
                                                            else
                                                                filePath = "csv/jiken/" + jikenParam.id + "/lose" + (finalLoseIndex + 1) + ".csv";

                                                            KaiwaPopup kaiwaPopup = KaiwaPopup.newInstance(
                                                                    filePath,
                                                                    jikenParam.jikenName
                                                            );
                                                            kaiwaPopup.setPopupButtonListener(new PopupButtonListener() {
                                                                @Override
                                                                public void pushedPositiveClick(BasePopup popup) {

                                                                }

                                                                @Override
                                                                public void pushedNegativeClick(BasePopup popup) {

                                                                    if (winFlag) {
                                                                        //このダイアログ終了
                                                                        JikenDetailPopup.this.hideSound = null;
                                                                        JikenDetailPopup.this.removeMe();

                                                                    }
                                                                    popup.removeMe();
                                                                }
                                                            });
                                                            showPopupFromPopup(kaiwaPopup);

                                                            mPopup.hideSound = null;
                                                            mPopup.removeMe();

                                                        }

                                                        @Override
                                                        public void pushedNegativeClick(BasePopup mPopup) {

                                                        }
                                                    });
                                                    showPopupFromPopup(suiriResultPopup);
                                                }
                                            }
                                    );
                                }
                            }
                        });
                    }
                    //暗号
                    else if (v.equals(angouButton))

                    {
                        //推理長編
                        if (jikenParam.suiriChouhenFlag) {
                            //暗号石板
                            showPopupFromPopup(AngouPopup.newInstance());
                        }
                        //暗号長編
                        else if (jikenParam.angouChouhenFlag) {
                            //取得済み暗号一覧angou
                            ArrayList<String> gottenAngouList = SaveManager.gottenAngouStringList(
                                    jikenParam.jikenID,
                                    jikenParam.angouString,
                                    jikenParam.kakushiAngouString
                            );
                            showPopupFromPopup(AngouListPopup.newInstance(jikenParam.angouString.length(), jikenParam.kakushiAngouString, gottenAngouList));
                        }
                    }
                    //証言
                    else if (v.equals(shougenButton)) {
                        showPopupFromPopup(ShougenListPopup.newInstance(jikenParam.jikenID, 0), true);
                    }
                }
            };
            storyButton.setOnClickListener(listener);
            sousaButton.setOnClickListener(listener);
            angouButton.setOnClickListener(listener);
            suiriButton.setOnClickListener(listener);
            kikikomiButton.setOnClickListener(listener);
            shougenButton.setOnClickListener(listener);

            angouButton.setEnabled(true);
            shougenButton.setEnabled(true);


            return view;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();

            Common.logD("ondestroy detail cell");
        }

        //------- ダイアログ

        /**
         * 推理前会話
         */
        private void pushPreSuiriKaiwa(PopupDisplayListener listener) {

            String filePath;
            String bgKaiwaPath;
            if (jikenParam.angouChouhenFlag) {
                filePath = "csv/jiken/0/pre_suiri_angou.csv";
                bgKaiwaPath = "csv/chouhen/" + jikenParam.chouhenSeriesNumber + "/" + jikenParam.chouhenEpisodeNumber + "/openning.csv";
            } else if (jikenParam.suiriChouhenFlag) {
                filePath = "csv/jiken/0/pre_suiri_test.csv";
                bgKaiwaPath = "csv/chouhen/" + jikenParam.chouhenSeriesNumber + "/" + jikenParam.chouhenEpisodeNumber + "/openning.csv";
            } else if (jikenParam.shougenChouhenFlag) {
                filePath = "csv/jiken/0/pre_suiri_shougen.csv";
                bgKaiwaPath = "csv/chouhen/" + jikenParam.chouhenSeriesNumber + "/" + jikenParam.chouhenEpisodeNumber + "/openning.csv";
            } else {
                filePath = "csv/jiken/0/pre_suiri_normal.csv";
                bgKaiwaPath = "csv/jiken/" + jikenParam.id + "/openning.csv";
            }

            //背景
            String haikeiName = null;
            ArrayList<KaiwaParam> kaiwaParams = CsvManager.kaiwaData(
                    bgKaiwaPath
            );
            for (KaiwaParam kaiwaParam : kaiwaParams) {
                if (kaiwaParam.haikeiName != null && kaiwaParam.haikeiName.length() > 0) {
                    haikeiName = kaiwaParam.haikeiName;
                    break;
                }
            }


            //推理前会話
            KaiwaPopup preKaiwa = KaiwaPopup.newInstance(
                    filePath,
                    jikenParam.jikenName,
                    haikeiName
            );
            preKaiwa.setPopupDisplayListener(listener);
            showPopupFromPopup(preKaiwa);
        }

        /**
         * 推理物証選択
         */
        private void pushSuiriBusshou(ArrayList<String> busshouList, final String seikaiItem,
                                      final APIDialogFragment.APIDialogListener apiDialogListener) {

            final SuiriBusshouPopup selectDialog = SuiriBusshouPopup.newInstance(
                    busshouList,
                    seikaiItem,
                    false
            );
            selectDialog.setBusshouSelectListener(new SuiriBusshouPopup.BusshouSelectListener() {

                @Override
                public void selectBusshou(String str, final SuiriBusshouPopup suiriBusshouPopup) {

                    selectedBusshou = str;

                    if (UserInfoManager.meganeCount() < 1) {
                        //メガネたりない

                        MessagePopup messagePopup = MessagePopup.newInstance(
                                "<img src=\"icon_megane\">虫眼鏡が足りません。<br>"
                                        + "<img src=\"icon_megane\">虫眼鏡を購入しますか？",
                                "やめる", "購入"
                        );
                        messagePopup.setPopupButtonListener(new PopupButtonListener() {

                            @Override
                            public void pushedPositiveClick(BasePopup popup) {
                                //メガネ購入
                                showPopupFromPopup(new MeganePopup());
                            }

                            @Override
                            public void pushedNegativeClick(BasePopup popup) {
                            }
                        });
                        showPopupFromPopup(messagePopup);

                    } else {
                        //メガネ足りた

                        SuiriBusshouConfirmPopup confirmPopup = SuiriBusshouConfirmPopup.newInstance(selectedBusshou);
                        confirmPopup.setPopupButtonListener(new PopupButtonListener() {
                            @Override
                            public void pushedPositiveClick(BasePopup mPopup) {


                                //コンテンツトレード
                                APIRequest tradeRequest = new APIRequest();
                                tradeRequest.name = APIDialogFragment.APIName.CONTENT_TRADE;
                                tradeRequest.params.put("proc_id", Common.myAppContext.getString(R.string.api_proc_trade_jiken));

                                APIDialogFragment tradeAPI = APIDialogFragment.newInstance(tradeRequest);
                                tradeAPI.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
                                    @Override
                                    public void getAPIResult(APIRequest request, Object object) {

                                        //トランザクション
                                        APIResponseParam param = (APIResponseParam) object;
                                        String transaction = new Gson().toJson(param.item.transaction);
                                        String procID = (String) request.params.get("proc_id");

                                        //トランザクション消費
                                        APIRequest transactionRequest = new APIRequest();
                                        transactionRequest.name = APIDialogFragment.APIName.TRANSACTION_COMMIT;
                                        transactionRequest.transactionString = transaction;
                                        transactionRequest.params.put("proc_id", procID);

                                        APIDialogFragment transactionAPI = APIDialogFragment.newInstance(transactionRequest);
                                        transactionAPI.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
                                            @Override
                                            public void getAPIResult(APIRequest request, Object object) {

                                                //ヘッダー更新
                                                updateMyHeaderStatus();

                                                winFlag = selectedBusshou.equals(seikaiItem);

                                                APIRequest judgeRequest = new APIRequest();

                                                //不正解、最初の事件、クリア済み事件はuserinfo更新
                                                if (jikenParam.jikenID.equals("001")
                                                        || UserInfoManager.jikenCleared(jikenParam.jikenID, false)
                                                        || !winFlag) {

                                                    judgeRequest.name = APIDialogFragment.APIName.USER_INFO;
                                                }
                                                //未正解事件はサーバーにクリアを報告
                                                else {
                                                    judgeRequest.name = APIDialogFragment.APIName.JIKEN_CLEAR;
                                                    judgeRequest.params.put("jiken_id", jikenParam.jikenID);
                                                }

                                                APIDialogFragment judgeAPI = APIDialogFragment.newInstance(judgeRequest);
                                                judgeAPI.setAPIDialogListener(apiDialogListener);
                                                fireApiFromPopup(judgeAPI);

                                            }
                                        });
                                        fireApiFromPopup(transactionAPI);
                                    }
                                });
                                fireApiFromPopup(tradeAPI);


                                mPopup.removeMe();
                                suiriBusshouPopup.hideSound = null;
                                suiriBusshouPopup.removeMe();
                            }

                            @Override
                            public void pushedNegativeClick(BasePopup mPopup) {

                            }
                        });
                        showPopupFromPopup(confirmPopup);
                    }
                }
            });
            showPopupFromPopup(selectDialog);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
        }

    }


    public void pushSousaDialog(final JikenParam jikenParam, boolean animate) {

        final KunrenParam kunrenParam = CsvManager.jikenNanido(jikenParam.jikenID);

        if (kunrenParam == null) return;

        KakuninPopup kakuninPopup = KakuninPopup.newinstance(kunrenParam.area, kunrenParam.in_level, jikenParam.jikenName, false, jikenParam);
        kakuninPopup.setPopupButtonListener(new PopupButtonListener() {

            @Override
            public void pushedNegativeClick(BasePopup mPopup) {

            }

            @Override
            public void pushedPositiveClick(BasePopup mPopup) {

                //虫眼鏡所有数
                int currentMegane = UserInfoManager.meganeCount();

                //メガネ足りない
                if (currentMegane < 1) {

                    MessagePopup messagePopup = MessagePopup.newInstance(
                            "<img src=\"icon_megane\">虫眼鏡が足りません。<br>"
                                    + "<img src=\"icon_megane\">虫眼鏡を購入しますか？",
                            "やめる", "購入"
                    );
                    messagePopup.setPopupButtonListener(new PopupButtonListener() {

                        @Override
                        public void pushedNegativeClick(BasePopup popup) {

                        }

                        @Override
                        public void pushedPositiveClick(BasePopup popup) {
                            //メガネ購入
                            showPopupFromPopup(MeganePopup.newInstance());
                        }
                    });
                    showPopupFromPopup(messagePopup);

                }
                //メガネ足りた
                else {

                    //難易度パラメーター
                    NanidoParam nanidoParam = null;
                    ArrayList<NanidoParam> nanidoList = CsvManager.nanidoList();
                    if (nanidoList != null) {
                        for (NanidoParam jikenNanidoParam : nanidoList) {
                            if (jikenNanidoParam.getArea() == kunrenParam.area && jikenNanidoParam.getLevel() == kunrenParam.in_level) {
                                jikenNanidoParam.isKunren = false;
                                nanidoParam = jikenNanidoParam;
                            }
                        }
                    }

                    if (nanidoParam == null) return;


                    //デッキの有効カード数
                    ArrayList<Integer> deckList = SaveManager.deckListByDeckIndex(-1);
                    int deckCardCount = 0;
                    for (int identifier : deckList) {
                        if (identifier > 0) deckCardCount++;
                    }

                    //カード枚数が使用カード枚数以上だと起動できない
                    if (deckCardCount > nanidoParam.getCard()) {

                        MessagePopup messagePopup = MessagePopup.newInstance(
                                "このステージで使える<img src=\"icon_card\">カードは<br/>" + nanidoParam.getCard() + "枚までです。"
                        );
                        showPopupFromPopup(messagePopup);

                        return;
                    }


                    //消費確認
                    MessagePopup messagePopup = MessagePopup.newInstance(
                            "虫眼鏡を1つ使用します。<br>"
                                    + "<img src=\"icon_megane\">" + currentMegane + " → <img src=\"icon_megane\">" + (currentMegane - 1) + "<br>"
                                    + "よろしいですか？",
                            "いいえ", "はい"
                    );
                    messagePopup.setPopupButtonListener(new PopupButtonListener() {

                        @Override
                        public void pushedNegativeClick(BasePopup popup) {

                        }

                        @Override
                        public void pushedPositiveClick(BasePopup popup) {

                            //API：コンテンツトレード
                            APIRequest request = new APIRequest();
                            request.name = APIDialogFragment.APIName.CONTENT_TRADE;
                            request.params.put("proc_id", Common.myAppContext.getString(R.string.api_proc_trade_jiken));

                            APIDialogFragment api = APIDialogFragment.newInstance(request);
                            api.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
                                @Override
                                public void getAPIResult(APIRequest request, Object object) {

                                    //トランザクション
                                    APIResponseParam param = (APIResponseParam) object;
                                    String transaction = new Gson().toJson(param.item.transaction);
                                    String procID = (String) request.params.get("proc_id");

                                    //トランザクション消費
                                    APIRequest tRequest = new APIRequest();
                                    tRequest.name = APIDialogFragment.APIName.TRANSACTION_COMMIT;
                                    tRequest.transactionString = transaction;
                                    tRequest.params.put("proc_id", procID);

                                    APIDialogFragment tApi = APIDialogFragment.newInstance(tRequest);
                                    tApi.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
                                        @Override
                                        public void getAPIResult(APIRequest request, Object object) {

                                            //ユーザーインフォ更新
                                            APIRequest uRequest = new APIRequest();
                                            uRequest.name = APIDialogFragment.APIName.USER_INFO;

                                            APIDialogFragment uApi = APIDialogFragment.newInstance(uRequest);
                                            uApi.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
                                                @Override
                                                public void getAPIResult(APIRequest request, Object object) {

                                                    //ヘッダー更新
                                                    updateMyHeaderStatus();

                                                    //難易度パラメーター
                                                    ArrayList<NanidoParam> nanidoList = CsvManager.nanidoList();
                                                    for (NanidoParam jikenNanidoParam : nanidoList) {
                                                        if (jikenNanidoParam.getArea() == kunrenParam.area && jikenNanidoParam.getLevel() == kunrenParam.in_level) {

                                                            jikenNanidoParam.isKunren = false;

                                                            ((BaseActivity) getActivity()).replaceViewController(SagashiFragment.newInstance(jikenNanidoParam, jikenParam));

                                                            break;
                                                        }
                                                    }

                                                    //kakuninPopup.removeMe();
                                                }
                                            });
                                            fireApiFromPopup(uApi);

                                        }
                                    });
                                    fireApiFromPopup(tApi);

                                }
                            });
                            fireApiFromPopup(api);

                        }

                    });
                    showPopupFromPopup(messagePopup);
                }
            }

        });
        kakuninPopup.setPopupDisplayListener(new PopupDisplayListener() {

            @Override
            public void didShowPopup(BasePopup popup) {
                CacheManager.instance().jikenSousaParam = jikenParam;
                Common.logD("cache up :" + CacheManager.instance().jikenSousaParam);
            }

            @Override
            public void didClosePopup(BasePopup popup) {
            }
        });
        if (!animate) {
            kakuninPopup.noShowAnimation = true;
            kakuninPopup.showSound = null;
        }
        showPopupFromPopup(kakuninPopup);
    }


    //------------------ フリックイベント


    private final GestureDetector.SimpleOnGestureListener mOnGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {

            try {

                if (Math.abs(event1.getY() - event2.getY()) > SWIPE_MAX_OFF_PATH) {

                } else if (event1.getX() - event2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    int newPage = currentPage + 1;
                    if (newPage < totalPages) viewPager.setCurrentItem(newPage, true);

                } else if (event2.getX() - event1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    int newPage = currentPage - 1;
                    if (newPage >= 0) viewPager.setCurrentItem(newPage, true);
                }

            } catch (Exception e) {
                //
                Common.logD("Exception:" + e.toString());
            }

            return false;
        }
    };
}

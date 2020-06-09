package jp.co.cybird.android.conanseek.activity.top;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import jp.co.cybird.android.conanseek.activity.gacha.GachaFragment;
import jp.co.cybird.android.conanseek.activity.jiken.JikenFragment;
import jp.co.cybird.android.conanseek.common.BaseActivity;
import jp.co.cybird.android.conanseek.common.BaseButton;
import jp.co.cybird.android.conanseek.common.BaseCell;
import jp.co.cybird.android.conanseek.common.BaseFragment;
import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.common.ImageGetTask;
import jp.co.cybird.android.conanseek.common.WebViewPopup;
import jp.co.cybird.android.conanseek.manager.APIDialogFragment;
import jp.co.cybird.android.conanseek.manager.APIRequest;
import jp.co.cybird.android.conanseek.manager.BgmManager;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.SaveManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.manager.UserInfoManager;
import jp.co.cybird.android.conanseek.param.APIResponseParam;
import jp.co.cybird.android.conanseek.param.TutorialParam;
import jp.souling.android.conanseek01.R;

/**
 * フラグメント：トップ
 */
public class TopFragment extends BaseFragment {

    //バナー
    private ViewPager viewPager;
    private int currentPage = 0;
    private int totalPages;

    //バナースクロールタイマー
    private Timer bannerTimer;
    private Handler bannerHandler = new android.os.Handler();


    //バナー一覧データ
    private ArrayList<APIResponseParam.Item.Banner> bannerList;

    //カード欄
    private ImageView topDeck1;
    private ImageView topDeck2;
    private ImageView topDeck3;

    public static TopFragment newInstance() {
        
        Bundle args = new Bundle();
        
        TopFragment fragment = new TopFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_top, container, false);

        bgmName = BgmManager.BgmName.MAIN;

        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(5);

        topDeck1 = (ImageView) view.findViewById(R.id.top_deck_1);
        topDeck2 = (ImageView) view.findViewById(R.id.top_deck_2);
        topDeck3 = (ImageView) view.findViewById(R.id.top_deck_3);

        return view;
    }

    @Override
    protected void fragmentDidAppear() {
        super.fragmentDidAppear();

        ArrayList<String> tutorialList = SaveManager.stringArray(SaveManager.KEY.TUTORIAL__stringList);

        if (!tutorialList.contains("jiken_8")) {
            SaveManager.updateStringArray(SaveManager.KEY.TUTORIAL__stringList, "jiken_1", false);
            SaveManager.updateStringArray(SaveManager.KEY.TUTORIAL__stringList, "jiken_2", false);
            SaveManager.updateStringArray(SaveManager.KEY.TUTORIAL__stringList, "jiken_3", false);
            SaveManager.updateStringArray(SaveManager.KEY.TUTORIAL__stringList, "jiken_4", false);
            SaveManager.updateStringArray(SaveManager.KEY.TUTORIAL__stringList, "jiken_5", false);
            SaveManager.updateStringArray(SaveManager.KEY.TUTORIAL__stringList, "jiken_6", false);
            SaveManager.updateStringArray(SaveManager.KEY.TUTORIAL__stringList, "jiken_7", false);
            SaveManager.updateStringArray(SaveManager.KEY.TUTORIAL__stringList, "jiken_8", false);
            SaveManager.updateStringArray(SaveManager.KEY.TUTORIAL__stringList, "jiken_sousa", false);
            tutorialList = SaveManager.stringArray(SaveManager.KEY.TUTORIAL__stringList);
        }


        if (!tutorialList.contains("jiken_1")) {

            startTutorial("jiken_1");

        } else if (tutorialList.contains("jiken_8")) {

            //今日トップページ開いたの初めて?
            String then = SaveManager.stringValue(SaveManager.KEY.TOP_SHOW_DATE__string, "");
            Calendar calendar = Calendar.getInstance();
            final String now = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.DAY_OF_MONTH);

            Common.logD("toppage then:"+then + " now:"+now);

            if (!then.equals(now)) {
                //userinfo更新
                APIRequest request = new APIRequest();
                request.name = APIDialogFragment.APIName.USER_INFO;

                APIDialogFragment f = APIDialogFragment.newInstance(request);
                f.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
                    @Override
                    public void getAPIResult(APIRequest request, Object object) {

                        //今日トップページ見てログインした
                        SaveManager.updateStringValue(SaveManager.KEY.TOP_SHOW_DATE__string, now);

                        WebViewPopup webViewPopup = WebViewPopup.newInstance(R.mipmap.title_oshirase, getString(R.string.oshirase));
                        webViewPopup.setPopupButtonListener(new BasePopup.PopupButtonListener() {
                            @Override
                            public void pushedPositiveClick(BasePopup popup) {

                            }

                            @Override
                            public void pushedNegativeClick(BasePopup popup) {

                                LoginBonusPopup loginBonusPopup = LoginBonusPopup.newInstance();
                                loginBonusPopup.setPopupDisplayListener(new BasePopup.PopupDisplayListener() {
                                    @Override
                                    public void didShowPopup(BasePopup popup) {

                                    }

                                    @Override
                                    public void didClosePopup(BasePopup popup) {

                                        //
                                        if (UserInfoManager.responseParam().item.present > 0) {
                                            SeManager.play(SeManager.SeName.PRESENT_ALERT);
                                        }
                                    }
                                });
                                showPopup(loginBonusPopup);
                            }
                        });
                        showPopup(webViewPopup);

                    }
                });
                fireApi(f);

            } else {

                //
                if (UserInfoManager.responseParam().item.present > 0) {
                    SeManager.play(SeManager.SeName.PRESENT_ALERT);
                }
            }
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        setMyPageAdapter();
        updateDeckImages();

        if (bannerTimer == null) {
            bannerTimer = new Timer();
            bannerTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    bannerHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            viewPager.setCurrentItem(currentPage + 1 + totalPages, true);
                        }
                    });
                }
            }, 5000, 5000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (bannerTimer != null) {
            bannerTimer.cancel();
            bannerTimer = null;
        }
    }


    private void updateDeckImages() {

        ArrayList<Integer> deckList = SaveManager.deckListByDeckIndex(SaveManager.integerValue(SaveManager.KEY.CARD_DECK_IDX__integer, 0));
        ArrayList<APIResponseParam.Item.Card> myCardList = UserInfoManager.responseParam().item.card;
        ArrayList<Integer> cardIdList = new ArrayList<>();

        int index = 0;

        for (int serialID : deckList) {

            cardIdList.add(0);

            for (APIResponseParam.Item.Card card : myCardList) {
                if (card.id == serialID) {
                    cardIdList.add(index, card.card_id);
                    break;
                }
            }
            index++;
        }

        Integer cardID = cardIdList.get(0);

        if (cardID > 0) {
            topDeck1.setImageBitmap(Common.decodedAssetBitmap("egara/426x600/" + cardID + ".jpg", 140, 198, 0.7f));
        } else {
            topDeck1.setImageBitmap(null);
        }


        cardID = cardIdList.get(1);

        if (cardID > 0) {
            topDeck2.setImageBitmap(Common.decodedAssetBitmap("egara/426x600/" + cardID + ".jpg", 140, 198, 0.7f));
        } else {
            topDeck2.setImageBitmap(null);
        }

        cardID = cardIdList.get(2);

        if (cardID > 0) {
            topDeck3.setImageBitmap(Common.decodedAssetBitmap("egara/426x600/" + cardID + ".jpg", 140, 198, 0.7f));
        } else {
            topDeck3.setImageBitmap(null);
        }
    }

    private void setMyPageAdapter() {

        bannerList = UserInfoManager.responseParam().item.banner;

        totalPages = bannerList.size();

        viewPager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentPage = position % totalPages;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

                if (state == ViewPager.SCROLL_STATE_IDLE) {

                    if (currentPage < totalPages || currentPage > totalPages * 2) {
                        viewPager.setCurrentItem(currentPage % totalPages + totalPages, false);
                    }
                }
            }
        });
        viewPager.setCurrentItem(currentPage + totalPages);
    }

    //---tutorial

    @Override
    public void didEndTutorial() {
        super.didEndTutorial();
    }

    @Override
    public void pushedTarget(TutorialParam param) {
        super.pushedTarget(param);

        SeManager.play(SeManager.SeName.PUSH_BUTTON);

        if (param.sousaCode.indexOf("事件ボタン") != -1) {
            ((BaseActivity)getActivity()).replaceViewController(JikenFragment.newInstance(null));
        }
    }


    //---table

    private class MyPagerAdapter extends FragmentStatePagerAdapter {


        public MyPagerAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {

            MyPageCell f = new MyPageCell();

            Bundle bundle = new Bundle();
            bundle.putInt("index", (position % totalPages));
            f.setArguments(bundle);

            return f;
        }

        @Override
        public int getCount() {
            return totalPages * 3;
        }

    }


    /**
     * カスタムセル
     */
    private class MyPageCell extends BaseCell {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            return inflater.inflate(R.layout.view_top_banner_cell, container, false);
        }


        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            BaseButton bannerImage = (BaseButton) view.findViewById(R.id.bannerImage);

            Bundle args = getArguments();
            int index = 0;
            if (args != null) {
                index = args.getInt("index", 0);
            }

            final APIResponseParam.Item.Banner banner = bannerList.get(index);


            String filePath = getString(R.string.api) + "banner/" + banner.img;
            String cacheDir = getActivity().getCacheDir() + "";
            String cacheFile = "banner__" + banner.img;

            Bitmap bitmap = Common.cachedBitmap(cacheFile);
            if (bitmap != null) {
                bannerImage.setImageBitmap(bitmap);
            } else {
                ImageGetTask task = new ImageGetTask(bannerImage, cacheDir, cacheFile);
                task.execute(filePath);
            }


            bannerImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(banner.url));
                    startActivity(intent);
                    //((BaseActivity)getActivity()).replaceViewController(new GachaFragment());

                    //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(banner.url));
                    //startActivity(intent);
                    //Toast.makeText(getContext(), banner.url, Toast.LENGTH_SHORT);

                }
            });
        }

    }
}

package jp.co.cybird.android.conanseek.activity.kunren;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import jp.co.cybird.android.conanseek.activity.jiken.JikenFragment;
import jp.co.cybird.android.conanseek.activity.sagashi.KakuninPopup;
import jp.co.cybird.android.conanseek.common.ArrowButton;
import jp.co.cybird.android.conanseek.common.BaseActivity;
import jp.co.cybird.android.conanseek.common.BaseButton;
import jp.co.cybird.android.conanseek.common.BaseCell;
import jp.co.cybird.android.conanseek.common.BaseFragment;
import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.manager.BgmManager;
import jp.co.cybird.android.conanseek.manager.CacheManager;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.CsvManager;
import jp.co.cybird.android.conanseek.manager.SaveManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.param.KunrenParam;
import jp.co.cybird.android.conanseek.param.TutorialParam;
import jp.souling.android.conanseek01.R;

public class KunrenFragment extends BaseFragment {

    private ViewPager viewPager;
    private int totalPages;
    private int currentPage;
    private boolean noSoundSwipeFlag = false;

    private TextView pageControl;

    private ArrowButton leftArrow;
    private ArrowButton rightArrow;

    private ArrayList<KunrenParam> kunrenList;

    public static KunrenFragment newInstance() {
        
        Bundle args = new Bundle();
        
        KunrenFragment fragment = new KunrenFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_kunren, container, false);

        bgmName = BgmManager.BgmName.KUNREN;

        //ページビュー
        viewPager = (ViewPager) view.findViewById(R.id.viewPager);

        leftArrow = (ArrowButton) view.findViewById(R.id.arrowLeft);
        rightArrow = (ArrowButton) view.findViewById(R.id.arrowRight);
        leftArrow.setOnClickListener(arrowClickListener);
        rightArrow.setOnClickListener(arrowClickListener);


        kunrenList = CsvManager.kunrenList();
        totalPages = (int) Math.ceil((float) kunrenList.size() / 9.0F);


        //ページコントロール
        pageControl = (TextView) view.findViewById(R.id.pagecontrol);
        updatePageControl();

        //ミッションボタン
        view.findViewById(R.id.mission_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SeManager.play(SeManager.SeName.PUSH_BUTTON);
                showPopup(MissionListPopup.newInstance());
            }
        });

        setMyPageAdapter();

        final int areaID = CacheManager.instance().kunrenLevelAreaID;
        final String areaName = CacheManager.instance().kunrenLevelAreaName;

        final int inLevel = CacheManager.instance().kunrenKakuninInLevel;
        final String sagashiTitle = CacheManager.instance().kunrenKakuninTitle;

        //キャッシュポップアップ
        if (areaID > 0 && inLevel > 0 && sagashiTitle != null && areaName != null) {

            //難易度レベル一覧
            ArrayList<Integer> levelList = new ArrayList<>();
            for (KunrenParam p : kunrenList) {
                if (p.area == areaID) {
                    levelList.add(p.in_level);
                }
            }

            //レベル選択
            KunrenLevelPopup levelPopup = KunrenLevelPopup.newInstance(areaID, areaName, levelList);
            levelPopup.noShowAnimation = true;
            levelPopup.showSound = null;
            levelPopup.setPopupDisplayListener(new BasePopup.PopupDisplayListener() {
                @Override
                public void didShowPopup(BasePopup popup) {

                    //確認
                    ((KunrenLevelPopup)popup).pushKakuninPopup(areaID, inLevel, false);
                }

                @Override
                public void didClosePopup(BasePopup popup) {

                }
            });
            showPopup(levelPopup);

        }

        return view;
    }

    private View.OnClickListener arrowClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.equals(leftArrow)) {
                if (currentPage > 0) {
                    viewPager.setCurrentItem(currentPage - 1);
                }
            } else {
                if (currentPage < totalPages - 1) {
                    viewPager.setCurrentItem(currentPage + 1);
                }
            }
        }
    };


    @Override
    protected void fragmentDidAppear() {
        super.fragmentDidAppear();

        ArrayList<String> tutorialList = SaveManager.stringArray(SaveManager.KEY.TUTORIAL__stringList);
        if (!tutorialList.contains("kunren")) {
            startTutorial("kunren");
        }
    }

    //---tutorial

    @Override
    public void didEndTutorial() {
        super.didEndTutorial();
    }

    @Override
    public void pushedTarget(TutorialParam param) {
        super.pushedTarget(param);

        if (param.sousaCode.indexOf("研究所") != -1) {

            SeManager.play(SeManager.SeName.PUSH_BUTTON);

            //難易度レベル一覧
            ArrayList<Integer> levelList = new ArrayList<>();
            for (KunrenParam p : kunrenList) {
                if (p.area == 1) {
                    levelList.add(p.in_level);
                }
            }
            showPopup(KunrenLevelPopup.newInstance(1, "研究所", levelList));
        }
        else if (param.sousaCode.indexOf("戻るボタン") != -1) {

            for (Fragment fragment : getChildFragmentManager().getFragments()) {
                if (fragment instanceof KunrenLevelPopup) {
                    ((BasePopup)fragment).removeMe();
                    SeManager.play(SeManager.SeName.PUSH_BACK);
                    break;
                }
            }
        }
        else if (param.sousaCode.indexOf("ミッションボタン") != -1) {

            SeManager.play(SeManager.SeName.PUSH_BUTTON);
            showPopup(MissionListPopup.newInstance());
        }
    }


    private void updatePageControl() {

        String str = " ";
        for (int i = 0; i < totalPages; i++) {
            if (i == currentPage)
                str += "<font color='#231200'>●</font> ";
            else
                str += "<font color='#ba703d'>●</font> ";
        }
        pageControl.setText(Html.fromHtml(str));

        leftArrow.setBlink(currentPage != 0);
        rightArrow.setBlink(currentPage != totalPages - 1);
    }


    private void setMyPageAdapter() {

        viewPager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));
        viewPager.clearOnPageChangeListeners();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                CacheManager.instance().kunrenScorollPage = currentPage;
                updatePageControl();

                if (!noSoundSwipeFlag)
                    SeManager.play(SeManager.SeName.SWIPE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        currentPage = CacheManager.instance().kunrenScorollPage;

        noSoundSwipeFlag = true;
        viewPager.setCurrentItem(currentPage, false);
        noSoundSwipeFlag = false;
    }


    /**
     * カスタムアダプタ
     */
    private class MyPagerAdapter extends FragmentStatePagerAdapter {

        public MyPagerAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {

            MyPageCell f = new MyPageCell();

            Bundle bundle = new Bundle();
            bundle.putSerializable("position", position);
            f.setArguments(bundle);

            return f;
        }

        @Override
        public int getCount() {
            return totalPages;
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
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.table_kunren, container, false);

            Bundle args = getArguments();
            int pos = 0;
            if (args != null) {
                pos = args.getInt("position", 0);
            }
            int number = pos * 3;

            BaseButton[] buttons = new BaseButton[3];

            for (int i = number, k = 0; i < number + 3; i++, k++) {

                int identifier = Common.myAppContext.getResources().getIdentifier("kunren_banner_" + (k + 1), "id", getActivity().getPackageName());
                buttons[k] = (BaseButton) view.findViewById(identifier);

                int x = number * 3 + k * 3;

                if (x < kunrenList.size()) {

                    final KunrenParam param = kunrenList.get(x);

                    buttons[k].setVisibility(View.VISIBLE);

                    identifier = Common.myAppContext.getResources().getIdentifier("thm_stage_" + param.area, "mipmap", getActivity().getPackageName());
                    buttons[k].setImageResource(identifier);

                    buttons[k].setOnClickListener(null);

                    buttons[k].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            SeManager.play(SeManager.SeName.PUSH_BUTTON);

                            //難易度レベル一覧
                            ArrayList<Integer> levelList = new ArrayList<>();
                            for (KunrenParam p : kunrenList) {
                                if (p.area == param.area) {
                                    levelList.add(p.in_level);
                                }
                            }

                            showPopup(KunrenLevelPopup.newInstance(param.area, param.areaName, levelList));
                        }
                    });

                } else {
                    buttons[k].setVisibility(View.INVISIBLE);
                }

            }


            return view;
        }


    }

}
package jp.co.cybird.android.conanseek.activity.kunren;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import jp.co.cybird.android.conanseek.common.ArrowButton;
import jp.co.cybird.android.conanseek.common.BaseButton;
import jp.co.cybird.android.conanseek.common.BaseCell;
import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.CsvManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.manager.UserInfoManager;
import jp.co.cybird.android.conanseek.param.APIResponseParam;
import jp.souling.android.conanseek01.R;

/**
 * ミッション一覧
 */
public class MissionListPopup extends BasePopup {

    private ArrayList<APIResponseParam.Item.Mission> missionList;

    private Map<Integer, String> areaMap;

    private ViewPager viewPager;
    private int totalPages;
    private int currentPage;
    private int missionLevel = 0;
    private boolean noSoundSwipe = false;

    private BaseButton tab1;
    private BaseButton tab2;
    private BaseButton tab3;

    private TextView pageControl;

    public static MissionListPopup newInstance() {
        
        Bundle args = new Bundle();
        
        MissionListPopup fragment = new MissionListPopup();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.popup_missionlist, container, false);

        //ページビュー
        viewPager = (ViewPager) view.findViewById(R.id.card_view_pager);
        missionList = UserInfoManager.responseParam().item.mission;
        totalPages = missionList.size() / 3 / 4;
        currentPage = 0;


        leftArrow = (ArrowButton) view.findViewById(R.id.arrowLeft);
        rightArrow = (ArrowButton) view.findViewById(R.id.arrowRight);
        leftArrow.setOnClickListener(arrowClickListener);
        rightArrow.setOnClickListener(arrowClickListener);

        //クリア条件文言用エリアファイル
        areaMap = CsvManager.areaList();

        setMyPageAdapter();

        //ページコントロール
        pageControl = (TextView) view.findViewById(R.id.pagecontrol);
        updatePageControl();

        //タブ
        tab1 = (BaseButton) view.findViewById(R.id.mission_tab_1);
        tab2 = (BaseButton) view.findViewById(R.id.mission_tab_2);
        tab3 = (BaseButton) view.findViewById(R.id.mission_tab_3);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SeManager.play(SeManager.SeName.PUSH_BUTTON);
                if (v.equals(tab1)) {
                    missionLevel = 0;
                } else if (v.equals(tab2)) {
                    missionLevel = 1;
                } else if (v.equals(tab3)) {
                    missionLevel = 2;
                }
                updateTab();
                setMyPageAdapter();
            }
        };

        tab1.setOnClickListener(listener);
        tab2.setOnClickListener(listener);
        tab3.setOnClickListener(listener);
        updateTab();


        view.findViewById(R.id.dialog_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SeManager.play(SeManager.SeName.PUSH_BACK);
                removeMe();
            }
        });

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
                updatePageControl();
                if (!noSoundSwipe)
                    SeManager.play(SeManager.SeName.SWIPE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        noSoundSwipe = true;
        viewPager.setCurrentItem(currentPage, false);
        noSoundSwipe = false;
    }


    /**
     * タブ更新
     */
    private void updateTab() {

        tab1.setSelected(missionLevel == 0);
        tab2.setSelected(missionLevel == 1);
        tab3.setSelected(missionLevel == 2);

    }


    /**
     * ページコントロール更新
     */
    private void updatePageControl() {

        String str = " ";
        for (int i = 0; i < totalPages; i++) {
            if (i == currentPage)
                str += "<font color='#98654a'>●</font> ";
            else
                str += "<font color='#d09a78'>●</font> ";
        }
        pageControl.setText(Html.fromHtml(str));

        leftArrow.setBlink(currentPage != 0);
        rightArrow.setBlink(currentPage != totalPages - 1);
    }

    /**
     * カスタムアダプター
     */
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

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.table_missionlist, container, false);

            int position = 0;
            Bundle args = getArguments();
            if (args != null) {
                position = args.getInt("position");
            }

            for (int row = 0; row < 4; row++) {

                int index = position * 3 * 4 + missionLevel * 4 + row;

                APIResponseParam.Item.Mission param = missionList.get(index);

                int identifier = Common.myAppContext.getResources().getIdentifier("mission_cell_" + (row + 1), "id", getActivity().getPackageName());

                //開放済み
                if (param.cleared) {
                    //findViewById(identifier)の中のfindViewById(R.id.mission_done)に修正
                    view.findViewById(identifier).findViewById(R.id.mission_done).setVisibility(View.VISIBLE);
                }

                //開放条件
                TextView joukenLabel = (TextView) view.findViewById(identifier).findViewById(R.id.mission_jouken_label);

                String joukenString = areaMap.get(param.area_id) + "Lv." + param.level + "を" + param.amount_int + "回クリア";
                joukenLabel.setText(joukenString);

                //リワード
                TextView rewardLabel = (TextView) view.findViewById(identifier).findViewById(R.id.mission_reward_label);
                String rewardString = param.reward;

                if (param.reward.equals("虫眼鏡")) {
                    rewardString = "<img src=\"icon_megane\"> x " + param.reward_amount;
                } else if (param.reward.equals("ガチャチケット")) {
                    rewardString = "<img src=\"icon_ticket\"> x " + param.reward_amount;
                }

                rewardLabel.setText(Html.fromHtml(
                        rewardString,
                        new Common.ResouroceImageGetter(getContext()), null));


            }

            return view;
        }
    }
}

package jp.co.cybird.android.conanseek.activity.card;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Map;

import jp.co.cybird.android.conanseek.common.ArrowButton;
import jp.co.cybird.android.conanseek.common.BaseButton;
import jp.co.cybird.android.conanseek.common.BaseCell;
import jp.co.cybird.android.conanseek.common.UntouchableViewPager;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.CsvManager;
import jp.co.cybird.android.conanseek.manager.SaveManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.manager.UserInfoManager;
import jp.co.cybird.android.conanseek.param.APIResponseParam;
import jp.co.cybird.android.conanseek.param.CardParam;
import jp.souling.android.conanseek01.R;
import jp.souling.android.conanseek01.Settings;

/**
 * カード：図鑑カード詳細
 */
public class CardZukanDetailFragment extends CardContentFragment {

    private UntouchableViewPager viewPager;
    private int totalPages;
    private int currentPage;
    private boolean noSwipeSoundFlag = false;

    private ArrowButton leftArrow;
    private ArrowButton rightArrow;

    // カード番号別の詳細情報
    private Map<Integer, CardParam> cardMapList;

    // 所有済み一覧
    private ArrayList<Integer> haveList;

    //初期表示のカードID
    private int startCardID;

    //デバッグ用全部見せる設定
    private boolean debugShowAllFlag = Settings.isDebug;


    private static final int SWIPE_MIN_DISTANCE = 50;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private static final int SWIPE_MAX_OFF_PATH = 250;


    public static CardZukanDetailFragment newInstance(int startCardID) {

        Bundle args = new Bundle();

        args.putInt("startCardID", startCardID);

        CardZukanDetailFragment fragment = new CardZukanDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_card_content_detail, container, false);

        Bundle arg = getArguments();
        if (arg != null) {
            this.startCardID = arg.getInt("startCardID");
        }

        haveList = UserInfoManager.zukanList();
        cardMapList = CsvManager.cardParamsWithSkillDetail();

        if (debugShowAllFlag) {
            debugShowAllFlag = SaveManager.boolValue(SaveManager.KEY.DEBUG_CARD_ALL_HAVE__boolean, false);
        }

        //ページビュー
        viewPager = (UntouchableViewPager) view.findViewById(R.id.viewPager);
        totalPages = Settings.totalCards;
        currentPage = this.startCardID - 1;

        leftArrow = (ArrowButton) view.findViewById(R.id.arrowLeft);
        rightArrow = (ArrowButton) view.findViewById(R.id.arrowRight);
        leftArrow.setOnClickListener(onClickListener);
        rightArrow.setOnClickListener(onClickListener);

        leftArrow.setBlink(true);
        rightArrow.setBlink(true);

        //戻る
        view.findViewById(R.id.card_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeManager.play(SeManager.SeName.PUSH_BACK);

                int currentItem = viewPager.getCurrentItem();

                CardZukanFragment zukanFragment = (CardZukanFragment)((CardFragment) getParentFragment()).getPrevContentFragment(CardZukanDetailFragment.this);

                zukanFragment.setFirstShowCardId(currentItem % totalPages + 1);

                ((CardFragment) getParentFragment()).popContentFragment(CardZukanDetailFragment.this);
            }
        });

        //スワイプ移動
        final GestureDetector gesture = new GestureDetector(getActivity(),
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
                            } else if (event1.getX() - event2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                viewPager.setCurrentItem(currentPage + 1, true);
                            } else if (event2.getX() - event1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                viewPager.setCurrentItem(currentPage - 1, true);
                            }

                        } catch (Exception e) {
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


    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.equals(leftArrow)) {
                viewPager.setCurrentItem(currentPage - 1);
            } else if (v.equals(rightArrow)) {
                viewPager.setCurrentItem(currentPage + 1);
            }
        }
    };


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setMyPageAdapter();
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
                if (!noSwipeSoundFlag)
                    SeManager.play(SeManager.SeName.SWIPE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

                if (state == ViewPager.SCROLL_STATE_IDLE) {

                    if (currentPage < totalPages || currentPage >= totalPages * 2) {
                        noSwipeSoundFlag = true;
                        viewPager.setCurrentItem(currentPage % totalPages + totalPages, false);
                        noSwipeSoundFlag = false;
                    }
                }
            }
        });
        noSwipeSoundFlag = true;
        viewPager.setCurrentItem(currentPage + totalPages);
        noSwipeSoundFlag = false;
    }


    private class MyPagerAdapter extends FragmentStatePagerAdapter {

        public MyPagerAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {

            //カード情報をセルに与える
            Bundle bundle = new Bundle();
            bundle.putInt("position", position);

            MyPageCell f = new MyPageCell();
            f.setArguments(bundle);

            return f;
        }


        @Override
        public int getCount() {
            return totalPages * 3;
        }

    }


    private class MyPageCell extends BaseCell {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.table_card_detail, container, false);
        }


        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            Bundle arg = getArguments();
            int position = 0;
            if (arg != null) {
                position = arg.getInt("position", 0);
            }
            int cardID = position % totalPages + 1;

            CardParam cardParam = cardMapList.get(cardID);

            //fav
            view.findViewById(R.id.btn_card_fav).setVisibility(View.GONE);

            TextView numberText = (TextView) view.findViewById(R.id.card_number_label);
            ImageView rareImage = (ImageView) view.findViewById(R.id.card_rare_image);
            TextView nameText = (TextView) view.findViewById(R.id.card_title_label);
            ImageView egaraImage = (ImageView) view.findViewById(R.id.card_face_image);
            ImageView skillImage = (ImageView) view.findViewById(R.id.card_skill_icon_image);
            TextView skillName = (TextView) view.findViewById(R.id.card_skill_name_label);
            TextView skillDetail = (TextView) view.findViewById(R.id.card_skill_detal_label);


            numberText.setText("No." + cardParam.cardID);

            //持っていない
            if (!haveList.contains(cardParam.cardID) && !debugShowAllFlag) {

                nameText.setText(null);
                skillName.setText(null);
                skillDetail.setText(null);
                rareImage.setImageBitmap(null);
                egaraImage.setImageBitmap(null);
                skillImage.setImageBitmap(null);

            }
            //持ってる
            else {

                nameText.setText(cardParam.name);
                skillName.setText(cardParam.skillName);
                skillDetail.setText(cardParam.skillDetailLong);

                egaraImage.setImageBitmap(Common.decodedAssetBitmap("egara/426x600/" + cardParam.cardID + ".jpg", 153, 207, 0.5f));

                switch (cardParam.skillType) {
                    case Time:
                        skillImage.setImageResource(R.mipmap.skill_time_icon);
                        break;
                    case Direction:
                        skillImage.setImageResource(R.mipmap.skill_direction_icon);
                        break;
                    case Color:
                        skillImage.setImageResource(R.mipmap.skill_color_icon);
                        break;
                    case Target:
                        skillImage.setImageResource(R.mipmap.skill_target_icon);
                        break;
                    case Number:
                        skillImage.setImageResource(R.mipmap.skill_order_icon);
                        break;
                }


                switch (cardParam.rareInt) {
                    case 1:
                        rareImage.setImageBitmap(Common.decodedResource(R.mipmap.rarity_hn, 40, 40));
                        break;
                    case 2:
                        rareImage.setImageBitmap(Common.decodedResource(R.mipmap.rarity_r, 40, 40));
                        break;
                    case 3:
                        rareImage.setImageBitmap(Common.decodedResource(R.mipmap.rarity_sr, 40, 40));
                        break;
                    case 4:
                        rareImage.setImageBitmap(Common.decodedResource(R.mipmap.rarity_ssr, 40, 40));
                        break;
                    default:
                        rareImage.setImageBitmap(Common.decodedResource(R.mipmap.rarity_n, 40, 40));
                        break;
                }
            }
        }
    }


}

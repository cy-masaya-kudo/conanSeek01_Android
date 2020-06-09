package jp.co.cybird.android.conanseek.activity.card;


import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import jp.co.cybird.android.conanseek.common.ArrowButton;
import jp.co.cybird.android.conanseek.common.BaseButton;
import jp.co.cybird.android.conanseek.common.BaseCell;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.CsvManager;
import jp.co.cybird.android.conanseek.manager.SaveManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.manager.UserInfoManager;
import jp.co.cybird.android.conanseek.param.CardParam;
import jp.souling.android.conanseek01.R;
import jp.souling.android.conanseek01.Settings;

/**
 * カード：図鑑
 */
public class CardZukanFragment extends CardContentFragment {

    private ViewPager viewPager;
    private int currentPage;
    private int totalPages;

    private ArrowButton leftArrow;
    private ArrowButton rightArrow;
    private boolean noSwipeSoundFlag = false;

    // カード番号別の詳細情報
    private Map<Integer, CardParam> cardMapList;

    // 所有済み一覧
    private ArrayList<Integer> haveList;

    //デバッグ用全部見せる設定
    private boolean debugShowAllFlag = Settings.isDebug;


    public static CardZukanFragment newInstance() {

        Bundle args = new Bundle();

        CardZukanFragment fragment = new CardZukanFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_card_content_zukan, container, false);

        haveList = UserInfoManager.zukanList();
        cardMapList = CsvManager.cardParamsWithSkillDetail();

        if (debugShowAllFlag) {
            debugShowAllFlag = SaveManager.boolValue(SaveManager.KEY.DEBUG_CARD_ALL_HAVE__boolean, false);
        }

        //ページ数
        totalPages = (int) Math.ceil((float) Settings.totalCards / (float) 16);

        //ページビュー
        viewPager = (ViewPager) view.findViewById(R.id.viewPager);


        leftArrow = (ArrowButton) view.findViewById(R.id.arrowLeft);
        rightArrow = (ArrowButton) view.findViewById(R.id.arrowRight);
        leftArrow.setOnClickListener(onClickListener);
        rightArrow.setOnClickListener(onClickListener);

        view.findViewById(R.id.card_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeManager.play(SeManager.SeName.PUSH_BACK);
                ((CardFragment) getParentFragment()).popContentFragment(CardZukanFragment.this);
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setMyPageAdapter();
    }

    //最初に一覧にあるカードID
    public void setFirstShowCardId(int cardId) {
        currentPage = (cardId - 1) / 16;

        noSwipeSoundFlag = true;
        viewPager.setCurrentItem(currentPage);
        noSwipeSoundFlag = false;
    }


    private View.OnClickListener onClickListener = new View.OnClickListener() {
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

                if (!noSwipeSoundFlag)
                    SeManager.play(SeManager.SeName.SWIPE);

                leftArrow.setBlink(position != 0);
                rightArrow.setBlink(position != totalPages - 1);

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        noSwipeSoundFlag = true;
        viewPager.setCurrentItem(currentPage);
        noSwipeSoundFlag = false;

        leftArrow.setBlink(currentPage != 0);
        rightArrow.setBlink(currentPage != totalPages - 1);
    }


    private class MyPagerAdapter extends FragmentStatePagerAdapter {

        public MyPagerAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {

            //左上カードのカードIDのみ渡す
            MyPageCell f = new MyPageCell();

            Bundle bundle = new Bundle();
            bundle.putInt("first_card_id", position * 16 + 1);
            f.setArguments(bundle);

            return f;
        }

        @Override
        public int getCount() {
            return totalPages;
        }

    }

    private class MyPageCell extends BaseCell {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.table_cell_card_list_with_title, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            int startCardlID = 1;
            Bundle arg = getArguments();
            if (arg != null) {
                startCardlID = arg.getInt("first_card_id", 1);
            }

            for (int i = 0; i < 16; i++) {

                int identifier = Common.myAppContext.getResources().getIdentifier("list_cell_" + i, "id", getActivity().getPackageName());
                ViewGroup cell = (ViewGroup) view.findViewById(identifier);

                int cardID = startCardlID + i;

                final CardParam cardParam = cardMapList.get(cardID);

                //カード総数を超える
                if (cardID > Settings.totalCards) {

                    //カード枠自体を見せない
                    cell.setVisibility(View.INVISIBLE);

                } else {

                    cell.setVisibility(View.VISIBLE);

                    //new
                    cell.findViewById(R.id.cell_new).setVisibility(View.GONE);
                    //fav
                    cell.findViewById(R.id.cell_fav).setVisibility(View.GONE);

                    //No
                    ((TextView) cell.findViewById(R.id.cell_text)).setText("No." + cardParam.cardID);

                    //egara
                    BaseButton cardFace = (BaseButton) cell.findViewById(R.id.cell_card);

                    //skill
                    ImageView skillTag = (ImageView) cell.findViewById(R.id.cell_skill);


                    if (!haveList.contains(cardParam.cardID) && !debugShowAllFlag) {
                        //持っていない
                        skillTag.setVisibility(View.INVISIBLE);
                        cardFace.setImageBitmap(null);
                        cardFace.setOnClickListener(null);
                        cardFace.setEnabled(false);
                    } else {
                        //持っている

                        //skill
                        skillTag.setVisibility(View.VISIBLE);
                        switch (cardParam.skillType) {
                            case Time:
                                skillTag.setImageResource(R.mipmap.skill_time);
                                break;
                            case Direction:
                                skillTag.setImageResource(R.mipmap.skill_direction);
                                break;
                            case Color:
                                skillTag.setImageResource(R.mipmap.skill_color);
                                break;
                            case Target:
                                skillTag.setImageResource(R.mipmap.skill_target);
                                break;
                            case Number:
                                skillTag.setImageResource(R.mipmap.skill_order);
                                break;
                        }

                        //egara
                        cardFace.setImageBitmap(Common.decodedAssetBitmap("egara/180x254/" + cardParam.cardID + ".jpg", 60, 90, 0.5f));

                        //button
                        cardFace.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                SeManager.play(SeManager.SeName.PUSH_BUTTON);

                                //詳細画面プッシュ
                                pushDetailFragment(cardParam.cardID);
                            }
                        });
                        cardFace.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {

                                SeManager.play(SeManager.SeName.PUSH_BUTTON);

                                //詳細画面プッシュ
                                pushDetailFragment(cardParam.cardID);
                                return true;
                            }
                        });
                    }
                }
            }
        }


        private void pushDetailFragment(Integer start) {


            CardZukanDetailFragment cardZukanDetailFragment = CardZukanDetailFragment.newInstance(start);
            ((CardFragment) getParentFragment().getParentFragment()).addContentFragment(cardZukanDetailFragment);
        }

    }
}

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
import jp.co.cybird.android.conanseek.param.APIResponseParam;
import jp.co.cybird.android.conanseek.param.CardParam;
import jp.souling.android.conanseek01.R;
import jp.souling.android.conanseek01.Settings;

/**
 * カード：デッキ編集
 */
public class CardDeckFragment extends CardContentFragment implements View.OnClickListener {

    private ViewPager viewPager;
    private int totalPages;
    private int currentPage;
    private boolean noSwipeSoundFlag = false;

    private ArrowButton leftArrow;
    private ArrowButton rightArrow;

    //カード詳細情報
    private Map<Integer, CardParam> cardParamMap;

    //所持カード一覧
    private ArrayList<APIResponseParam.Item.Card> myCardList;

    //お気に入り一覧
    private ArrayList<Integer> favList;

    public static CardDeckFragment newInstance() {

        Bundle args = new Bundle();

        CardDeckFragment fragment = new CardDeckFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_card_content_deck, container, false);

        cardParamMap = CsvManager.cardParamsWithSkillDetail();
        myCardList = UserInfoManager.myCardList();

        //ページ数
        totalPages = Settings.totalDecks;

        //ページビュー
        viewPager = (ViewPager) view.findViewById(R.id.viewPager);

        currentPage = SaveManager.integerValue(SaveManager.KEY.CARD_DECK_IDX__integer, 0);

        leftArrow = (ArrowButton) view.findViewById(R.id.arrowLeft);
        rightArrow = (ArrowButton) view.findViewById(R.id.arrowRight);
        leftArrow.setOnClickListener(this);
        rightArrow.setOnClickListener(this);
        leftArrow.setBlink(true);
        rightArrow.setBlink(true);

        view.findViewById(R.id.card_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeManager.play(SeManager.SeName.PUSH_BACK);
                ((CardFragment) getParentFragment()).popContentFragment(CardDeckFragment.this);
            }
        });

        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setMyPageAdapter();
    }

    //更新
    public void updateTable() {
        setMyPageAdapter();
    }


    @Override
    public void willShowAgain() {
        super.willShowAgain();
        updateTable();
    }

    private void setMyPageAdapter() {

        favList = SaveManager.integerList(SaveManager.KEY.CARD_FAV__integerList);

        viewPager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));
        viewPager.clearOnPageChangeListeners();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                currentPage = position;

                //選択デッキ更新
                SaveManager.updateInegerValue(SaveManager.KEY.CARD_DECK_IDX__integer, currentPage % totalPages);

                if (!noSwipeSoundFlag)
                    SeManager.play(SeManager.SeName.SWIPE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

                if (state == ViewPager.SCROLL_STATE_IDLE) {

                    if (currentPage < totalPages || currentPage > totalPages * 2) {
                        noSwipeSoundFlag = true;
                        viewPager.setCurrentItem(currentPage % totalPages + totalPages, false);
                        noSwipeSoundFlag = false;
                    }
                }
            }
        });
        noSwipeSoundFlag = true;
        viewPager.setCurrentItem(currentPage % totalPages + totalPages);
        noSwipeSoundFlag = false;
    }


    @Override
    public void onClick(View v) {

        if (v.equals(leftArrow)) {
            viewPager.setCurrentItem(currentPage - 1);
        } else if (v.equals(rightArrow)) {
            viewPager.setCurrentItem(currentPage + 1);
        }

    }


    private class MyPagerAdapter extends FragmentStatePagerAdapter {

        public MyPagerAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {

            MyPageCell f = new MyPageCell();

            //デッキ番号のみセルに渡す
            Bundle bundle = new Bundle();
            bundle.putInt("deck_index", (position % totalPages));
            f.setArguments(bundle);

            return f;
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public int getCount() {
            return totalPages * 3;
        }

    }

    private class MyPageCell extends BaseCell {

        ArrayList<Integer> deckList;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.table_card_deck, container, false);
        }


        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            //デッキIndex
            Bundle arg = getArguments();
            int deckIndex = 0;
            if (arg != null) {
                deckIndex = arg.getInt("deck_index", 0);
            }

            //デッキデータ
            deckList = SaveManager.deckListByDeckIndex(deckIndex);

            //デッキ名
            ImageView deckTitle = (ImageView) view.findViewById(R.id.deck_title_image);
            int titleID = Common.myAppContext.getResources().getIdentifier("label_deck_" + (deckIndex + 1), "mipmap", getActivity().getPackageName());
            deckTitle.setImageBitmap(Common.decodedResource(titleID, 51, 14));

            //カードセル
            for (int i = 0; i < 3; i++) {

                int identifier = Common.myAppContext.getResources().getIdentifier("list_cell_" + i, "id", getActivity().getPackageName());

                View cell = view.findViewById(identifier);

                ImageView favImage = (ImageView) cell.findViewById(R.id.cell_fav);
                ImageView skillImage = (ImageView) cell.findViewById(R.id.cell_skill);
                TextView skillName = (TextView) cell.findViewById(R.id.skill_name);
                TextView skillDetail = (TextView) cell.findViewById(R.id.skill_detail);

                BaseButton cardButton = (BaseButton) cell.findViewById(R.id.cell_card);

                final int serialID = deckList.get(i);

                if (serialID > 0) {

                    Integer cardID = 0;

                    //カードIDを調べる
                    for (int k = 0; k < myCardList.size(); k++) {
                        Integer mySerialID = myCardList.get(k).id;
                        if (mySerialID.equals(serialID)) {
                            cardID = myCardList.get(k).card_id;
                        }
                    }

                    if (cardID > 0) {

                        CardParam card = cardParamMap.get(cardID);


                        cardButton.setImageBitmap(Common.decodedAssetBitmap("egara/326x460/" + cardID + ".jpg", 103, 143, 0.5f));

                        //お気に入り
                        favImage.setVisibility(favList.contains(serialID) ? View.VISIBLE : View.INVISIBLE);

                        //スキル
                        switch (card.skillType) {

                            case Time:
                                skillImage.setImageBitmap(Common.decodedResource(R.mipmap.skill_time, 17, 17));
                                break;
                            case Direction:
                                skillImage.setImageBitmap(Common.decodedResource(R.mipmap.skill_direction, 17, 17));
                                break;
                            case Color:
                                skillImage.setImageBitmap(Common.decodedResource(R.mipmap.skill_color, 17, 17));
                                break;
                            case Target:
                                skillImage.setImageBitmap(Common.decodedResource(R.mipmap.skill_target, 17, 17));
                                break;
                            case Number:
                                skillImage.setImageBitmap(Common.decodedResource(R.mipmap.skill_order, 17, 17));
                                break;
                        }

                        skillName.setText(card.skillName);
                        skillDetail.setText(card.skillDetail);
                    }

                } else {
                    favImage.setVisibility(View.INVISIBLE);
                    skillImage.setVisibility(View.INVISIBLE);
                    skillName.setText("");
                    skillDetail.setText("");

                }

                final int listIndex = i;

                //button
                cardButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        SeManager.play(SeManager.SeName.PUSH_BUTTON);

                        //選択画面プッシュ
                        CardSelectFragment cardSelectFragment = CardSelectFragment.newInstance(false, deckList, listIndex);
                        ((CardFragment) getParentFragment().getParentFragment()).addContentFragment(cardSelectFragment);
                    }
                });
                if (serialID > 0) {

                    //カードが入っている場合は長押しで詳細に飛ぶ
                    cardButton.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {

                            SeManager.play(SeManager.SeName.PUSH_BUTTON);

                            int detailIndex = 0;

                            ArrayList<APIResponseParam.Item.Card> mySortedCardList = UserInfoManager.mySortedCardList(-1);
                            for (APIResponseParam.Item.Card c : mySortedCardList) {
                                if (c.id == serialID) {
                                    break;
                                }
                                detailIndex++;
                            }

                            //詳細画面プッシュ
                            CardShojiDetailFragment cardShojiDetailFragment = CardShojiDetailFragment.newInstance(detailIndex, null);
                            ((CardFragment) getParentFragment().getParentFragment()).addContentFragment(cardShojiDetailFragment);
                            return true;
                        }
                    });
                }
            }
        }
    }


}

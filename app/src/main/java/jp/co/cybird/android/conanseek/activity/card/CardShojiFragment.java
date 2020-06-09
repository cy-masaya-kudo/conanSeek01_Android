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
import jp.co.cybird.android.conanseek.common.BaseFragment;
import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.CsvManager;
import jp.co.cybird.android.conanseek.manager.SaveManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.manager.UserInfoManager;
import jp.co.cybird.android.conanseek.param.APIResponseParam;
import jp.co.cybird.android.conanseek.param.CardParam;
import jp.souling.android.conanseek01.R;

import static jp.co.cybird.android.conanseek.manager.SaveManager.integerValue;

/**
 * カード：所持
 */
public class CardShojiFragment extends CardContentFragment implements View.OnClickListener {

    private ViewPager viewPager;
    private int totalPages;
    private int currentPage;

    private ArrowButton leftArrow;
    private ArrowButton rightArrow;
    private BaseButton sortButton;
    private boolean noSwipeSoundFlag = false;

    // カード番号別の詳細情報
    private Map<Integer, CardParam> cardMapList;

    // 表示カード配列
    private ArrayList<APIResponseParam.Item.Card> haveList;

    //お気に入りカード一覧
    ArrayList<Integer> favSerialList;

    //一度見たserial
    private int maxSerialId = 0;
    private int lastMaxSerialId = 0;


    private TextView pageText;

    public static CardShojiFragment newInstance() {

        Bundle args = new Bundle();

        CardShojiFragment fragment = new CardShojiFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_card_content_shoji, container, false);

        //ページ番号
        pageText = (TextView) view.findViewById(R.id.page_label);

        //スキル詳細情報テーブル
        cardMapList = CsvManager.cardParamsWithSkillDetail();

        //ページビュー
        viewPager = (ViewPager) view.findViewById(R.id.viewPager);

        leftArrow = (ArrowButton) view.findViewById(R.id.arrowLeft);
        rightArrow = (ArrowButton) view.findViewById(R.id.arrowRight);
        leftArrow.setOnClickListener(this);
        rightArrow.setOnClickListener(this);

        sortButton = (BaseButton) view.findViewById(R.id.sort_button);
        sortButton.setOnClickListener(this);

        //所有済み一覧更新
        updateHaveList();

        //ソートボタン表示更新
        updateSortButton();

        view.findViewById(R.id.card_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeManager.play(SeManager.SeName.PUSH_BACK);
                ((CardFragment) getParentFragment()).popContentFragment(CardShojiFragment.this);
            }
        });

        lastMaxSerialId = SaveManager.integerValue(SaveManager.KEY.CARD_NEW_SERIAL__integer, 0);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setMyPageAdapter();
        updatePageLabel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //チェックしたカードシリアル番号を更新
        if (lastMaxSerialId < maxSerialId) {
            SaveManager.updateInegerValue(SaveManager.KEY.CARD_NEW_SERIAL__integer, maxSerialId);
        }
    }

    //最初に一覧にあるカードindex
    public void setFirstShowCardIndex(int index) {

        favSerialList = SaveManager.integerList(SaveManager.KEY.CARD_FAV__integerList);

        currentPage = index / 16;

        //ソース
        updateHaveList();
        setMyPageAdapter();
        updatePageLabel();
    }


    //表示データ更新
    private void updateHaveList() {

        haveList = UserInfoManager.mySortedCardList(-1);

        favSerialList = SaveManager.integerList(SaveManager.KEY.CARD_FAV__integerList);

        //ページ数
        totalPages = haveList.size() / 16 + 1;

        updatePageLabel();
    }


    // ページ番号更新
    private void updatePageLabel() {
        pageText.setText((currentPage + 1) + "/" + totalPages);

    }

    //ソートボタン情報更新
    private void updateSortButton() {

        Integer current = integerValue(SaveManager.KEY.CARD_ORDER__integer, 0);

        if (current == SaveManager.ORDER_GET) {
            sortButton.setImageResource(R.mipmap.btn_sort_get);
        } else if (current == SaveManager.ORDER_NUM) {
            sortButton.setImageResource(R.mipmap.btn_sort_number);
        } else if (current == SaveManager.ORDER_RARE) {
            sortButton.setImageResource(R.mipmap.btn_sort_rarity);
        } else if (current == SaveManager.ORDER_FAV) {
            sortButton.setImageResource(R.mipmap.btn_sort_favorite);
        } else if (current == SaveManager.ORDER_TGT) {
            sortButton.setImageResource(R.mipmap.btn_sort_target);
        } else if (current == SaveManager.ORDER_STIME) {
            sortButton.setImageResource(R.mipmap.btn_sort_time);
        } else if (current == SaveManager.ORDER_SDIR) {
            sortButton.setImageResource(R.mipmap.btn_sort_direction);
        } else if (current == SaveManager.ORDER_SNUM) {
            sortButton.setImageResource(R.mipmap.btn_sort_order);
        } else if (current == SaveManager.ORDER_SCOL) {
            sortButton.setImageResource(R.mipmap.btn_sort_color);
        }
    }


    private void setMyPageAdapter() {

        //お気に入り

        viewPager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));
        viewPager.clearOnPageChangeListeners();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                currentPage = position;
                updatePageLabel();

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


    @Override
    public void onClick(View v) {

        if (v.equals(sortButton)) {
            SeManager.play(SeManager.SeName.PUSH_BUTTON);

            SortPopup sortPopup = new SortPopup();
            sortPopup.setPopupButtonListener(new BasePopup.PopupButtonListener() {
                @Override
                public void pushedPositiveClick(BasePopup popup) {

                    currentPage = 0;
                    updateHaveList();
                    updateSortButton();
                    setMyPageAdapter();
                }

                @Override
                public void pushedNegativeClick(BasePopup popup) {

                }
            });
            showPopupFromCardContent(sortPopup);

        } else if (v.equals(leftArrow)) {
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

            //左上カードindex
            int startSerialIndex = position * 16;

            //カード情報をセルに与える
            MyPageCell f = new MyPageCell();

            Bundle bundle = new Bundle();
            bundle.putInt("start_serial_index", startSerialIndex);

            f.setArguments(bundle);

            return f;
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public int getCount() {
            return totalPages;
        }

    }


    private class MyPageCell extends BaseCell {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.table_cell_card_list_without_title, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            Bundle arg = getArguments();

            int startSerialIndex = 1;
            if (arg != null) {
                startSerialIndex = arg.getInt("start_serial_index", 1);
            }


            for (int i = startSerialIndex, k = 0; i < startSerialIndex + 16; i++, k++) {

                int identifier = Common.myAppContext.getResources().getIdentifier("list_cell_" + k, "id", getActivity().getPackageName());
                ViewGroup cell = (ViewGroup) view.findViewById(identifier);


                ImageView newTag = (ImageView) cell.findViewById(R.id.cell_new);
                ImageView favTag = (ImageView) cell.findViewById(R.id.cell_fav);
                BaseButton cardFace = (BaseButton) cell.findViewById(R.id.cell_card);
                ImageView skillTag = (ImageView) cell.findViewById(R.id.cell_skill);

                //端数
                if (haveList.size() <= i) {

                    newTag.setVisibility(View.GONE);
                    favTag.setVisibility(View.GONE);
                    skillTag.setVisibility(View.GONE);
                    //cardFace.setImageBitmap(Common.decodedResource(R.mipmap.card_bg_small, 60, 90));

                } else {

                    final APIResponseParam.Item.Card haveParam = haveList.get(i);
                    CardParam cardParam = cardMapList.get(haveParam.card_id);

                    if (haveParam.id > maxSerialId) {
                        maxSerialId = haveParam.id;
                    }

                    if (haveParam.id > lastMaxSerialId) {
                        newTag.setVisibility(View.VISIBLE);
                    } else {
                        newTag.setVisibility(View.GONE);
                    }


                    //fav
                    favTag.setVisibility(favSerialList.contains(haveParam.id) ? View.VISIBLE : View.GONE);

                    //egara
                    cardFace.setImageBitmap(Common.decodedAssetBitmap("egara/180x254/" + cardParam.cardID + ".jpg", 60, 90, 0.5f));

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

                    final int listIndex = i;

                    //button
                    cardFace.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            SeManager.play(SeManager.SeName.PUSH_BUTTON);

                            //詳細画面プッシュ
                            pushDetailFragment(listIndex);
                        }
                    });
                    cardFace.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {

                            SeManager.play(SeManager.SeName.PUSH_BUTTON);

                            //詳細画面プッシュ
                            pushDetailFragment(listIndex);
                            return true;
                        }
                    });
                }
            }
        }
    }


    private void pushDetailFragment(Integer start) {

        CardShojiDetailFragment cardShojiDetailFragment = CardShojiDetailFragment.newInstance(start, null);
        ((CardFragment) getParentFragment()).addContentFragment(cardShojiDetailFragment);
    }

}

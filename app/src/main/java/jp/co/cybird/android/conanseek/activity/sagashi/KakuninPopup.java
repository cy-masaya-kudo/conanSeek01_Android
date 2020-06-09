package jp.co.cybird.android.conanseek.activity.sagashi;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import jp.co.cybird.android.conanseek.activity.card.CardDeckFragment;
import jp.co.cybird.android.conanseek.activity.card.CardFragment;
import jp.co.cybird.android.conanseek.common.ArrowButton;
import jp.co.cybird.android.conanseek.common.BaseActivity;
import jp.co.cybird.android.conanseek.common.BaseCell;
import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.manager.CacheManager;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.CsvManager;
import jp.co.cybird.android.conanseek.manager.SaveManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.manager.UserInfoManager;
import jp.co.cybird.android.conanseek.param.APIResponseParam;
import jp.co.cybird.android.conanseek.param.CardParam;
import jp.co.cybird.android.conanseek.param.JikenParam;
import jp.co.cybird.android.conanseek.param.NanidoParam;
import jp.souling.android.conanseek01.R;
import jp.souling.android.conanseek01.Settings;

/**
 * 探し確認
 */
public class KakuninPopup extends BasePopup implements View.OnClickListener {

    private ViewPager viewPager;
    private int totalPages;
    private int currentPage;
    private boolean noSoundSwipe = false;

    private TextView pageControl;

    private int inLevel;
    private int areaID;
    private String title;

    //訓練か事件か
    private boolean isKunren;

    //難易度パラメーター
    private NanidoParam nanidoParam;



    //所持カード一覧
    private ArrayList<APIResponseParam.Item.Card> myCardList;

    //カード情報
    private Map<Integer, CardParam> cardParamMap;

    private JikenParam jikenParam;


    public static KakuninPopup newinstance(int areaID, int inLevel, String title, boolean isKunren, JikenParam jikenParam) {

        KakuninPopup kakuninPopup = new KakuninPopup();

        Bundle bundle = new Bundle();
        bundle.putInt("area",areaID);
        bundle.putInt("inLevel", inLevel);
        bundle.putString("title", title);
        bundle.putBoolean("isKunren", isKunren);
        bundle.putSerializable("jikenParam", jikenParam);

        kakuninPopup.setArguments(bundle);

        return kakuninPopup;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view;

        if (isKunren) {
            view = inflater.inflate(R.layout.popup_sagashi_prev_kunren, container, false);
            CacheManager.instance().kunrenKakuninInLevel = inLevel;
            if (title != null)
                CacheManager.instance().kunrenKakuninTitle = title;
        }
        else {
            view = inflater.inflate(R.layout.popup_sagashi_prev_jiken, container, false);
        }


        Bundle args = getArguments();
        if (args != null) {
            this.areaID = args.getInt("area");
            this.inLevel = args.getInt("inLevel");
            this.title = args.getString("title", null);
            this.isKunren = args.getBoolean("isKunren", false);
            this.jikenParam = (JikenParam) args.getSerializable("jikenParam");
        }


        myCardList = UserInfoManager.myCardList();
        cardParamMap = CsvManager.cardParamsWithSkillDetail();


        //難易度パラメーター
        ArrayList<NanidoParam> nanidoList = CsvManager.nanidoList();
        for (NanidoParam param : nanidoList) {
            if (param.getArea() == areaID && param.getLevel() == inLevel) {
                nanidoParam = param;
                break;
            }
        }

        if (title == null) {
            Map<Integer, String> areaList = CsvManager.areaList();
            title = areaList.get(nanidoParam.getArea());
            title += "Lv." + nanidoParam.getLevel();

            if (isKunren)
                CacheManager.instance().kunrenKakuninTitle = title;
        }

        //ページビュー
        totalPages = Settings.totalDecks;
        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        currentPage = SaveManager.integerValue(SaveManager.KEY.CARD_DECK_IDX__integer, 0);

        //ページコントロール
        pageControl = (TextView) view.findViewById(R.id.pagecontrol);
        setMyPageAdapter();


        //title
        ((TextView) view.findViewById(R.id.dialog_title_text)).setText(title);

        //thumb
        String path = CsvManager.areaImageFileFromAreaID(areaID, false);
        ((ImageView) view.findViewById(R.id.thumb_mini)).setImageBitmap(Common.decodedAssetBitmap(path, 99, 56));
        ((ImageView) view.findViewById(R.id.image_dialog_bg)).setImageBitmap(Common.decodedAssetBitmap(path, 80, 80));


        //cost

        //time
        ((TextView) view.findViewById(R.id.time_label)).setText(Common.secondsToMinutes(nanidoParam.getTime()));

        //target
        ((TextView) view.findViewById(R.id.target_label)).setText("" + nanidoParam.getMono());

        //skill
        ((TextView) view.findViewById(R.id.skill_label)).setText("" + nanidoParam.getCard());

        //skills
        if (nanidoParam.getRotate() == 0)
            ((ImageView) view.findViewById(R.id.skill_direction)).setImageResource(R.mipmap.icon_none_stageselection);
        if (nanidoParam.getColor() == 0)
            ((ImageView) view.findViewById(R.id.skill_color)).setImageResource(R.mipmap.icon_none_stageselection);
        if (nanidoParam.getNumber() != 0)
            ((ImageView) view.findViewById(R.id.skill_order)).setImageResource(R.mipmap.icon_none_stageselection);


        //start
        view.findViewById(R.id.btn_start).setOnClickListener(this);

        //deck edit
        view.findViewById(R.id.btn_deck).setOnClickListener(this);

        //close
        view.findViewById(R.id.dialog_close).setOnClickListener(this);

        //arrow
        leftArrow = (ArrowButton) view.findViewById(R.id.arrowLeft);
        rightArrow = (ArrowButton) view.findViewById(R.id.arrowRight);
        leftArrow.setBlink(true);
        rightArrow.setBlink(true);
        leftArrow.setOnClickListener(this);
        rightArrow.setOnClickListener(this);

        return view;
    }


    @Override
    public void onClick(View v) {

        //閉じる
        if (v.getId() == R.id.dialog_close) {
            SeManager.play(SeManager.SeName.PUSH_BACK);
            removeMe();
        }
        //スタート
        else if (v.getId() == R.id.btn_start) {
            SeManager.play(SeManager.SeName.PUSH_BUTTON);
            if (buttonListener != null)
                buttonListener.pushedPositiveClick(KakuninPopup.this);

        }
        //デッキ編集
        else if (v.getId() == R.id.btn_deck) {

            SeManager.play(SeManager.SeName.PUSH_BUTTON);
            if (isKunren) {
                ((BaseActivity) getActivity()).replaceViewController(CardFragment.newInstance(CardDeckFragment.newInstance(), "kunren", null));
            } else {
                ((BaseActivity) getActivity()).replaceViewController(CardFragment.newInstance(CardDeckFragment.newInstance(), "jiken", jikenParam));
            }
        }
        else if (v.equals(leftArrow)) {
            viewPager.setCurrentItem(currentPage - 1);
        }
        else if (v.equals(rightArrow)) {
            viewPager.setCurrentItem(currentPage + 1);
        }
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
                SaveManager.updateInegerValue(SaveManager.KEY.CARD_DECK_IDX__integer, currentPage % totalPages);
                updatePageControl();

                if (!noSoundSwipe)
                    SeManager.play(SeManager.SeName.SWIPE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {

                    if (currentPage < totalPages || currentPage > totalPages * 2) {
                        noSoundSwipe = true;
                        viewPager.setCurrentItem(currentPage % totalPages + totalPages, false);
                        noSoundSwipe = false;
                    }
                }
            }
        });
        noSoundSwipe = true;
        viewPager.setCurrentItem(currentPage % totalPages + totalPages);
        noSoundSwipe = false;
    }


    private void updatePageControl() {

        String str = " ";
        for (int i = 0; i < totalPages; i++) {
            if (i == currentPage % Settings.totalDecks)
                str += "<font color='#98654a'>●</font> ";
            else
                str += "<font color='#d09a78'>●</font> ";
        }
        pageControl.setText(Html.fromHtml(str));
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
            bundle.putInt("position", position % totalPages);
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

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_sagashi_prev_kunren_cell, container, false);
        }


        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            Bundle args = getArguments();
            int deck_index = 0;
            if (args != null) {
                deck_index = args.getInt("position", 0) % totalPages;
            }

            //デッキデータ
            final ArrayList<Integer> deckList = SaveManager.deckListByDeckIndex(deck_index);

            //デッキ名
            ImageView deckTitle = (ImageView) view.findViewById(R.id.deck_title_image);
            int titleID = Common.myAppContext.getResources().getIdentifier("label_deck_" + (deck_index + 1), "mipmap", getActivity().getPackageName());
            deckTitle.setImageResource(titleID);


            for (int i = 0; i < 3; i++) {

                int identifier = Common.myAppContext.getResources().getIdentifier("list_cell_" + i, "id", getActivity().getPackageName());

                View cell = view.findViewById(identifier);

                ImageView skillImage = (ImageView) cell.findViewById(R.id.cell_skill);
                ImageView egaraImage = (ImageView) cell.findViewById(R.id.cell_egara);

                Integer serialID = deckList.get(i);

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

                        egaraImage.setImageBitmap(Common.decodedAssetBitmap("egara/326x460/" + cardID + ".jpg", 60, 85));

                        //スキル
                        switch (card.skillType) {

                            case Time:
                                skillImage.setImageResource(R.mipmap.skill_time);
                                break;
                            case Direction:
                                skillImage.setImageResource(R.mipmap.skill_direction);
                                break;
                            case Color:
                                skillImage.setImageResource(R.mipmap.skill_color);
                                break;
                            case Target:
                                skillImage.setImageResource(R.mipmap.skill_target);
                                break;
                            case Number:
                                skillImage.setImageResource(R.mipmap.skill_order);
                                break;
                        }
                    }

                } else {
                    skillImage.setVisibility(View.INVISIBLE);

                }

            }
        }
    }
}

package jp.co.cybird.android.conanseek.activity.gacha;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Map;

import jp.co.cybird.android.conanseek.activity.shop.CoinPopup;
import jp.co.cybird.android.conanseek.activity.top.HeaderMenuFragment;
import jp.co.cybird.android.conanseek.activity.top.TopFragment;
import jp.co.cybird.android.conanseek.common.ArrowButton;
import jp.co.cybird.android.conanseek.common.BaseActivity;
import jp.co.cybird.android.conanseek.common.BaseCell;
import jp.co.cybird.android.conanseek.common.BaseFragment;
import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.common.ImageGetTask;
import jp.co.cybird.android.conanseek.common.MessagePopup;
import jp.co.cybird.android.conanseek.manager.APIDialogFragment;
import jp.co.cybird.android.conanseek.manager.APIRequest;
import jp.co.cybird.android.conanseek.manager.BgmManager;
import jp.co.cybird.android.conanseek.manager.CacheManager;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.SaveManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.manager.UserInfoManager;
import jp.co.cybird.android.conanseek.param.APIResponseParam;
import jp.co.cybird.android.conanseek.param.TutorialParam;
import jp.souling.android.conanseek01.R;
import jp.souling.android.conanseek01.Settings;

public class GachaFragment extends BaseFragment {

    private ViewPager viewPager;
    private int totalPages;
    private int currentPage;
    private boolean noSoundSwipe = false;

    private ArrowButton leftArrow;
    private ArrowButton rightArrow;

    private ArrayList<APIResponseParam.Item.GachaParam> sourceArray;

    //カードガチャで選ぶカード一覧
    ArrayList<Integer> cardGachaSerialList;


    public static GachaFragment newInstance(ArrayList<Integer> cardGachaSerialList) {

        Bundle args = new Bundle();
        args.putIntegerArrayList("cardGachaSerialList", cardGachaSerialList);

        GachaFragment fragment = new GachaFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static GachaFragment newInstance() {
        return newInstance(null);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_gacha, container, false);

        Bundle arg = getArguments();
        if (arg != null) {
            this.cardGachaSerialList = arg.getIntegerArrayList("cardGachaSerialList");
        }
        sourceArray = CacheManager.instance().gachaParamArrayList;

        bgmName = BgmManager.BgmName.GACHA;

        //ページビュー
        viewPager = (ViewPager) view.findViewById(R.id.viewPager);

        leftArrow = (ArrowButton) view.findViewById(R.id.arrowLeft);
        rightArrow = (ArrowButton) view.findViewById(R.id.arrowRight);
        leftArrow.setBlink(false);
        rightArrow.setBlink(false);
        rightArrow.setOnClickListener(arrowClickListener);
        leftArrow.setOnClickListener(arrowClickListener);

        currentPage = CacheManager.instance().gachaSelectedIndex;

        if (cardGachaSerialList != null) {

            CardGachaPopup cardGachaPopup = CardGachaPopup.newInstance(
                    sourceArray.get(currentPage),
                    cardGachaSerialList
            );
            cardGachaPopup.noShowAnimation = true;
            cardGachaPopup.showSound = null;
            showPopup(cardGachaPopup);
        }

        return view;
    }


    View.OnClickListener arrowClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.equals(leftArrow)) {
                if (currentPage > 0) {
                    viewPager.setCurrentItem(currentPage - 1);
                }
            } else {
                if (currentPage < totalPages * 3 - 1) {
                    viewPager.setCurrentItem(currentPage + 1);
                }
            }
        }
    };

    @Override
    protected void fragmentDidAppear() {
        super.fragmentDidAppear();

        //カードガチャ選択帰り
        if (cardGachaSerialList != null) {
            totalPages = sourceArray.size();
            setMyPageAdapter();
        }
        //通常表示
        else {
            //ガチャ情報をAPIから得る
            updateGachaData();
        }
    }


    /**
     * ガチャ情報更新
     */
    private void updateGachaData() {

        sourceArray = new ArrayList<>();


        APIRequest request = new APIRequest();
        request.name = APIDialogFragment.APIName.GACHA_LIST;

        APIDialogFragment f = APIDialogFragment.newInstance(request);

        f.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
            @Override
            public void getAPIResult(APIRequest request, Object object) {

                APIResponseParam param = (APIResponseParam) object;
                sourceArray = param.item.gacha_list;
                CacheManager.instance().gachaParamArrayList = sourceArray;

                totalPages = sourceArray.size();


                APIResponseParam.Item.GachaParam gachaParam = sourceArray.get(0);

                if (CacheManager.instance().gachaFirstDelivery == gachaParam.delivery_id) {
                    currentPage = CacheManager.instance().gachaCurrentPage;
                }
                CacheManager.instance().gachaFirstDelivery = gachaParam.delivery_id;

                setMyPageAdapter();

                ArrayList<String> tutorialList = SaveManager.stringArray(SaveManager.KEY.TUTORIAL__stringList);
                if (!tutorialList.contains("gacha_1")) {
                    startTutorial("gacha_1");
                } else if (tutorialList.contains("gacha_2") && !tutorialList.contains("gacha_3")) {
                    startTutorial("gacha_3");
                }


            }

        });
        fireApi(f);
    }


    //---tutorial

    @Override
    public void didEndTutorial() {
        super.didEndTutorial();
    }

    @Override
    public void pushedTarget(TutorialParam param) {
        super.pushedTarget(param);

        if (param.sousaCode.indexOf("ガチャボタン") != -1) {

            SeManager.play(SeManager.SeName.PUSH_BUTTON);

            final APIResponseParam.Item.GachaParam gachaParam = sourceArray.get(0);

            int currentTicket = UserInfoManager.ticketCount();

            String message = "<img src=\"icon_ticket\">チケット1枚使用します。<br/>";
            message += "<img src=\"icon_ticket\">" + currentTicket + " → <img src=\"icon_ticket\">" + (currentTicket - 1) + "<br/>";
            message += "よろしいですか？";

            MessagePopup messagePopup = MessagePopup.newInstance(message, "いいえ", "はい");
            messagePopup.setPopupButtonListener(new BasePopup.PopupButtonListener() {
                @Override
                public void pushedPositiveClick(BasePopup popup) {

                    //チケットガチャ
                    APIRequest request = new APIRequest();
                    request.name = APIDialogFragment.APIName.GACHA_FIRE;
                    request.params.put("proc_id", getString(R.string.api_proc_gacha_ticket_one));
                    request.params.put("delivery_id", String.valueOf(gachaParam.delivery_id));
                    APIDialogFragment api = APIDialogFragment.newInstance(request);
                    api.setAPIDialogListener(fireDialogListener);
                    fireApi(api);
                }

                @Override
                public void pushedNegativeClick(BasePopup popup) {

                }
            });
            showPopup(messagePopup);
        } else if (param.sousaCode.indexOf("TOPボタン") != -1) {
            SeManager.play(SeManager.SeName.PUSH_BUTTON);
            ((BaseActivity) getActivity()).replaceViewController(TopFragment.newInstance());
        }
    }

    //--------


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
                if (!noSoundSwipe) {
                    SeManager.play(SeManager.SeName.SWIPE);
                    CacheManager.instance().gachaCurrentPage = currentPage;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

                if (state == ViewPager.SCROLL_STATE_IDLE) {

                    if (currentPage < totalPages || currentPage > totalPages * 2) {
                        noSoundSwipe = true;
                        viewPager.setCurrentItem(currentPage % totalPages + totalPages, false);
                        noSoundSwipe = false;
                    }
                    leftArrow.setBlink(true);
                    rightArrow.setBlink(true);
                }
            }
        });
        noSoundSwipe = true;
        viewPager.setCurrentItem(totalPages + currentPage, false);
        noSoundSwipe = false;

        if (totalPages > 0) {
            leftArrow.setBlink(true);
            rightArrow.setBlink(true);
        }
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
            bundle.putSerializable("data", sourceArray.get(position % totalPages));
            bundle.putInt("gachaIndex", position % totalPages);

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

        ImageView visualImage;
        APIResponseParam.Item.GachaParam gachaParam;
        String cacheDir;


        @Override
        public void onDestroy() {
            super.onDestroy();
            visualImage.setImageDrawable(null);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.table_gacha_list, container, false);
        }


        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            visualImage = (ImageView) view.findViewById(R.id.visual_image);

            Bundle arg = getArguments();
            if (arg != null) {
                gachaParam = (APIResponseParam.Item.GachaParam) arg.getSerializable("data");
            }

            //クリエイティブイメージ
            String filePath = getString(R.string.api) + "creative/" + gachaParam.image_file + ".png";
            cacheDir = Common.myAppContext.getCacheDir() + "";
            String cacheFile = "gacha__" + gachaParam.image_file + ".png";

            Bitmap bitmap = Common.cachedBitmap(cacheFile);
            if (bitmap != null) {
                visualImage.setImageBitmap(bitmap);
            } else {
                ImageGetTask task = new ImageGetTask(visualImage, cacheDir, cacheFile);
                task.execute(filePath);
            }


            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    SeManager.play(SeManager.SeName.PUSH_BUTTON);


                    //カードガチャ
                    if (gachaParam.delivery_type.equals("カード")) {

                        showPopup(CardGachaPopup.newInstance(gachaParam, null));

                        Bundle arg = getArguments();
                        if (arg != null) {
                            CacheManager.instance().gachaSelectedIndex = arg.getInt("gachaIndex");
                        }

                    }
                    //通貨消費ガチャ
                    else {

                        //限定ガチャ：残り枚数次第では回せない
                        if (gachaParam.delivery_type.equals("ボックス")) {

                            String labels[] = new String[5];
                            labels[0] = "SSR";
                            labels[1] = "SR";
                            labels[2] = "R";
                            labels[3] = "HN";
                            labels[4] = "N";
                            Map<String, Integer> totalData = gachaParam.display.total_amount;
                            Map<String, Integer> gottenData = gachaParam.display.total_gotten;

                            int gottenCount = 0;
                            int totalCount = 0;
                            for (int i = 0; i < 5; i++) {
                                String slug = labels[i];
                                if (gottenData.containsKey(slug)) {
                                    gottenCount += gottenData.get(slug);
                                    totalCount += totalData.get(slug);
                                }
                            }

                            //全部所得済
                            if (totalCount - gottenCount < 1) {

                                showPopup(MessagePopup.newInstance(
                                        "おめでとう！<br>このガチャの<img src=\"icon_card\">カードをコンプリートしたよ！", null, null
                                ));
                                return;
                            }
                            //10連では無理
                            else if (v.getId() == R.id.gacha_fire_ten_button && totalCount - gottenCount < 10) {

                                showPopup(MessagePopup.newInstance(
                                        "このガチャの<img src=\"icon_card\">カードが残り10枚を切ったよ！<br>"
                                                + "コンプリートまであと少し！<br>"
                                                + "1回ずつガチャろう！",
                                        null, null
                                ));
                                return;
                            }

                        }

                        boolean canFireCoin = false;
                        boolean canFireTicket = false;
                        boolean tenFlag = false;

                        int coinCost = 5;
                        int ticketCost = 1;

                        int currentCoin = UserInfoManager.coinCount();
                        int currentTicket = UserInfoManager.ticketCount();


                        //one
                        if (v.getId() == R.id.gacha_fire_one_button) {
                        }
                        //ten
                        else if (v.getId() == R.id.gacha_fire_ten_button) {
                            coinCost *= 10;
                            ticketCost *= 10;
                            tenFlag = true;
                        }

                        //ガチャれるか
                        canFireCoin = coinCost <= currentCoin;
                        canFireTicket = ticketCost <= currentTicket;

                        //チケット使えるのはレアガチャのみ
                        if (!gachaParam.delivery_type.equals("レア")) {
                            canFireTicket = false;
                        }

                        //チケットでガチャれる
                        if (canFireTicket) {

                            final String procID = getString(tenFlag ? R.string.api_proc_gacha_ticket_ten : R.string.api_proc_gacha_ticket_one);

                            String message = "<img src=\"icon_ticket\">チケット" + ticketCost + "枚使用します。<br/>";
                            message += "<img src=\"icon_ticket\">" + currentTicket + " → <img src=\"icon_ticket\">" + (currentTicket - ticketCost) + "<br/>";
                            message += "よろしいですか？";

                            MessagePopup messagePopup = MessagePopup.newInstance(message, "いいえ", "はい");
                            messagePopup.setPopupButtonListener(new BasePopup.PopupButtonListener() {
                                @Override
                                public void pushedPositiveClick(BasePopup popup) {

                                    //チケットガチャ
                                    APIRequest request = new APIRequest();
                                    request.name = APIDialogFragment.APIName.GACHA_FIRE;
                                    request.params.put("proc_id", procID);
                                    request.params.put("delivery_id", String.valueOf(gachaParam.delivery_id));
                                    APIDialogFragment api = APIDialogFragment.newInstance(request);
                                    api.setAPIDialogListener(fireDialogListener);
                                    fireApi(api);
                                }

                                @Override
                                public void pushedNegativeClick(BasePopup popup) {

                                }
                            });
                            showPopup(messagePopup);
                        }
                        //コインでガチャれる
                        else if (canFireCoin) {

                            final String procID = getString(tenFlag ? R.string.api_proc_gacha_coin_ten : R.string.api_proc_gacha_coin_one);

                            String message = "<img src=\"icon_coin\">コインを" + coinCost + "枚使用します。<br/>";
                            message += "<img src=\"icon_coin\">" + currentCoin + " → <img src=\"icon_coin\">" + (currentCoin - coinCost) + "<br/>";
                            message += "よろしいですか？";

                            MessagePopup messagePopup = MessagePopup.newInstance(message, "いいえ", "はい");
                            messagePopup.setPopupButtonListener(new BasePopup.PopupButtonListener() {
                                @Override
                                public void pushedPositiveClick(BasePopup popup) {

                                    //コインガチャ
                                    APIRequest request = new APIRequest();
                                    request.name = APIDialogFragment.APIName.GACHA_FIRE;
                                    request.params.put("proc_id", procID);
                                    request.params.put("delivery_id", String.valueOf(gachaParam.delivery_id));
                                    APIDialogFragment api = APIDialogFragment.newInstance(request);
                                    api.setAPIDialogListener(fireDialogListener);
                                    fireApi(api);
                                }

                                @Override
                                public void pushedNegativeClick(BasePopup popup) {

                                }
                            });
                            showPopup(messagePopup);

                        }
                        //コストに届かない
                        else {

                            String message = "<img src=\"icon_coin\">コインが足りません。<br/>";
                            message += "<img src=\"icon_coin\">コインを購入しますか？";

                            MessagePopup messagePopup = MessagePopup.newInstance(message, "いいえ", "はい");
                            messagePopup.positiveNoClose = true;
                            messagePopup.setPopupButtonListener(new BasePopup.PopupButtonListener() {
                                @Override
                                public void pushedPositiveClick(BasePopup popup) {
                                    showPopup(CoinPopup.newInstance());
                                }

                                @Override
                                public void pushedNegativeClick(BasePopup popup) {

                                }
                            });
                            showPopup(messagePopup);
                        }
                    }
                }
            };

            view.findViewById(R.id.gacha_fire_one_button).

                    setOnClickListener(listener);

            view.findViewById(R.id.gacha_fire_ten_button).

                    setOnClickListener(listener);

            view.findViewById(R.id.gacha_shutsugen_button).

                    setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               SeManager.play(SeManager.SeName.PUSH_BUTTON);
                                               ShutsugenPopup shutsugenPopup = ShutsugenPopup.newInstance(gachaParam);
                                               showPopup(shutsugenPopup);
                                           }
                                       }

                    );

            ImageView oneImage = (ImageView) view.findViewById(R.id.gacha_fire_one_note);
            ImageView tenImage = (ImageView) view.findViewById(R.id.gacha_fire_ten_note);
            ImageView noticeImage = (ImageView) view.findViewById(R.id.gacha_ticket_notice);

            //ガチャタイプ:レア
            if (gachaParam.delivery_type.equals("レア"))

            {

                oneImage.setImageResource(R.mipmap.btn_gacha_desc);
                tenImage.setImageResource(R.mipmap.btn_gacha_10ren_desc);
                noticeImage.setVisibility(View.VISIBLE);
            }
            //ガチャタイプ:ボックス
            else if (gachaParam.delivery_type.equals("ボックス"))

            {
                noticeImage.setVisibility(View.INVISIBLE);
                oneImage.setImageResource(R.mipmap.btn_gacha_desc_2);
                tenImage.setImageResource(R.mipmap.btn_gacha_10ren_desc_2);

            }
            //ガチャタイプ:カード
            else if (gachaParam.delivery_type.equals("カード"))

            {
                //10連なし
                View tenView = view.findViewById(R.id.gacha_fire_ten);
                tenView.setVisibility(View.GONE);


                oneImage.setImageResource(R.mipmap.btn_gacha_card_desc);
                noticeImage.setVisibility(View.INVISIBLE);
            }
        }
    }


    public APIDialogFragment.APIDialogListener fireDialogListener = new APIDialogFragment.APIDialogListener() {
        @Override
        public void getAPIResult(APIRequest request, Object object) {

            //今持ってるカードの中に含まれないカードをお気に入り、デッキから削除

            ArrayList<APIResponseParam.Item.Card> myCardList = UserInfoManager.myCardList();
            ArrayList<Integer> mySerialList = new ArrayList<>();

            for (APIResponseParam.Item.Card card : myCardList) {
                mySerialList.add(card.id);
            }

            //お気に入り
            ArrayList<Integer> favList = SaveManager.integerList(SaveManager.KEY.CARD_FAV__integerList);
            ArrayList<Integer> saveFavList = new ArrayList<>();
            for (Integer val : favList) {
                if (mySerialList.contains(val)) {
                    saveFavList.add(val);
                }
            }
            SaveManager.updateIntegerList(SaveManager.KEY.CARD_FAV__integerList, saveFavList);

            //デッキ
            ArrayList<Integer> deckList = SaveManager.integerList(SaveManager.KEY.CARD_DECK__integerList);
            for (int i = 0; i < Settings.totalDecks * 3; i++) {
                if (deckList.size() > i) {
                    if (!mySerialList.contains(deckList.get(i).intValue())) {
                        deckList.set(i, 0);
                    }
                }
            }
            SaveManager.updateIntegerList(SaveManager.KEY.CARD_DECK__integerList, deckList);


            updateMyHeaderStatus();

            APIResponseParam param = (APIResponseParam) object;

            ArrayList<Integer> list = new ArrayList<>();
            for (APIResponseParam.Item.Card card : param.item.get_card) {
                list.add(card.id);
            }


            //エフェクト
            ((BaseActivity) getActivity()).replaceViewController(GachaEffectFragment.newInstance(param.item.get_card));

        }
    };
}
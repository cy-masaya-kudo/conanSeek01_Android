package jp.co.cybird.android.conanseek.activity.top;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

import jp.co.cybird.android.conanseek.common.BaseActivity;
import jp.co.cybird.android.conanseek.common.BaseButton;
import jp.co.cybird.android.conanseek.common.BaseCell;
import jp.co.cybird.android.conanseek.common.BaseFragment;
import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.common.MessagePopup;
import jp.co.cybird.android.conanseek.manager.APIDialogFragment;
import jp.co.cybird.android.conanseek.manager.APIRequest;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.CsvManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.manager.UserInfoManager;
import jp.co.cybird.android.conanseek.param.APIResponseParam;
import jp.co.cybird.android.conanseek.param.CardParam;
import jp.souling.android.conanseek01.R;

/**
 * プレゼントボックス
 */
public class PresentBoxPopup extends BasePopup {

    private ArrayList<APIResponseParam.Item.PresentParam> present_list;

    private ViewPager viewPager;
    private int totalPages;
    private int currentPage;

    private TextView pageControl;

    private TextView noitemText;
    private BaseButton ikkatsuButton;

    private static final int CELL_ITEM_COUNT = 6;


    public static PresentBoxPopup newInstance() {
        
        Bundle args = new Bundle();
        
        PresentBoxPopup fragment = new PresentBoxPopup();
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popup_present, container, false);

        present_list = UserInfoManager.responseParam().item.present_list;


        //プレゼントはありません
        noitemText = (TextView) view.findViewById(R.id.noitem_text);

        //ページビュー
        viewPager = (ViewPager) view.findViewById(R.id.card_view_pager);
        totalPages = 0;
        currentPage = 0;

        //ページコントロール
        pageControl = (TextView) view.findViewById(R.id.pagecontrol);


        view.findViewById(R.id.dialog_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeManager.play(SeManager.SeName.PUSH_BACK);
                if (buttonListener != null)
                    buttonListener.pushedNegativeClick(PresentBoxPopup.this);
                removeMe();
            }
        });

        ikkatsuButton = (BaseButton) view.findViewById(R.id.btn_ikkatsu);
        ikkatsuButton.setVisibility(View.GONE);
        ikkatsuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SeManager.play(SeManager.SeName.PUSH_BUTTON);

                int addCoin = 0;
                int addMegane = 0;
                int addHeart = 0;
                int addTicket = 0;
                int addCard = 0;
                ArrayList<String> presentIdList = new ArrayList<String>();

                for (APIResponseParam.Item.PresentParam param : present_list) {

                    if (param.fuyo_key.equals("coin"))
                        addCoin += param.fuyo_value;
                    else if (param.fuyo_key.equals("megane"))
                        addMegane += param.fuyo_value;
                    else if (param.fuyo_key.equals("heart"))
                        addHeart += param.fuyo_value;
                    else if (param.fuyo_key.equals("ticket"))
                        addTicket += param.fuyo_value;
                    else if (param.fuyo_key.equals("card"))
                        addCoin += 1;

                    presentIdList.add(String.valueOf(param.present_id));
                }

                if (addCoin > 0 && !detectItemJougen("coin", addCoin)) return;
                if (addMegane > 0 && !detectItemJougen("megane", addMegane)) return;
                if (addHeart > 0 && !detectItemJougen("heart", addHeart)) return;
                if (addTicket > 0 && !detectItemJougen("ticket", addTicket)) return;
                if (addCard > 0 && !detectItemJougen("card", addCard)) return;

                String ids = TextUtils.join(",", presentIdList);

                firePresent(ids);
            }
        });

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        updatePresentList();
    }


    private void setMyPageAdapter() {

        totalPages = (int) (Math.ceil((float) present_list.size() / CELL_ITEM_COUNT));

        viewPager.clearOnPageChangeListeners();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                updatePageControl();
                SeManager.play(SeManager.SeName.SWIPE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        viewPager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));
    }


    private void updatePageControl() {

        String str = " ";
        for (int i = 0; i < totalPages; i++) {
            if (i == currentPage)
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
            return inflater.inflate(R.layout.table_present, container, false);
        }


        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            Bundle args = getArguments();
            int position = 0;
            if (args != null) {
                position = args.getInt("position", 0);
            }

            for (int i = 0; i < CELL_ITEM_COUNT; i++) {

                int identifier = Common.myAppContext.getResources().getIdentifier("list_cell_" + (i + 1), "id", getActivity().getPackageName());
                ViewGroup cell = (ViewGroup) view.findViewById(identifier);

                BaseButton cellButton = (BaseButton) cell.findViewById(R.id.cell_bg_button);
                FrameLayout cellContainer = (FrameLayout) cell.findViewById(R.id.cell_container);

                int index = position * CELL_ITEM_COUNT + i;

                if (index < present_list.size()) {

                    //param
                    final APIResponseParam.Item.PresentParam presentParam = present_list.get(index);

                    //cell
                    cell.setVisibility(View.VISIBLE);

                    ImageView iconImage = (ImageView) cell.findViewById(R.id.cell_icon);
                    TextView titleText = (TextView) cell.findViewById(R.id.cell_title);
                    TextView detailText = (TextView) cell.findViewById(R.id.cell_detail);
                    TextView dateText = (TextView) cell.findViewById(R.id.cell_date);

                    String detailString = "";

                    int cardID = 0;
                    int value = presentParam.fuyo_value;

                    //ガチャチケット
                    if (presentParam.fuyo_key.equals("ticket")) {
                        iconImage.setImageResource(R.mipmap.icon_ticket);
                        detailString = "ガチャチケット";
                    }
                    //虫眼鏡
                    else if (presentParam.fuyo_key.equals("megane")) {
                        iconImage.setImageResource(R.mipmap.icon_megane);
                        detailString = "虫眼鏡";
                    }
                    //ハート
                    else if (presentParam.fuyo_key.equals("heart")) {
                        iconImage.setImageResource(R.mipmap.icon_heart);
                        detailString = "ハート全回復";
                    }
                    //コイン
                    else if (presentParam.fuyo_key.equals("coin")) {
                        iconImage.setImageResource(R.mipmap.icon_coin);
                        detailString = "コイン";
                    }
                    //カード
                    else if (presentParam.fuyo_key.equals("card")) {
                        iconImage.setImageResource(R.mipmap.icon_card);
                        cardID = value;
                        if (cardID > 0) {
                            CardParam cardParam = CsvManager.cardByCardID(cardID);
                            detailString = cardParam.rareString + " " + cardParam.name;
                            value = 1;
                        } else {
                            detailString = "カード";
                        }
                    }

                    //title
                    if (presentParam.present_type.equals("login"))
                        titleText.setText("ログインボーナス");
                    else if (presentParam.present_type.equals("owabi"))
                        titleText.setText("お詫び");
                    else if (presentParam.present_type.equals("tokute"))
                        titleText.setText("特典");
                    else if (presentParam.present_type.equals("jiken"))
                        titleText.setText("事件報酬");
                    else if (presentParam.present_type.equals("kunren"))
                        titleText.setText("訓練報酬");
                    else if (presentParam.present_type.equals("present"))
                        titleText.setText("プレゼント");

                    //detail
                    detailText.setText(
                            detailString + " x " + value
                    );


                    //date
                    if (presentParam.kigen_nokori == 0) {
                        dateText.setText("本日まで");
                    } else {
                        dateText.setText("あと" + presentParam.kigen_nokori + "日");
                    }

                    cellButton.setImageResource(R.mipmap.bg);
                    cellButton.setEnabled(true);

                    final int finalValue = value;
                    cellButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            SeManager.play(SeManager.SeName.PUSH_BUTTON);

                            //所有数上限確認
                            if (detectItemJougen(presentParam.fuyo_key, finalValue)) {

                                firePresent(String.valueOf(presentParam.present_id));
                            }

                        }
                    });

                } else {

                    cellContainer.setVisibility(View.GONE);
                    cellButton.setImageResource(R.mipmap.bg_blank);
                    cellButton.setEnabled(false);
                }
            }
        }
    }


    //------------------ API


    /**
     * 一覧更新
     */
    private void updatePresentList() {

        //一覧取得
        APIRequest request = new APIRequest();
        request.name = APIDialogFragment.APIName.PRESENT_LIST;

        APIDialogFragment api = APIDialogFragment.newInstance(request);
        api.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
            @Override
            public void getAPIResult(APIRequest request, Object object) {
                APIResponseParam param = (APIResponseParam) object;
                present_list = param.item.present_list;

                Common.logD("present_list.size()" + present_list.size());

                if (present_list.size() > 0) {
                    noitemText.setVisibility(View.GONE);
                    ikkatsuButton.setVisibility(View.VISIBLE);

                } else {
                    noitemText.setVisibility(View.VISIBLE);
                    ikkatsuButton.setVisibility(View.GONE);
                }

                currentPage = 0;
                setMyPageAdapter();
                updatePageControl();
            }
        });
        fireApiFromPopup(api);
    }

    /**
     * 所有数上限確認
     */
    private boolean detectItemJougen(String fuyo_key, int fuyo_value) {

        //所有数上限確認
        if (fuyo_key.equals("ticket")) {
            if (UserInfoManager.ticketCount() + fuyo_value > 99999) {
                showPopupFromPopup(MessagePopup.newInstance("これ以上<img src=\"icon_ticket\">ガチャチケットを持てません", null, null));
                return false;
            }
        }
        if (fuyo_key.equals("megane")) {
            if (UserInfoManager.meganeCount() + fuyo_value > 99999) {
                showPopupFromPopup(MessagePopup.newInstance("これ以上<img src=\"icon_megane\">虫眼鏡を持てません", null, null));
                return false;
            }
        }
        if (fuyo_key.equals("coin")) {
            if (UserInfoManager.coinCount() + fuyo_value > 99999) {
                showPopupFromPopup(MessagePopup.newInstance("これ以上<img src=\"icon_coin\">コインを持てません", null, null));
                return false;
            }
        }
        if (fuyo_key.equals("heart")) {
            if (UserInfoManager.heartCount() == 10) {
                showPopupFromPopup(MessagePopup.newInstance("これ以上<img src=\"icon_heart\">ハートを持てません", null, null));
                return false;
            }
        }
        if (fuyo_key.equals("card")) {
            if (UserInfoManager.myCardList().size() + fuyo_value == 2000) {
                showPopupFromPopup(MessagePopup.newInstance("これ以上<img src=\"icon_card\">カードを持てません", null, null));
                return false;
            }
        }

        return true;
    }

    /**
     * プレゼント取得
     */
    private void firePresent(String present_id_string) {

        //API送信
        APIRequest request = new APIRequest();
        request.name = APIDialogFragment.APIName.PRESENT_FIRE;
        request.params.put("present_id", present_id_string);

        APIDialogFragment api = APIDialogFragment.newInstance(request);
        api.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
            @Override
            public void getAPIResult(APIRequest request, Object object) {

                //result付きのuserInfoを得られる
                APIResponseParam param = (APIResponseParam) object;

                String jsonString = param.item.result.toString();
                APIResponseParam.Item.PresentFireResult json = new Gson().fromJson(jsonString, APIResponseParam.Item.PresentFireResult.class);

                String resultMessage = "";

                if (json.megane > 0)
                    resultMessage += "<img src=\"icon_megane\">虫眼鏡 x " + json.megane + "<br>";
                if (json.ticket > 0)
                    resultMessage += "<img src=\"icon_ticket\">ガチャチケット x " + json.ticket + "<br>";
                if (json.card > 0)
                    resultMessage += "<img src=\"icon_card\">カード x " + json.card + "<br>";
                if (json.coin > 0)
                    resultMessage += "<img src=\"icon_coin\">コイン x " + json.coin + "<br>";
                if (json.heart > 0)
                    resultMessage += "<img src=\"icon_heart\">ハート全回復" + "<br>";

                resultMessage += "を受け取りました。";

                MessagePopup messagePopup = MessagePopup.newInstance(resultMessage, null, null);
                messagePopup.setPopupButtonListener(new PopupButtonListener() {
                    @Override
                    public void pushedPositiveClick(BasePopup popup) {
                    }

                    @Override
                    public void pushedNegativeClick(BasePopup popup) {

                        ((BaseFragment) getParentFragment()).updateMyHeaderStatus();
                        updatePresentList();
                    }
                });
                showPopupFromPopup(messagePopup);
            }
        });
        fireApiFromPopup(api);

    }
}

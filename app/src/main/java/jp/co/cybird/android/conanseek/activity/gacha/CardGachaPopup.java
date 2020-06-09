package jp.co.cybird.android.conanseek.activity.gacha;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import jp.co.cybird.android.conanseek.activity.card.CardFragment;
import jp.co.cybird.android.conanseek.activity.card.CardSelectFragment;
import jp.co.cybird.android.conanseek.common.BaseActivity;
import jp.co.cybird.android.conanseek.common.BaseButton;
import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.common.MessagePopup;
import jp.co.cybird.android.conanseek.manager.APIDialogFragment;
import jp.co.cybird.android.conanseek.manager.APIRequest;
import jp.co.cybird.android.conanseek.manager.CacheManager;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.CsvManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.manager.UserInfoManager;
import jp.co.cybird.android.conanseek.param.APIResponseParam;
import jp.co.cybird.android.conanseek.param.CardParam;
import jp.souling.android.conanseek01.R;

/**
 * カードガチャ
 */
public class CardGachaPopup extends BasePopup implements View.OnClickListener {

    private APIResponseParam.Item.GachaParam gachaParam;
    private ArrayList<Integer> cardSerialList;

    private BaseButton cardButtons[] = new BaseButton[5];
    private BaseButton matometeButton;
    private BaseButton gacharuButton;

    public static CardGachaPopup newInstance(APIResponseParam.Item.GachaParam gachaParam, ArrayList<Integer> cardSerialList) {

        Bundle args = new Bundle();

        args.putSerializable("gachaParam", gachaParam);
        args.putIntegerArrayList("cardSerialList", cardSerialList);

        CardGachaPopup fragment = new CardGachaPopup();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.popup_cardgacha, container, false);

        Bundle arg = getArguments();
        if (arg != null) {
            gachaParam = (APIResponseParam.Item.GachaParam) arg.getSerializable("gachaParam");
            cardSerialList = arg.getIntegerArrayList("cardSerialList");
        }

        ArrayList<APIResponseParam.Item.Card> myCardList = UserInfoManager.myCardList();

        if (cardSerialList == null) {
            cardSerialList = new ArrayList<>();
            cardSerialList.add(0);
            cardSerialList.add(0);
            cardSerialList.add(0);
            cardSerialList.add(0);
            cardSerialList.add(0);
        }

        for (int i = 0; i < 5; i++) {
            int identifire = Common.myAppContext.getResources().getIdentifier("list_cell_" + (i + 1), "id", Common.myAppContext.getPackageName());
            cardButtons[i] = (BaseButton) view.findViewById(identifire);
            cardButtons[i].setOnClickListener(this);

            if (cardSerialList != null && cardSerialList.size() > i) {
                int serialID = cardSerialList.get(i);
                if (serialID > 0) {
                    for (APIResponseParam.Item.Card card : myCardList) {
                        if (card.id == serialID) {
                            cardButtons[i].setImageBitmap(Common.decodedAssetBitmap("egara/326x460/" + card.card_id + ".jpg", 80, 100));
                            break;
                        }
                    }
                }
            }
        }

        //80 110 4

        matometeButton = (BaseButton) view.findViewById(R.id.dialog_left);
        gacharuButton = (BaseButton) view.findViewById(R.id.dialog_right);
        gacharuButton.enableAlpha = true;
        gacharuButton.setEnabled(false);

        //ガチャるボタン押せる条件
        if (cardSerialList != null && cardSerialList.size() == 5 && cardSerialList.indexOf(0) == -1) {
            gacharuButton.setEnabled(true);
        }


        matometeButton.setOnClickListener(this);
        gacharuButton.setOnClickListener(this);

        view.findViewById(R.id.dialog_close).setOnClickListener(this);


        return view;
    }


    @Override
    public void onClick(View v) {

        //閉じる
        if (v.getId() == R.id.dialog_close) {
            SeManager.play(SeManager.SeName.PUSH_BACK);
            CacheManager.instance().gachaParamArrayList = null;
            removeMe();
        }
        //カードまとめて選択
        else if (v.equals(matometeButton)) {
            SeManager.play(SeManager.SeName.PUSH_BUTTON);
            switchCardActivity(-1);
        }
        //ガチャ実行
        else if (v.equals(gacharuButton)) {
            SeManager.play(SeManager.SeName.PUSH_BUTTON);

            //--選んだカードの妥当性

            //R以上のカードが含まれている
            boolean rareCardFukumu = false;

            ArrayList<APIResponseParam.Item.Card> myCardList = UserInfoManager.myCardList();

            for (int serialID : cardSerialList) {
                for (APIResponseParam.Item.Card card : myCardList) {
                    if (card.id == serialID) {
                        CardParam cardParam = CsvManager.cardByCardID(card.card_id);
                        if (cardParam.rareInt > 1) {
                            rareCardFukumu = true;
                        }
                        break;
                    }
                }
                if (rareCardFukumu) break;
            }

            if (rareCardFukumu) {

                MessagePopup messagePopup = MessagePopup.newInstance(
                        "R以上の<img src=\"icon_card\">カードが選択されています。<br>よろしいでしょうか？",
                        "いいえ", "はい"
                );
                messagePopup.setPopupButtonListener(new PopupButtonListener() {
                    @Override
                    public void pushedPositiveClick(BasePopup popup) {
                        fireGacha();
                    }

                    @Override
                    public void pushedNegativeClick(BasePopup popup) {

                    }
                });
                showPopupFromPopup(messagePopup);

            } else {
                fireGacha();
            }

        }
        //各カード枠
        else {

            SeManager.play(SeManager.SeName.PUSH_BUTTON);
            for (int i = 0; i < 5; i++) {
                if (v.equals(cardButtons[i])) {
                    switchCardActivity(i);
                }
            }
        }
    }

    /**
     * カードアクティビティ遷移
     */
    private void switchCardActivity(int targetFrameIndex) {

        //カードガチャ選択でカードフラグメントに行く
        CardSelectFragment cardSelectFragment = CardSelectFragment.newInstance(
                targetFrameIndex == -1,
                cardSerialList,
                targetFrameIndex == -1 ? 0 : targetFrameIndex
        );
        ((BaseActivity) getActivity()).replaceViewController(CardFragment.newInstance(cardSelectFragment, "gacha", null));

    }


    /**
     * ガチャ実行
     */
    private void fireGacha() {

        String cardIDs = "";
        for (int serialID : cardSerialList) {
            if (cardIDs.length() > 0) cardIDs += ",";
            cardIDs += serialID;
        }

        APIRequest request = new APIRequest();
        request.name = APIDialogFragment.APIName.GACHA_FIRE;
        request.params.put("proc_id", getString(R.string.api_proc_gacha_card));
        request.params.put("delivery_id", String.valueOf(gachaParam.delivery_id));
        request.params.put("tsukau_card", cardIDs);
        APIDialogFragment api = APIDialogFragment.newInstance(request);
        api.setAPIDialogListener(((GachaFragment) getParentFragment()).fireDialogListener);
        fireApiFromPopup(api);
    }
}

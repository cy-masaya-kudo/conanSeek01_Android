package jp.co.cybird.android.conanseek.activity.jiken;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import jp.co.cybird.android.conanseek.common.BaseButton;
import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.CsvManager;
import jp.co.cybird.android.conanseek.manager.SaveManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.manager.UserInfoManager;
import jp.co.cybird.android.conanseek.param.APIResponseParam;
import jp.co.cybird.android.conanseek.param.CardParam;
import jp.co.cybird.android.conanseek.param.JikenParam;
import jp.souling.android.conanseek01.R;
import jp.souling.android.conanseek01.Settings;

/**
 * 事件：未開放
 */
public class LockedPopup extends BasePopup {

    private String jikenID;

    public static LockedPopup newInstance(String jikenID) {

        Bundle args = new Bundle();

        args.putString("jikenID", jikenID);

        LockedPopup fragment = new LockedPopup();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.popup_jiken_locked, container, false);

        Bundle arg = getArguments();
        if (arg != null) {
            jikenID = arg.getString("jikenID");
        }

        view.findViewById(R.id.dialog_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SeManager.play(SeManager.SeName.PUSH_BACK);
                if (buttonListener != null) {
                    buttonListener.pushedNegativeClick(LockedPopup.this);
                }
                removeMe();
            }
        });

        BaseButton unlockButton = (BaseButton) view.findViewById(R.id.btn_unlock);
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SeManager.play(SeManager.SeName.PUSH_BUTTON);
                SeManager.play(SeManager.SeName.NEW_CHOUHEN);
                if (buttonListener != null) {
                    buttonListener.pushedPositiveClick(LockedPopup.this);
                }
            }
        });
        unlockButton.setEnabled(false);

        ArrayList<JikenParam> jikenList = CsvManager.jikenMaster();
        int index = 0;
        for (JikenParam param : jikenList) {
            if (param.jikenID.equals(jikenID)) {

                //title
                ((TextView) view.findViewById(R.id.dialog_title_text)).setText(param.jikenName);

                //jouken
                String joukenStr = "開放条件\n"
                        + "["
                        + param.kaihouJoukenCardRare
                        + "]カード"
                        + param.kaihouJoukenCardCount
                        + "枚以上";
                ((TextView) view.findViewById(R.id.jouken_text)).setText(joukenStr);

                //thumbnail
                int identifier = Common.myAppContext.getResources().getIdentifier("thumb_" + param.chouhenSeriesNumber, "mipmap", getActivity().getPackageName());
                ((ImageView) view.findViewById(R.id.thumb_mini)).setImageResource(identifier);

                //満たしている？

                //所有カード
                ArrayList<APIResponseParam.Item.Card> cardList = UserInfoManager.myCardList();
                //詳細持ちカード情報
                final Map<Integer, CardParam> cardParamArray = CsvManager.cardParamsWithSkillDetail();

                boolean canPushFlag = false;
                int rareInt = 0;
                int joukenCount = param.kaihouJoukenCardCount;

                if (param.kaihouJoukenCardRare.equals("HN")) rareInt = 1;
                else if (param.kaihouJoukenCardRare.equals("R")) rareInt = 2;
                else if (param.kaihouJoukenCardRare.equals("SR")) rareInt = 3;
                else if (param.kaihouJoukenCardRare.equals("SSR")) rareInt = 4;

                for (APIResponseParam.Item.Card card : cardList) {
                    CardParam cardParam = cardParamArray.get(card.card_id);
                    if (cardParam.rareInt == rareInt) {
                        joukenCount--;
                    }
                    if (joukenCount <= 0) {
                        canPushFlag = true;
                        break;
                    }
                }
                if (Settings.isDebug && SaveManager.boolValue(SaveManager.KEY.DEBUG_JIKEN_ALL_SHOW__boolean, false))
                    canPushFlag = true;

                //ボタンアンロック状態更新
                unlockButton.setEnabled(canPushFlag);

                //L-XXX-OPの次の行の最初の捜査から背景画像を拝借
                JikenParam nextJikenParam = jikenList.get(index + 1);

                //事件マスタに事件の詳細情報付加
                nextJikenParam = CsvManager.addJikenDetail(nextJikenParam);

                //background
                String areaID = CsvManager.areaIdFromAreaName(nextJikenParam.sagashiStage);
                String largeImagePath = CsvManager.areaImageFileFromAreaID(Common.parseInt(areaID), false);

                ImageView dialogBgImage = (ImageView) view.findViewById(R.id.image_dialog_bg);
                dialogBgImage.setImageBitmap(Common.decodedAssetBitmap(largeImagePath, 540, 310));

                break;
            }
            index++;
        }

        return view;
    }

}

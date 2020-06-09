package jp.co.cybird.android.conanseek.activity.sagashi;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.CsvManager;
import jp.co.cybird.android.conanseek.manager.SaveManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.manager.UserInfoManager;
import jp.co.cybird.android.conanseek.param.APIResponseParam;
import jp.co.cybird.android.conanseek.param.CardParam;
import jp.souling.android.conanseek01.R;

/**
 * ポーズ
 */
public class PausePopup extends BasePopup implements View.OnClickListener {

    public static PausePopup newInstance() {
        
        Bundle args = new Bundle();
        
        PausePopup fragment = new PausePopup();
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.popup_pause, container, false);

        view.findViewById(R.id.dialog_close).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_yes).setOnClickListener(this);


        ArrayList<Integer> deckList = SaveManager.deckListByDeckIndex(-1);
        ArrayList<APIResponseParam.Item.Card> myCardList = UserInfoManager.responseParam().item.card;
        ArrayList<Integer> cardIdList = new ArrayList<>();
        Map<Integer, CardParam> cardParamMap = CsvManager.cardParamsWithSkillDetail();

        int index = 0;

        for (int serialID : deckList) {

            cardIdList.add(0);

            for (APIResponseParam.Item.Card card : myCardList) {
                if ( card.id == serialID ) {
                    cardIdList.add(index, card.card_id);
                    break;
                }
            }
            index++;
        }


        for (int i = 0; i < 3; i++) {

            int identifier = Common.myAppContext.getResources().getIdentifier("card_frame_" + (i+1), "id", getActivity().getPackageName());
            FrameLayout frame = (FrameLayout) view.findViewById(identifier);

            int cardID = cardIdList.get(i);

            if (cardID > 0) {

                frame.setVisibility(View.VISIBLE);

                ImageView cardFace = (ImageView) frame.findViewById(R.id.card_face_image);
                ImageView skillImage = (ImageView) frame.findViewById(R.id.skill_image);
                TextView skillName = (TextView) frame.findViewById(R.id.skill_name);
                TextView skillDetail = (TextView) frame.findViewById(R.id.skill_detail);

                CardParam cardParam = cardParamMap.get(cardID);

                try {
                    InputStream is1 = Common.myAppContext.getResources().getAssets().open("egara/426x600/" + cardID + ".jpg");
                    Bitmap bm1 = BitmapFactory.decodeStream(is1);
                    cardFace.setImageBitmap(bm1);
                    is1.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                skillName.setText(cardParam.skillName);
                skillDetail.setText(cardParam.skillDetail);

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
            } else {
                frame.setVisibility(View.GONE);
            }
        }

        SeManager.play(SeManager.SeName.PAUSE_SOUSA);

        return view;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.dialog_close || v.getId() == R.id.btn_cancel) {

            SeManager.play(SeManager.SeName.PUSH_BACK);

            if (buttonListener != null)
                buttonListener.pushedNegativeClick(PausePopup.this);

            SeManager.play(SeManager.SeName.RESTART_SOUSA);

            removeMe();

        } else if (v.getId() == R.id.btn_yes) {

            SeManager.play(SeManager.SeName.PUSH_BUTTON);

            GiveupPopup giveupPopup = GiveupPopup.newInstance();
            giveupPopup.setPopupButtonListener(new PopupButtonListener() {
                @Override
                public void pushedPositiveClick(BasePopup popup) {
                    if (buttonListener != null)
                        buttonListener.pushedPositiveClick(PausePopup.this);
                    popup.removeMe();
                }

                @Override
                public void pushedNegativeClick(BasePopup popup) {

                }
            });
            showPopupFromPopup(giveupPopup);
        }
    }

}

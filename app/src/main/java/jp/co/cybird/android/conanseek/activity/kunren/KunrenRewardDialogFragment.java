package jp.co.cybird.android.conanseek.activity.kunren;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Map;

import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.CsvManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.param.CardParam;
import jp.souling.android.conanseek01.R;

/**
 * 訓練報酬
 */
public class KunrenRewardDialogFragment extends BasePopup {

    private int cardID;

    public static KunrenRewardDialogFragment newInstance(int cardID) {

        Bundle args = new Bundle();
        args.putInt("cardID", cardID);

        KunrenRewardDialogFragment fragment = new KunrenRewardDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.popup_kunren_reward, container, false);

        Bundle args = getArguments();
        if (args != null) {
            cardID = args.getInt("cardID");
        }


        view.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeManager.play(SeManager.SeName.PUSH_BACK);
                if (buttonListener != null)
                    buttonListener.pushedPositiveClick(KunrenRewardDialogFragment.this);
                removeMe();
            }
        });

        Map<Integer, CardParam> cardParamMap = CsvManager.cardParamsWithSkillDetail();
        CardParam cardParam = cardParamMap.get(cardID);

        //No.
        TextView numberText = (TextView) view.findViewById(R.id.card_number_label);
        numberText.setText("No." + cardID);

        //Name
        TextView nameText = (TextView) view.findViewById(R.id.card_name);
        nameText.setText(cardParam.name);

        //Skill
        TextView skillText = (TextView) view.findViewById(R.id.skill_name);
        skillText.setText(cardParam.skillName);

        //Detail
        TextView detailText = (TextView) view.findViewById(R.id.skill_detail);
        detailText.setText(cardParam.skillDetail);

        //Image
        ImageView cardFace = (ImageView) view.findViewById(R.id.card_face_image);

        cardFace.setImageBitmap(Common.decodedAssetBitmap("egara/426x600/" + cardID + ".jpg", 70, 50));

        return view;
    }

}

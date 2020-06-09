package jp.co.cybird.android.conanseek.activity.kunren;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.souling.android.conanseek01.R;

/**
 * ミッション報酬
 */
public class MissionRewardDialogFragment extends BasePopup {

    private String rewardTitle;
    private String rewardItem;


    public static MissionRewardDialogFragment newInstance(String rewardTitle, String rewardItem) {

        Bundle args = new Bundle();
        args.putString("rewardTitle", rewardTitle);
        args.putString("rewardItem", rewardItem);

        MissionRewardDialogFragment fragment = new MissionRewardDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.popup_mission_reward, container, false);

        Bundle args = getArguments();
        if (args != null) {
            rewardTitle = args.getString("rewardTitle");
            rewardItem = args.getString("rewardItem");
        }

        view.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeManager.play(SeManager.SeName.PUSH_BUTTON);
                if (buttonListener != null)
                    buttonListener.pushedPositiveClick(MissionRewardDialogFragment.this);
                removeMe();
            }
        });

        TextView titleText = (TextView) view.findViewById(R.id.reward_title);
        titleText.setText(rewardTitle);


        TextView itemText = (TextView) view.findViewById(R.id.reward_item);
        itemText.setText(

                Html.fromHtml(
                        rewardItem,
                        new Common.ResouroceImageGetter(getContext()), null)




        );

        return view;
    }

}

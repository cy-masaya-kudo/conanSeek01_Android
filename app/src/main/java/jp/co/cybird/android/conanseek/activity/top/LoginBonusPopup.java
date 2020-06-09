package jp.co.cybird.android.conanseek.activity.top;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Map;

import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.CsvManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.manager.UserInfoManager;
import jp.co.cybird.android.conanseek.param.LoginbonusParam;
import jp.souling.android.conanseek01.R;

/**
 * ログインボーナス
 */
public class LoginBonusPopup extends BasePopup implements View.OnClickListener {

    //点滅するセル
    private FrameLayout blinkCell;

    public static LoginBonusPopup newInstance() {
        
        Bundle args = new Bundle();
        
        LoginBonusPopup fragment = new LoginBonusPopup();
        fragment.setArguments(args);
        return fragment;
    }

    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popup_loginbonus, container, false);

        view.findViewById(R.id.btn_yes).setOnClickListener(this);

        //ログイン回数
        int loginCount = UserInfoManager.responseParam().item.tsuuka.login_day_count;

        //ログインボーナスマスタ
        Map<Integer, LoginbonusParam> integerLoginbonusParamMap = CsvManager.loginBonusMaster();

        //ログイン回数日の10日を表示
        int startDay = (loginCount / 10) * 10 + 1;

        for (int i = startDay, k = 0; i < startDay + 10; i++, k++) {

            int identifier = Common.myAppContext.getResources().getIdentifier("list_cell_" + k, "id", getActivity().getPackageName());
            FrameLayout cell = (FrameLayout) view.findViewById(identifier);

            FrameLayout iconFrame = (FrameLayout) cell.findViewById(R.id.icon_frame);
            TextView iconAmount = (TextView) cell.findViewById(R.id.icon_amount);
            TextView dayText = (TextView) cell.findViewById(R.id.day_text);

            LoginbonusParam param = integerLoginbonusParamMap.get(i);

            if (param.rewardName.equals("虫眼鏡")) {
                iconFrame.setBackgroundResource((loginCount > i) ? R.mipmap.icon_megane_sumi : R.mipmap.icon_megane_plane);
            } else if (param.rewardName.equals("ガチャチケット")) {
                iconFrame.setBackgroundResource((loginCount > i) ? R.mipmap.icon_ticket_sumi : R.mipmap.icon_ticket_plane);
            }

            iconAmount.setText("x" + param.rewardAmount);

            dayText.setText(param.dayCount + "日目");

            if (i == loginCount) {
                blinkCell = cell;
            }

        }


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (blinkCell != null) {
            Animation animation = AnimationUtils.loadAnimation(Common.myAppContext, R.anim.blink);
            blinkCell.startAnimation(animation);
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_yes) {

            SeManager.play(SeManager.SeName.PUSH_BACK);
            if (buttonListener != null) {
                buttonListener.pushedNegativeClick(LoginBonusPopup.this);
            }
            removeMe();
        }
    }
}

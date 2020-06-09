package jp.co.cybird.android.conanseek.activity.top;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.co.cybird.android.conanseek.common.BaseButton;
import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.manager.BgmManager;
import jp.co.cybird.android.conanseek.manager.SaveManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.souling.android.conanseek01.R;

/**
 * コンフィグ
 */
public class SoundPopup extends BasePopup implements View.OnClickListener {

    private BaseButton bgmOnButton;
    private BaseButton bgmOffButton;
    private BaseButton seOnButton;
    private BaseButton seOffButton;


    public static SoundPopup newInstance() {

        Bundle args = new Bundle();

        SoundPopup fragment = new SoundPopup();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popup_sound, container, false);

        bgmOnButton = (BaseButton) view.findViewById(R.id.btn_bgm_on);
        bgmOffButton = (BaseButton) view.findViewById(R.id.btn_bgm_off);
        seOnButton = (BaseButton) view.findViewById(R.id.btn_se_on);
        seOffButton = (BaseButton) view.findViewById(R.id.btn_se_off);

        view.findViewById(R.id.dialog_close).setOnClickListener(this);
        bgmOnButton.setOnClickListener(this);
        bgmOffButton.setOnClickListener(this);
        seOffButton.setOnClickListener(this);
        seOnButton.setOnClickListener(this);

        updateButtonState();


        return view;
    }


    private void updateButtonState() {

        boolean bgmFlag = !SaveManager.boolValue(SaveManager.KEY.SOUND_BGM_DISABLE__boolean, false);
        boolean seFlag = !SaveManager.boolValue(SaveManager.KEY.SOUND_SE_DISABLE__boolean, false);

        bgmOnButton.setSelected(bgmFlag);
        bgmOffButton.setSelected(!bgmFlag);
        seOnButton.setSelected(seFlag);
        seOffButton.setSelected(!seFlag);


    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.dialog_close) {

            SeManager.play(SeManager.SeName.PUSH_BACK);
            removeMe();

        } else {
            SeManager.play(SeManager.SeName.PUSH_BUTTON);
            if (v.equals(bgmOnButton)) {
                SaveManager.updateBoolValue(SaveManager.KEY.SOUND_BGM_DISABLE__boolean, false);
                BgmManager.setBgm(BgmManager.BgmName.CONFIG_ON);
                BgmManager.setBgm(BgmManager.BgmName.MAIN);
            }
            else if (v.equals(bgmOffButton)) {
                SaveManager.updateBoolValue(SaveManager.KEY.SOUND_BGM_DISABLE__boolean, true);
                BgmManager.setBgm(BgmManager.BgmName.PAUSE);
            }
            else if (v.equals(seOnButton)) {
                SaveManager.updateBoolValue(SaveManager.KEY.SOUND_SE_DISABLE__boolean, false);

            }
            else if (v.equals(seOffButton)) {
                SaveManager.updateBoolValue(SaveManager.KEY.SOUND_SE_DISABLE__boolean, true);

            }
        }

        updateButtonState();
    }
}

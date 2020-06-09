package jp.co.cybird.android.conanseek.activity.top;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.common.WebViewPopup;
import jp.co.cybird.android.conanseek.manager.SaveManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import com.gency.aid.GencyAID;
import jp.co.cybird.android.minors.ManagerBase;
import jp.co.cybird.android.support.v4.minors.MinorsDialogManager;
import jp.co.cybird.android.utils.Util;
import jp.co.cybird.app.android.lib.commons.security.popgate.Codec;
import jp.souling.android.conanseek01.R;

/**
 * ポップアップ：コンフィグ
 */
public class ConfigPopup extends BasePopup implements View.OnClickListener {

    public static ConfigPopup newInstance() {

        Bundle args = new Bundle();

        ConfigPopup fragment = new ConfigPopup();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.popup_config, container, false);

        view.findViewById(R.id.dialog_close).setOnClickListener(this);
        view.findViewById(R.id.btn_sound).setOnClickListener(this);
        view.findViewById(R.id.btn_torihiki).setOnClickListener(this);
        view.findViewById(R.id.btn_shikin).setOnClickListener(this);
        view.findViewById(R.id.btn_kiyaku).setOnClickListener(this);
        view.findViewById(R.id.btn_privacy).setOnClickListener(this);
        view.findViewById(R.id.btn_toiawase).setOnClickListener(this);
        view.findViewById(R.id.btn_miseinen).setOnClickListener(this);
        view.findViewById(R.id.btn_howto).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.dialog_close || v.getId() == R.id.btn_cancel) {

            SeManager.play(SeManager.SeName.PUSH_BACK);
            removeMe();

        } else {

            SeManager.play(SeManager.SeName.PUSH_BUTTON);

            if (v.getId() == R.id.btn_sound) {
                showPopupFromPopup(new SoundPopup());

            } else {

                WebViewPopup webViewPopup = null;

                switch (v.getId()) {
                    case R.id.btn_torihiki: {
                        webViewPopup = WebViewPopup.newInstance(R.mipmap.title_torihiki, getString(R.string.torihiki));
                        break;
                    }
                    case R.id.btn_shikin: {
                        webViewPopup = WebViewPopup.newInstance(R.mipmap.title_kessai, getString(R.string.kessai));
                        break;
                    }
                    case R.id.btn_kiyaku: {
                        webViewPopup = WebViewPopup.newInstance(R.mipmap.title_kiyaku, getString(R.string.kiyaku));
                        break;
                    }
                    case R.id.btn_privacy: {
                        webViewPopup = WebViewPopup.newInstance(R.mipmap.title_privacy, getString(R.string.privacy));
                        break;
                    }
                    case R.id.btn_toiawase: {
                        String urlString = getMinorsParams(getString(R.string.contact));
                        webViewPopup = WebViewPopup.newInstance(R.mipmap.title_contact, urlString);
                        break;
                    }
                    case R.id.btn_miseinen: {
                        String urlString = getMinorsParams(getString(R.string.miseinen));
                        webViewPopup = WebViewPopup.newInstance(R.mipmap.title_miseinen, urlString);
                        break;
                    }
                    case R.id.btn_howto: {
                        webViewPopup = WebViewPopup.newInstance(R.mipmap.title_asobikata, getString(R.string.help));
                        break;
                    }
                }

                if (webViewPopup != null)
                    showPopupFromPopup(webViewPopup);
            }
        }
    }

    private String getMinorsParams(String baseUrlString) {
        try {
            //IDに関するパラメータ
            String apUUID = SaveManager.stringValue(SaveManager.KEY.AP_UUID__string, "");
            if (apUUID.equals("")) {
                //apuuidが取得できない場合、cyuuidを付与
                apUUID = "CYUUID_" + GencyAID.getGencyAID(getContext());
            }
            String uuidParam = "id=" + Codec.encode(apUUID);

            //未成年に関するパラメータ
            int eulaVersion = getResources().getInteger(R.integer.MinorEulaVersion);
            ManagerBase managerBase = new MinorsDialogManager(getActivity(), eulaVersion, getFragmentManager());
            managerBase.setEulaVer(eulaVersion);

            String minorParam = "";
            if (managerBase.isAgreement()) {
                int age = Util.ageCalculation(Util.birthCalendar(managerBase.getBirthYear(), managerBase.getBirthMonth()), managerBase.getCurrentCalendar());
                minorParam = minorParam + "age=" + Util.getCategory(1, age);
                minorParam = minorParam + "&";
                minorParam = minorParam + "adate=" + managerBase.getAgeRegistDate();
                minorParam = minorParam + "&";
                minorParam = minorParam + "pagree=" + String.valueOf((managerBase.isMinorAgreed()) ? 1 : 0);
                minorParam = minorParam + "&";
                minorParam = minorParam + "pdate=" + managerBase.getMinorAgreementDate();
            }
            minorParam = "minor=" + Codec.encode(minorParam);

            //IDと未成年パラメータを付与
            String param = baseUrlString + "?" + uuidParam + "&" + minorParam;
            return param;
        } catch (Exception e) {

        }
        return baseUrlString;
    }
}

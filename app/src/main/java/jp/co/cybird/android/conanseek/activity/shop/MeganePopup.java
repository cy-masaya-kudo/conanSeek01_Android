package jp.co.cybird.android.conanseek.activity.shop;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.co.cybird.android.conanseek.common.BaseActivity;
import jp.co.cybird.android.conanseek.common.BaseFragment;
import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.common.MessagePopup;
import jp.co.cybird.android.conanseek.manager.APIDialogFragment;
import jp.co.cybird.android.conanseek.manager.APIRequest;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.manager.UserInfoManager;
import jp.co.cybird.android.conanseek.param.APIResponseParam;
import jp.souling.android.conanseek01.R;

/**
 * メガネ購入
 */
public class MeganePopup extends BasePopup implements View.OnClickListener {


    public static MeganePopup newInstance() {

        Bundle args = new Bundle();

        MeganePopup fragment = new MeganePopup();
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.popup_megane, container, false);

        view.findViewById(R.id.dialog_close).setOnClickListener(this);
        view.findViewById(R.id.btn_dialog_megane_5).setOnClickListener(this);
        view.findViewById(R.id.btn_dialog_megane_30).setOnClickListener(this);
        view.findViewById(R.id.btn_dialog_megane_65).setOnClickListener(this);

        return view;
    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.dialog_close) {

            SeManager.play(SeManager.SeName.PUSH_BACK);
            removeMe();

        } else {

            SeManager.play(SeManager.SeName.PUSH_BUTTON);

            int meganeCount = 0;
            int coinCount = 0;
            String procID = getString(R.string.api_proc_trade_megane5);

            if (v.getId() == R.id.btn_dialog_megane_5) {
                meganeCount = 5;
                coinCount = 1;
                procID = getString(R.string.api_proc_trade_megane5);
            }
            if (v.getId() == R.id.btn_dialog_megane_30) {
                meganeCount = 30;
                coinCount = 5;
                procID = getString(R.string.api_proc_trade_megane30);
            }
            if (v.getId() == R.id.btn_dialog_megane_65) {
                meganeCount = 65;
                coinCount = 10;
                procID = getString(R.string.api_proc_trade_megane65);
            }
            Common.logD("123");

            //メガネマックスか
            if (UserInfoManager.meganeCount() + meganeCount >= 99999) {

                showPopupFromPopup(MessagePopup.newInstance("これ以上<img src=\"icon_megane\">虫眼鏡を持てません", null, null));

            }
            //コイン足りるか
            else if (UserInfoManager.coinCount() < coinCount) {

                MessagePopup messagePopup = MessagePopup.newInstance(
                        "<img src=\"icon_coin\">コインが足りません<br/>" + "<img src=\"icon_coin\">コインを購入しますか？",
                        "やめる", "購入"
                );
                messagePopup.setPopupButtonListener(new PopupButtonListener() {
                    @Override
                    public void pushedPositiveClick(BasePopup popup) {
                        showPopupFromPopup(CoinPopup.newInstance());
                    }

                    @Override
                    public void pushedNegativeClick(BasePopup popup) {

                    }
                });
                showPopupFromPopup(messagePopup);

            }

            //条件よし
            else {

                APIRequest request = new APIRequest();
                request.name = APIDialogFragment.APIName.POINT_TRADE;
                request.params.put("proc_id", procID);
                final APIDialogFragment tradeAPI = APIDialogFragment.newInstance(request);
                final int finalCount = meganeCount;
                tradeAPI.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
                    @Override
                    public void getAPIResult(APIRequest request, Object object) {

                        APIResponseParam param = (APIResponseParam) object;

                        //購入成功
                        if (param.item.result.toString().equals("true")) {

                            //userinfo更新
                            APIRequest apiRequest = new APIRequest();
                            apiRequest.name = APIDialogFragment.APIName.USER_INFO;
                            APIDialogFragment infoAPI = APIDialogFragment.newInstance(apiRequest);

                            infoAPI.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
                                @Override
                                public void getAPIResult(APIRequest request, Object object) {

                                    ((BaseFragment)getParentFragment()).updateMyHeaderStatus();

                                    SeManager.play(SeManager.SeName.SHOP_MEGANE);

                                    showPopupFromPopup(MessagePopup.newInstance("<img src=\"icon_megane\">虫眼鏡 x " + finalCount + "個<br/>購入しました。", null, null));
                                }
                            });
                            fireApiFromPopup(infoAPI);

                        }
                        //購入失敗
                        else {

                            showPopupFromPopup(MessagePopup.newInstance("<img src=\"icon_megane\">虫眼鏡の購入に失敗しました", null, null));

                        }
                    }
                });
                fireApiFromPopup(tradeAPI);
            }
        }
    }
}

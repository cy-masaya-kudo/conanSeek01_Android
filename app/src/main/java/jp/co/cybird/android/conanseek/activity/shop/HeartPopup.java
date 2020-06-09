package jp.co.cybird.android.conanseek.activity.shop;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.co.cybird.android.conanseek.common.BaseActivity;
import jp.co.cybird.android.conanseek.common.BaseFragment;
import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.common.MessagePopup;
import jp.co.cybird.android.conanseek.manager.APIDialogFragment;
import jp.co.cybird.android.conanseek.manager.APIRequest;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.manager.UserInfoManager;
import jp.co.cybird.android.conanseek.param.APIResponseParam;
import jp.souling.android.conanseek01.R;

/**
 * ハート購入
 */
public class HeartPopup extends BasePopup implements View.OnClickListener {


    public static HeartPopup newInstance() {

        Bundle args = new Bundle();

        HeartPopup fragment = new HeartPopup();
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.popup_heart, container, false);

        view.findViewById(R.id.dialog_close).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_yes).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.dialog_close || v.getId() == R.id.btn_cancel) {

            SeManager.play(SeManager.SeName.PUSH_BACK);
            removeMe();
        } else {

            SeManager.play(SeManager.SeName.PUSH_BUTTON);

            if (UserInfoManager.heartCount() >= 10) {
                //ハートマックスか
                showPopupFromPopup(MessagePopup.newInstance("<img src=\"icon_heart\">ハートはすでに満タンです",null,null));


            } else if (UserInfoManager.coinCount() < 1) {
                //コイン足りるか
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

            } else {

                //条件よし
                APIRequest request = new APIRequest();
                request.name = APIDialogFragment.APIName.POINT_TRADE;
                request.params.put("proc_id", getString(R.string.api_proc_trade_heartmax));

                final APIDialogFragment tradeAPI = APIDialogFragment.newInstance(request);
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

                                    //ヘッダー更新
                                    ((BaseFragment)getParentFragment()).updateMyHeaderStatus();

                                    SeManager.play(SeManager.SeName.SHOP_HEART);

                                    showPopupFromPopup(MessagePopup.newInstance("<img src=\"icon_heart\">ハート全回復<br/>購入しました。", null, null));
                                }
                            });
                            fireApiFromPopup(infoAPI);

                        }
                        //購入失敗
                        else {

                            showPopupFromPopup(MessagePopup.newInstance("<img src=\"icon_heart\">ハートの購入に失敗しました", null, null));
                        }
                    }
                });
                fireApiFromPopup(tradeAPI);
            }
        }
    }
}

package jp.souling.android.conanseek01;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.gency.aid.GencyAID;
import com.gency.gcm.GencyGCMUtilities;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tapjoy.Tapjoy;

import java.util.ArrayList;

import jp.co.cybird.android.conanseek.activity.card.CardFragment;
import jp.co.cybird.android.conanseek.activity.gacha.GachaFragment;
import jp.co.cybird.android.conanseek.activity.jiken.JikenFragment;
import jp.co.cybird.android.conanseek.activity.kunren.KunrenFragment;
import jp.co.cybird.android.conanseek.activity.shop.CoinPopup;
import jp.co.cybird.android.conanseek.activity.shop.ShopFragment;
import jp.co.cybird.android.conanseek.common.BaseActivity;
import jp.co.cybird.android.conanseek.common.BaseFragment;
import jp.co.cybird.android.conanseek.manager.APIDialogFragment;
import jp.co.cybird.android.conanseek.manager.APIRequest;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.SaveManager;
import jp.co.cybird.android.conanseek.param.APIResponseParam;
import jp.co.cybird.android.conanseek.param.NanidoParam;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Common.myAppContext = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Common.myAppContext = getApplicationContext();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        String url = getString(R.string.api);
        this.notapLayout = (FrameLayout) findViewById(R.id.notap_cover);
        this.fadeLayout = (FrameLayout) findViewById(R.id.fade_cover);

        this.wrapperLayout = (RelativeLayout) findViewById(R.id.wrapper);
        this.adjustActivitySize();

        Intent intent = getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
            android.net.Uri uri = intent.getData();

            if (uri.getScheme().equals("conanseek01") && uri.getHost().length() > 0) {

                String userInfo = SaveManager.stringValue(SaveManager.KEY.USER_INFO__string, null);
                if (userInfo == null) {

                    new AlertDialog.Builder(this)
                            .setTitle("error")
                            .setMessage("ゲームにログインしてから実行してくださいll")
                            .setPositiveButton("OK", null)
                            .show();


                } else {

                    if (uri.getHost().equals("card")) {
                        replaceViewController(CardFragment.newInstance(null, null, null));
                        return;
                    } else if (uri.getHost().equals("kunren")) {
                        replaceViewController(KunrenFragment.newInstance());
                        return;
                    } else if (uri.getHost().equals("jiken")) {
                        replaceViewController(JikenFragment.newInstance(null));
                        return;
                    } else if (uri.getHost().equals("gacha")) {
                        replaceViewController(GachaFragment.newInstance());
                        return;
                    } else if (uri.getHost().equals("shop")) {
                        replaceViewController(ShopFragment.newInstance());
                        return;
                    } else if (uri.getHost().equals("coin")) {
                        replaceViewController(ShopFragment.newInstance());
                        return;
                    } else if (uri.getHost().equals("megane")) {
                        replaceViewController(ShopFragment.newInstance());
                        return;
                    } else if (uri.getHost().equals("heart")) {
                        replaceViewController(ShopFragment.newInstance());
                        return;
                    } else if (uri.getHost().equals("code")) {

                        String code = uri.getQueryParameter("code");

                        if (code.length() > 0) {

                            APIRequest tokutenRequest = new APIRequest();
                            tokutenRequest.name = APIDialogFragment.APIName.ADD_TOKUTEN;
                            tokutenRequest.params.put("tokuten_code", code);

                            APIDialogFragment tokutenAPI = APIDialogFragment.newInstance(tokutenRequest);
                            tokutenAPI.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
                                @Override
                                public void getAPIResult(APIRequest request, Object object) {

                                    APIResponseParam param = (APIResponseParam) object;

                                    APIResponseParam.Item.Tokuten tokutenParam
                                            = new Gson().fromJson(param.item.result.toString(), new TypeToken<ArrayList<NanidoParam>>() {
                                    }.getType());

                                    String message = null;

                                    switch (tokutenParam.present_key) {
                                        case "megane":
                                            message = "<[megane]>虫眼鏡 x " + tokutenParam.present_value + "<br>をプレゼントボックスに送りました。";
                                            break;
                                        case "ticket":
                                            message = "<[ticket]>ガチャチケット x " + tokutenParam.present_value + "<br>をプレゼントボックスに送りました。";
                                            break;
                                        case "card":
                                            message = "<[card]>カード x 1<br>をプレゼントボックスに送りました。";
                                            break;
                                    }

                                    if (message != null) {
                                        replaceViewController(SplashFragment.newInstance(message));
                                    }


                                }
                            });
                            tokutenAPI.show(getSupportFragmentManager(), null);
                        }


                    }
                }
            }

        }

        replaceViewController(SplashFragment.newInstance(null));

        this.startGCM();


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Common.logD("onActivityResult:" + resultCode + " " + resultCode + " data " + data);

        if (resultCode == RESULT_OK || resultCode == RESULT_CANCELED) {
            BaseFragment baseFragment = getCurrentViewController();
            if (baseFragment != null) {
                if (baseFragment.getChildFragmentManager() != null && baseFragment.getChildFragmentManager().getFragments() != null) {
                    for (Fragment fragment : baseFragment.getChildFragmentManager().getFragments()) {
                        if (fragment instanceof CoinPopup) {

                            CoinPopup coinPopup = (CoinPopup) fragment;
                            coinPopup.receiveOnActivityResult(requestCode, resultCode, data);
                            break;
                        }
                    }
                }
            }
        }
        startGCM();
    }

    private void startGCM() {
        //PUSH開始
        try {
            GencyGCMUtilities.runGCM(this, getCyUUID());        // 旧版はGencyAIDを送信してたけど、最新版は送信しないので修正
            //PUSH設定
            GencyGCMUtilities.setWillSendNotification(true);
            GencyGCMUtilities.setWillPlaySound(true);
            GencyGCMUtilities.setWillVibrate(true);
        } catch (Exception e) {

        }
    }

    /**
     * GencyAIDを取得するメソッド
     * @return GencyAID
     */
    @SuppressWarnings("deprecation")
    public String getCyUUID() {
        String aid = null;
        try {
            aid = GencyAID.getGencyAID(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return aid;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Tapjoy.onActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Tapjoy.onActivityStop(this);
    }
}
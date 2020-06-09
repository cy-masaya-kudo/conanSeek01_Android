package jp.souling.android.conanseek01;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.tapjoy.TJActionRequest;
import com.tapjoy.TJConnectListener;
import com.tapjoy.TJError;
import com.tapjoy.TJPlacement;
import com.tapjoy.TJPlacementListener;
import com.tapjoy.Tapjoy;
import com.tapjoy.TapjoyConnectFlag;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

import jp.co.cybird.android.agreement.AgreementDialog;
import jp.co.cybird.android.agreement.AgreementDialog.OnAgreeListener;
import jp.co.cybird.android.agreement.AgreementDialog.OnNoUpdateListener;
import jp.co.cybird.android.agreement.AgreementDialog.OnCancelListener;
import jp.co.cybird.android.agreement.AgreementDialog.OnDeclineListener;
import jp.co.cybird.android.agreement.AgreementDialog.OnDismissListener;
import jp.co.cybird.android.conanseek.activity.effect.MachineEffect;
import jp.co.cybird.android.conanseek.activity.jiken.KaiwaPopup;
import jp.co.cybird.android.conanseek.activity.top.DebugPopup;
import jp.co.cybird.android.conanseek.activity.top.TopFragment;
import jp.co.cybird.android.conanseek.common.BaseActivity;
import jp.co.cybird.android.conanseek.common.BaseFragment;
import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.common.FlashingButton;
import jp.co.cybird.android.conanseek.common.MessagePopup;
import jp.co.cybird.android.conanseek.manager.APIDialogFragment;
import jp.co.cybird.android.conanseek.manager.APIRequest;
import jp.co.cybird.android.conanseek.manager.BgmManager;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.CsvManager;
import jp.co.cybird.android.conanseek.manager.SaveManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.manager.UserInfoManager;
import jp.co.cybird.android.utils.AsyncHttpTask;

/**
 * フラグメント：スプラッシュ
 */
public class SplashFragment extends BaseFragment implements View.OnClickListener,
        OnAgreeListener, OnNoUpdateListener, OnDeclineListener, OnCancelListener, OnDismissListener, AsyncHttpTask.OnResponseReceive, TJConnectListener {

    private FlashingButton startButton;

    private AgreementDialog mAgreementDialog;

    boolean startSeFlag = false;

    public static SplashFragment newInstance(String message) {

        Bundle args = new Bundle();

        args.putString("message", message);

        SplashFragment fragment = new SplashFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_splash, container, false);

        bgmName = BgmManager.BgmName.MAIN;

        view.findViewById(R.id.fragment_wrapper).setOnClickListener(this);

        startButton = (FlashingButton) view.findViewById(R.id.splashButton);
        startButton.setOnClickListener(this);

        if (Settings.isDebug) {
            //デバッグ初回起動時にフラグ系に値を与える
            boolean setuped = SaveManager.boolValue(SaveManager.KEY.DEBUG_SETUP__boolean, false);
            if (!setuped) {
                SaveManager.updateBoolValue(SaveManager.KEY.DEBUG_SETUP__boolean, true);
                SaveManager.updateBoolValue(SaveManager.KEY.DEBUG_CARD_ALL_HAVE__boolean, false);
                SaveManager.updateBoolValue(SaveManager.KEY.DEBUG_JIKEN_ALL_SHOW__boolean, false);
                SaveManager.updateBoolValue(SaveManager.KEY.DEBUG_KUNREN_ALL_SHOW__boolean, false);
            }
        }

        startSeFlag = false;

        // ダイアログ表示に使用するクラスを初期化
        int eulaVersion = getResources().getInteger(R.integer.AgreementEulaVersion);
        mAgreementDialog = new AgreementDialog(getActivity(),eulaVersion,getString(R.string.kiyaku),false);
        // 各イベントを取得できるようにリスナーを設定
        mAgreementDialog.setNoUpdateListener(this);
        mAgreementDialog.setOnCancelListener(this);
        mAgreementDialog.setOnDeclineListener(this);
        mAgreementDialog.setOnAgreeListener(this);
        mAgreementDialog.setOnDismissListener(this);

        //デバッグボタン

        if (Settings.isDebug) {

            Button button = new Button(getContext());
            button.setText("DBG");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopup(new DebugPopup());
                }
            });
            ((ViewGroup) view).addView(button);

            ViewGroup.LayoutParams param = button.getLayoutParams();
            param.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            param.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            button.setLayoutParams(param);

        }



        return view;
    }


    private boolean appeared = false;

    @Override
    protected void fragmentDidAppear() {
        super.fragmentDidAppear();

        if (!appeared) {
            if (onAddedFlag) {
                if (new Random().nextBoolean()) {
                    SeManager.play(SeManager.SeName.SPLAH_SHOW_A);
                } else {
                    SeManager.play(SeManager.SeName.SPLAH_SHOW_B);
                }
                onAddedFlag = false;
            }
            appeared = true;
        }

        final Bundle args = getArguments();

        if (args != null && args.getString("message") != null) {
            MessagePopup messagePopup = MessagePopup.newInstance(args.getString("message"));
            messagePopup.setPopupDisplayListener(new BasePopup.PopupDisplayListener() {
                @Override
                public void didShowPopup(BasePopup popup) {
                    args.putString("message", null);
                }

                @Override
                public void didClosePopup(BasePopup popup) {

                }
            });
            showPopup(messagePopup);
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        startButton.startFlashing();
    }

    @Override
    public void onPause() {
        super.onPause();
        startButton.stopFlashing();
    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.fragment_wrapper) {

            mAgreementDialog.show();

//            if (!mAgreementDialog.isAgreed()) {
//                mAgreementDialog.show();
//            } else {
//                Hashtable<String, Object> connectFlags = new Hashtable<>();
//                connectFlags.put(TapjoyConnectFlag.ENABLE_LOGGING, "true");
//
//                Tapjoy.connect(Common.myAppContext, Settings.TAPJOY_KEY, connectFlags, this);
//                if (Settings.isDebug) {
//                    Tapjoy.setDebugEnabled(true);
//                }
//
//                SeManager.play(SeManager.SeName.PUSH_BUTTON);
//
//                startButton.startFastFlashing();
//
//                String apUUID = SaveManager.stringValue(SaveManager.KEY.AP_UUID__string, "");
//
//                if (apUUID.length() > 0) {
//                    if (detectFirstZipDownload()) {
//                        if (detectFinishedFirstKaiwa()) {
//                            getUserInfo();
//                        }
//                    }
//                } else {
//                    getUserCreate();
//                }
//            }
        }

    }
    @Override
    public void onAgree() {
        Common.logD("AgreementDEMO onAgree()");
    }

    @Override
    public void onNoUpdate() {
        Hashtable<String, Object> connectFlags = new Hashtable<>();
        connectFlags.put(TapjoyConnectFlag.ENABLE_LOGGING, "true");

        Tapjoy.connect(Common.myAppContext, Settings.TAPJOY_KEY, connectFlags, this);
        if (Settings.isDebug) {
            Tapjoy.setDebugEnabled(true);
        }

        SeManager.play(SeManager.SeName.PUSH_BUTTON);

        startButton.startFastFlashing();

        String apUUID = SaveManager.stringValue(SaveManager.KEY.AP_UUID__string, "");

        if (apUUID.length() > 0) {
            if (detectFirstZipDownload()) {
                if (detectFinishedFirstKaiwa()) {
                    getUserInfo();
                }
            }
        } else {
            getUserCreate();
        }
    }

    @Override
    public void onDecline() {
        Common.logD("AgreementDEMO onDecline()");

    }

    @Override
    public void onCancel() {
        Common.logD("AgreementDEMO onCancel()");
    }

    @Override
    public void onDissmiss() {
        Common.logD("AgreementDEMO onDismiss()");
    }


    @Override
    public void onResponseReceive(String ret) {
        Common.logD("onResponseReceive ret()"+ret);

    }

    //初回story
    private boolean detectFinishedFirstKaiwa() {

        boolean finished = SaveManager.boolValue(SaveManager.KEY.FIRST_KAIWA__boolena, false);

        Common.logD("push fist kaiwa:"+finished);

        if (finished) {
            return true;
        }


        final KaiwaPopup kaiwaPopup = KaiwaPopup.newInstance(
                "csv/jiken/0/chutorial1.csv", null
        );
        kaiwaPopup.setKaiwaEffectListener(new KaiwaPopup.KaiwaEffectListener() {
            @Override
            public void kaiwaEffectCode(String code) {
                if (code.equals("マシン演出")) {

                    MachineEffect machineEffect = new MachineEffect();
                    machineEffect.setMachineListener(new MachineEffect.MachineListener() {
                        @Override
                        public void machineDidFlyaway() {


                            android.support.v4.app.FragmentTransaction ft = getChildFragmentManager().beginTransaction();

                            for (Fragment fragment : getChildFragmentManager().getFragments()) {
                                if (fragment instanceof MachineEffect) {
                                    ft.remove(fragment);
                                    break;
                                }
                            }

                            ft.commit();

                            kaiwaPopup.next(true);
                        }
                    });

                    android.support.v4.app.FragmentTransaction ft = getChildFragmentManager().beginTransaction();

                    ft.replace(R.id.effect_container, machineEffect);

                    ft.commit();
                }
            }
        });
        kaiwaPopup.setPopupDisplayListener(new BasePopup.PopupDisplayListener() {
            @Override
            public void didShowPopup(BasePopup popup) {
                if (getView() != null) {
                    getView().findViewById(R.id.fragment_wrapper).setBackground(null);
                    startButton.setAnimation(null);
                    startButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void didClosePopup(BasePopup popup) {
                SaveManager.updateBoolValue(SaveManager.KEY.FIRST_KAIWA__boolena, true);
                ((BaseActivity) getActivity()).replaceViewController(new TopFragment());
            }
        });
        showPopup(kaiwaPopup);


        return false;
    }

    //初期コンテンツ持ち確認
    private boolean detectFirstZipDownload() {

        Bitmap bitmap = Common.decodedBitmap(
                CsvManager.bitmapImagePath("zipflag", "", "1", "png"),
                1, 1
        );

        if (bitmap == null) {

            MessagePopup messagePopup = MessagePopup.newInstance(
                    "データのダウンロードを開始します。<br/>よろしいでしょうか？",
                    "いいえ", "はい"
            );
            messagePopup.setPopupButtonListener(new BasePopup.PopupButtonListener() {

                @Override
                public void pushedNegativeClick(BasePopup popup) {

                }

                @Override
                public void pushedPositiveClick(BasePopup popup) {

                    APIRequest request = new APIRequest();
                    request.name = APIDialogFragment.APIName.FILE_DOWNLOAD;
                    request.params.put("zip", "1");

                    APIDialogFragment f = APIDialogFragment.newInstance(request);
                    f.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
                        @Override
                        public void getAPIResult(APIRequest request, Object object) {

                            getUserInfo();

                        }
                    });
                    fireApi(f);
                }

            });
            showPopup(messagePopup);

            return false;
        }
        return true;
    }


    //進捗にあったデータ持ち確認
    private boolean detectZipsDownload() {

        //進捗に見合ったzipがDL済みかを確認
        ArrayList<String> notDownloadZipArray = new ArrayList<>();

        Bitmap bitmap = Common.decodedBitmap(CsvManager.bitmapImagePath("zipflag", "", "1", "png"), 1, 1);
        Common.logD("zip check 1:" + bitmap);
        if (bitmap == null) {
            notDownloadZipArray.add("1");
        }

        if (UserInfoManager.jikenCleared("003", true)) {
            bitmap = Common.decodedBitmap(CsvManager.bitmapImagePath("zipflag", "", "2", "png"), 1, 1);
            Common.logD("zip check 2:" + bitmap);
            if (bitmap == null)
                notDownloadZipArray.add("2");
        }

        if (UserInfoManager.jikenCleared("157", true)) {
            bitmap = Common.decodedBitmap(CsvManager.bitmapImagePath("zipflag", "", "3", "png"), 1, 1);
            Common.logD("zip check 3:" + bitmap);
            if (bitmap == null)
                notDownloadZipArray.add("3");
        }
        if (notDownloadZipArray.size() == 0) {
            return true;
        }

        //未ダウンロードzipを落とす
        APIRequest r = new APIRequest();
        r.name = APIDialogFragment.APIName.FILE_DOWNLOAD;
        r.params.put("zip", notDownloadZipArray.get(0));

        APIDialogFragment download = APIDialogFragment.newInstance(r);
        download.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
            @Override
            public void getAPIResult(APIRequest request, Object object) {
                if (detectZipsDownload())
                    ((BaseActivity) getActivity()).replaceViewController(new TopFragment());
            }
        });
        fireApi(download);

        return false;
    }


    private void getUserInfo() {

        APIRequest request = new APIRequest();
        request.name = APIDialogFragment.APIName.USER_INFO;

        APIDialogFragment f = APIDialogFragment.newInstance(request);
        f.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
            @Override
            public void getAPIResult(APIRequest request, Object object) {

                Tapjoy.setUserID(UserInfoManager.responseParam().item.user.ap_uuid);

                if (detectZipsDownload()) {
                    if (detectFinishedFirstKaiwa()) {
                        if (!startSeFlag)
                            SeManager.play(SeManager.SeName.SPLSH_END);
                        startSeFlag = true;
                        ((BaseActivity) getActivity()).replaceViewController(new TopFragment());
                    }
                }
            }
        });
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(f, null);
        ft.commitAllowingStateLoss();
        //f.show(getFragmentManager(), null);




    }


    private void getUserCreate() {


        APIRequest request = new APIRequest();
        request.name = APIDialogFragment.APIName.USER_CREATE;

        APIDialogFragment f = APIDialogFragment.newInstance(request);
        f.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
            @Override
            public void getAPIResult(APIRequest request, Object object) {
                if (detectFirstZipDownload())
                    getUserInfo();
            }
        });
        fireApi(f);
    }






    @Override
    public void onConnectSuccess() {
        Common.tjLog("Tapjoy connect Succeeded");

        TJPlacementListener placementListener = new TJPlacementListener() {
            @Override
            public void onRequestSuccess(TJPlacement tjPlacement) {
                Common.tjLog("onRequestSuccess");
            }

            @Override
            public void onRequestFailure(TJPlacement tjPlacement, TJError tjError) {
                Common.tjLog("onRequestFailure");
            }

            @Override
            public void onContentReady(TJPlacement tjPlacement) {
                Common.tjLog("onContentReady");
            }

            @Override
            public void onContentShow(TJPlacement tjPlacement) {
                Common.tjLog("onContentShow");
            }

            @Override
            public void onContentDismiss(TJPlacement tjPlacement) {
                Common.tjLog("onContentDismiss");
            }

            @Override
            public void onPurchaseRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s) {
                Common.tjLog("onPurchaseRequest");
            }

            @Override
            public void onRewardRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s, int i) {
                Common.tjLog("onRewardRequest");
            }
        };
        TJPlacement p = new TJPlacement(Common.myAppContext, "APP_LAUNCH", placementListener);
        p.requestContent();
    }

    @Override
    public void onConnectFailure() {
        Common.tjLog("Tapjoy connect Failed");
    }
}

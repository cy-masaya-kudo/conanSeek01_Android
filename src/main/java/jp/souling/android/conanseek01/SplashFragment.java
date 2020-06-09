package jp.souling.android.conanseek01;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Random;

import jp.co.cybird.android.conanseek.activity.effect.BaseEffect;
import jp.co.cybird.android.conanseek.activity.effect.CardEffect;
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

/**
 * フラグメント：スプラッシュ
 */
public class SplashFragment extends BaseFragment implements View.OnClickListener {

    //start button
    private FlashingButton startButton;

    jp.co.cybird.android.support.v4.minors.MinorsDialogManager mMinorsDialogManager;



    public SplashFragment (){}

    private MessagePopup schemeMessage;

    public SplashFragment (MessagePopup messagePopup){
        this.schemeMessage = messagePopup;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_splash, container, false);

        bgmName = BgmManager.BgmName.MAIN;

        view.findViewById(R.id.fragment_wrapper).setOnClickListener(this);

        startButton = (FlashingButton) view.findViewById(R.id.splashButton);
        startButton.setOnClickListener(this);


        // ダイアログ表示に使用するクラスを初期化
        mMinorsDialogManager = new jp.co.cybird.android.support.v4.minors.MinorsDialogManager(getContext(), 1, getFragmentManager());
        // 各イベントを取得できるようにリスナーを設定
        //mMinorsDialogManager.setOnCancelListener(this);
        //mMinorsDialogManager.setOnDeclineListener(this);
        //mMinorsDialogManager.setOnAgreeListener(this);
        //mMinorsDialogManager.setOnDismissListener(this);


        //デバッグボタン

        if (Settings.isDebug) {

            Button button = new Button(getContext());
            button.setText("DBG");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pushPopup(new DebugPopup());
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
            if (new Random().nextBoolean()) {
                SeManager.play(getContext(), SeManager.SeName.SPLAH_SHOW_A);
            } else {
                SeManager.play(getContext(), SeManager.SeName.SPLAH_SHOW_B);
            }
            appeared = true;
        }

        if (schemeMessage != null) {
            schemeMessage.setPopupDisplayListener(new BasePopup.PopupDisplayListener() {
                @Override
                public void didShowPopup(BasePopup popup) {
                    schemeMessage = null;
                }

                @Override
                public void didClosePopup(BasePopup popup) {

                }
            });
            pushPopup(schemeMessage);
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

            SeManager.play(getContext(), SeManager.SeName.PUSH_BUTTON);

            startButton.startFastFlashing();

            String apUUID = SaveManager.stringValue(getContext(), SaveManager.KEY.AP_UUID__string, "");

            if (apUUID.length() > 0) {
                if (detectFirstZipDownload()) {
                    if (detectFinishedFirstKaiwa()) {
                        getUserInfo();
                    }
                }
            } else {
                getUserCreate();
            }
        } else {

        }

    }


    //初回story
    private boolean detectFinishedFirstKaiwa() {

        boolean finished = SaveManager.boolValue(getContext(), SaveManager.KEY.FIRST_KAIWA__boolena, false);

        Common.logD("push fist kaiwa:"+finished);

        if (finished) {
            return true;
        }


        final KaiwaPopup kaiwaPopup = new KaiwaPopup(
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
                ((ViewGroup) getView().findViewById(R.id.fragment_wrapper)).setBackground(null);
                startButton.setAnimation(null);
                startButton.setVisibility(View.GONE);
            }

            @Override
            public void didClosePopup(BasePopup popup) {
                SaveManager.updateBoolValue(getContext(), SaveManager.KEY.FIRST_KAIWA__boolena, true);
                ((BaseActivity) getActivity()).replaceViewController(new TopFragment());
            }
        });
        pushPopup(kaiwaPopup);


        return false;
    }

    //初期コンテンツ持ち確認
    private boolean detectFirstZipDownload() {

        Bitmap bitmap = Common.decodedBitmap(
                getResources(),
                CsvManager.bitmapImagePath(getContext(), "zipflag", "", "1", "png"),
                1, 1
        );

        if (bitmap == null) {

            MessagePopup messagePopup = new MessagePopup(
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
                    f.show(getChildFragmentManager(), null);
                }

            });
            pushPopup(messagePopup);

            return false;
        }
        return true;
    }


    //進捗にあったデータ持ち確認
    private boolean detectZipsDownload() {

        //進捗に見合ったzipがDL済みかを確認
        ArrayList<String> notDownloadZipArray = new ArrayList<>();

        Bitmap bitmap = Common.decodedBitmap(getResources(), CsvManager.bitmapImagePath(getContext(), "zipflag", "", "1", "png"), 1, 1);
        Common.logD("zip check 1:" + bitmap);
        if (bitmap == null) {
            notDownloadZipArray.add("1");
        }

        if (UserInfoManager.jikenCleared(getContext(), "003")) {
            bitmap = Common.decodedBitmap(getResources(), CsvManager.bitmapImagePath(getContext(), "zipflag", "", "2", "png"), 1, 1);
            Common.logD("zip check 2:" + bitmap);
            if (bitmap == null)
                notDownloadZipArray.add("2");
        }

        if (UserInfoManager.jikenCleared(getContext(), "157")) {
            bitmap = Common.decodedBitmap(getResources(), CsvManager.bitmapImagePath(getContext(), "zipflag", "", "3", "png"), 1, 1);
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
        download.show(getChildFragmentManager(), null);

        return false;
    }


    private void getUserInfo() {

        APIRequest request = new APIRequest();
        request.name = APIDialogFragment.APIName.USER_INFO;

        APIDialogFragment f = APIDialogFragment.newInstance(request);
        f.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
            @Override
            public void getAPIResult(APIRequest request, Object object) {
                if (detectZipsDownload()) {
                    if (detectFinishedFirstKaiwa()) {
                        SeManager.play(getActivity().getApplicationContext(), SeManager.SeName.SPLSH_END);
                        ((BaseActivity) getActivity()).replaceViewController(new TopFragment());
                    }
                }
            }
        });
        f.show(getFragmentManager(), null);


        //TutorialCover.instance(getApplication()).show(getSystemService(WINDOW_SERVICE));


        //SagashiSceneEffect effect = SagashiSceneEffect.newInstance(SagashiSceneEffect.Style.START);
        //effect.show(getSupportFragmentManager(), null);


        /*
        Common.logD("startService");

*/


        //TutorialCover tutorialCover = new TutorialCover(getBaseContext());
        //this.wrapperLayout.addView(tutorialCover);

        APIRequest r = new APIRequest();
        r.name = APIDialogFragment.APIName.FILE_DOWNLOAD;
        r.params.put("zip", String.valueOf(3));

        APIDialogFragment download = APIDialogFragment.newInstance(r);
        download.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
            @Override
            public void getAPIResult(APIRequest request, Object object) {
            }
        });
        //download.show(getSupportFragmentManager(), null);

        CardEffect ef = CardEffect.newInstance(14);
        ef.setEffectListener(new BaseEffect.EffectListener() {
            @Override
            public void effectFinished() {

            }
        });
        //ef.show(getSupportFragmentManager(), null);

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
        f.show(getChildFragmentManager(), null);
    }

}

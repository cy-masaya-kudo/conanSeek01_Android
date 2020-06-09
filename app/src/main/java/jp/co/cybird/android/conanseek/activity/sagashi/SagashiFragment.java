package jp.co.cybird.android.conanseek.activity.sagashi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import jp.co.cybird.android.conanseek.activity.effect.BaseEffect;
import jp.co.cybird.android.conanseek.activity.effect.CardEffect;
import jp.co.cybird.android.conanseek.activity.effect.SagashiSceneEffect;
import jp.co.cybird.android.conanseek.activity.jiken.AngouRewardPopup;
import jp.co.cybird.android.conanseek.activity.jiken.JikenFragment;
import jp.co.cybird.android.conanseek.activity.jiken.TargetIcon;
import jp.co.cybird.android.conanseek.activity.kunren.KunrenFragment;
import jp.co.cybird.android.conanseek.activity.kunren.KunrenRewardDialogFragment;
import jp.co.cybird.android.conanseek.activity.kunren.MissionRewardDialogFragment;
import jp.co.cybird.android.conanseek.activity.shop.HeartPopup;
import jp.co.cybird.android.conanseek.activity.shop.MeganePopup;
import jp.co.cybird.android.conanseek.activity.top.TopFragment;
import jp.co.cybird.android.conanseek.common.BaseActivity;
import jp.co.cybird.android.conanseek.common.BaseButton;
import jp.co.cybird.android.conanseek.common.BaseFragment;
import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.common.MessagePopup;
import jp.co.cybird.android.conanseek.manager.APIDialogFragment;
import jp.co.cybird.android.conanseek.manager.APIRequest;
import jp.co.cybird.android.conanseek.manager.BgmManager;
import jp.co.cybird.android.conanseek.manager.CacheManager;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.CsvManager;
import jp.co.cybird.android.conanseek.manager.SaveManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.manager.UserInfoManager;
import jp.co.cybird.android.conanseek.param.APIResponseParam;
import jp.co.cybird.android.conanseek.param.CardParam;
import jp.co.cybird.android.conanseek.param.JikenParam;
import jp.co.cybird.android.conanseek.param.LocationParam;
import jp.co.cybird.android.conanseek.param.MonoParam;
import jp.co.cybird.android.conanseek.param.NanidoParam;
import jp.co.cybird.android.conanseek.param.TutorialParam;
import jp.souling.android.conanseek01.R;
import jp.souling.android.conanseek01.Settings;

public class SagashiFragment extends BaseFragment {

    //ゲームの状態
    State gameState;

    private enum State {
        STANDBY,
        PLAYING,
        PAUSE,
        FINISHED
    }

    //パレットのフレーム（ズーム影響なしタイマーエフェクト置き場）
    private FrameLayout paletteFrame;
    //パレットコンテナ
    private SagashiPaletteFrame paletteContainer;
    //モノ配置パレット
    private FrameLayout palette;
    //障害物配置パレット
    private FrameLayout shougaiPalette;
    //背景画像ビュー
    private ImageView paletteBg;
    //パレットを覆う白いビュー：エフェクト時フラッシュ
    private View paletteFlash;

    //カードデータ
    private ArrayList<CardParam> cardList;
    //カードビュー
    private FrameLayout cardFrames[] = new FrameLayout[3];

    //ヘッダー
    private SagashiHeaderFragment headerFragment;
    //ポーズボタン
    private BaseButton pauseButton;

    //ミスタップカウント
    private int missTapCount = 0;

    //ミスタップ上限
    private static final int MISS_TAP_LIMIT = 5;

    //アイテムにタッチできる余分領域
    private static final int ITEM_TAP_MARGIN = 36;

    //--------- ゲーム設定

    //チュートリアル設定
    private boolean tutorialSetup;

    //難易度設定
    private NanidoParam nanidoParam;

    //事件設定
    private JikenParam jikenParam;

    //ロケーションパラメーター一覧
    private ArrayList<LocationParam> locationList;
    //もの情報マップ
    private Map<Integer, MonoParam> monoMap;

    //今回のターゲット一覧
    private ArrayList<LocationParam> thisTargetList;
    //今回のテーゲット数
    private int thisTargetCount = 15;

    //エフェクトの状態：カラー
    private boolean effectMonochroFlag = false;
    //エフェクトの状態：向き/0正常 1横 2縦
    private int effectDirectionValue = 0;
    //エフェクトの状態：順番指定
    private boolean effectOrderFlag = false;


    // 再ダウンロード完了フラグ
    private boolean reDownloadFlag = false;


    public static SagashiFragment newInstance(NanidoParam nanidoParam, JikenParam jikenParam) {

        Bundle args = new Bundle();
        args.putSerializable("nanidoParam", nanidoParam);
        args.putSerializable("jikenParam", jikenParam);

        SagashiFragment fragment = new SagashiFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sagashi, container, false);

        Bundle args = getArguments();
        if (args != null) {

            this.nanidoParam = (NanidoParam) args.getSerializable("nanidoParam");
            this.jikenParam = (JikenParam) args.getSerializable("jikenParam");
        }

        bgmName = BgmManager.BgmName.SOUSA;

        palette = (FrameLayout) view.findViewById(R.id.mono_palette);
        shougaiPalette = (FrameLayout) view.findViewById(R.id.shougai_palette);
        paletteBg = (ImageView) view.findViewById(R.id.palette_bg);
        paletteFlash = view.findViewById(R.id.platte_flash);
        paletteContainer = (SagashiPaletteFrame) view.findViewById(R.id.palette_container);
        paletteFrame = (FrameLayout) view.findViewById(R.id.palette_frame);

        headerFragment = (SagashiHeaderFragment) getChildFragmentManager().findFragmentById(R.id.headerMenu);
        pauseButton = (BaseButton) view.findViewById(R.id.pause_button);

        cardFrames[0] = (FrameLayout) view.findViewById(R.id.card_frame_1);
        cardFrames[1] = (FrameLayout) view.findViewById(R.id.card_frame_2);
        cardFrames[2] = (FrameLayout) view.findViewById(R.id.card_frame_3);

        //パレットタップ
        paletteContainer.setTapListener(new SagashiPaletteFrame.PaletteTapListener() {
            @Override
            public void tappedPalette(float posX, float posY) {

                //プレイ中以外は操作受けつけない
                if (gameState != State.PLAYING) return;

                tapPallette(posX, posY);
            }

            @Override
            public void zoomedPalette(boolean zoomed) {
                stepNextTutorial();
            }
        });

        //タイマー状態受信
        headerFragment.setSagashiTimerListener(new SagashiHeaderFragment.SagashiTimerListener() {
            @Override
            public void monochromeOver() {
                if (nanidoParam.getColor() > 0) {
                    effectMonochro(nanidoParam.getColor() > 0, true, 0);
                }
            }

            @Override
            public void roteteBackOver() {
                if (nanidoParam.getRotate() > 0) {
                    effectDirection(nanidoParam.getRotate(), true, 0);
                }
            }

            @Override
            public void timeLimitAlert() {
                timeLimitAlertGame();
            }

            @Override
            public void timeLimitAlertClear() {
                timeLimitAlertClearGame();
            }

            @Override
            public void timeLimitOver() {
                timeOverGame();
            }
        });

        //ポーズ
        pauseButton.setOnClickListener(gameStateButtonListener);

        if (!Settings.isDebug) {
            view.findViewById(R.id.dbg_btn_win).setVisibility(View.GONE);
            view.findViewById(R.id.dbg_btn_lose).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.dbg_btn_win).setOnClickListener(gameStateButtonListener);
            view.findViewById(R.id.dbg_btn_lose).setOnClickListener(gameStateButtonListener);
        }

        CacheManager.instance().jikenSousaParam = null;

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        shougaiPalette.removeAllViews();
        palette.removeAllViews();

        paletteBg.setImageBitmap(null);
        paletteBg.setImageDrawable(null);
    }

    private void tapPallette(float posX, float posY) {

        SeManager.play(SeManager.SeName.PUSH_CONTENT);

        boolean tutorialFirstTap = false;

        if (posX == -1 && posY == -1) {
            tutorialFirstTap = true;
        }

        float ratio = (float) palette.getWidth() / 847.0F;
        posX /= ratio;
        posY /= ratio;

        boolean correctFlag = false;

        //タップした箇所に存在する8番目までのターゲット
        for (int i = 0; i < 8; i++) {

            if (i < thisTargetList.size()) {

                LocationParam p = thisTargetList.get(i);
                float aWidth = Math.abs(p.image_width * p.width) / 2 + ITEM_TAP_MARGIN;
                float aHeight = Math.abs(p.image_height * p.height) / 2 + ITEM_TAP_MARGIN;

                Common.logD("p.mono_name:" + p.mono_name);
                Common.logD("p.x:" + p.x + " aWidth:" + aWidth + " posX:" + posX);

                if ((p.x - aWidth < posX
                        && p.x + aWidth > posX
                        && p.y - aHeight < posY
                        && p.y + aHeight > posY) || (tutorialFirstTap && i == 2)) {


                    Common.logD("in p.mono_name:" + p.mono_name);
                    //あたり
                    correctFlag = true;

                    //ターゲット消す
                    for (int k = 0; k < palette.getChildCount(); ++k) {
                        TargetMono child = (TargetMono) palette.getChildAt(k);
                        if (child.param.location_id == p.location_id) {

                            if (child.param.targettedFlag) {
                                //ターゲット強調済みアイテムならターゲットアイコンん削除
                                for (int z = 0; z < shougaiPalette.getChildCount(); ++z) {
                                    View cv = shougaiPalette.getChildAt(z);
                                    if (cv.getClass().toString().equals(TargetIcon.class.toString())) {
                                        TargetIcon targetIcon = (TargetIcon) cv;
                                        if (targetIcon.location_id == child.param.location_id) {
                                            targetIcon.setImageDrawable(null);
                                            shougaiPalette.removeView(targetIcon);
                                        }
                                    }
                                }

                            }
                            child.setImageDrawable(null);
                            palette.removeView(child);
                        }
                    }

                    //ヘッダー更新
                    thisTargetList.remove(i);
                    headerFragment.updateHeaderTargets(thisTargetList, effectOrderFlag);

                    //クリア判定
                    if (thisTargetList.size() == 0) {
                        clearGame();
                    }


                    break;

                }
            }

            //順番指定時は一番上だけ
            if (effectOrderFlag) break;
        }

        if (!correctFlag) {
            //ミスタップ
            missTapCount++;
            if (missTapCount >= MISS_TAP_LIMIT) {
                effectAddTime(-5);
                missTapCount = 0;
            }
        } else {
            missTapCount = 0;
        }
    }

    private View.OnClickListener gameStateButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            SeManager.play(SeManager.SeName.PUSH_BUTTON);

            if (v.equals(pauseButton)) {
                pushPausePopup();
            } else if (v.getId() == R.id.dbg_btn_win) {
                clearGame();
            } else if (v.getId() == R.id.dbg_btn_lose) {
                timeOverGame();
            }
        }
    };

    protected void pushPausePopup() {

        //ステートによってはポーズ画面出さない
        if (gameState == State.STANDBY || gameState == State.FINISHED) {
            return;
        }

        boolean alreadyFlag = false;
        for (Fragment fragment : getChildFragmentManager().getFragments()) {
            if (fragment instanceof PausePopup) {
                alreadyFlag = true;
            }
        }
        if (!alreadyFlag) {

            PausePopup pausePopup = PausePopup.newInstance();
            pausePopup.setPopupButtonListener(new BasePopup.PopupButtonListener() {
                @Override
                public void pushedPositiveClick(BasePopup popup) {
                    giveupGame();
                }

                @Override
                public void pushedNegativeClick(BasePopup popup) {

                }
            });
            showPopup(pausePopup);
        }
    }

    @Override
    protected void fragmentDidAppear() {
        super.fragmentDidAppear();


        ViewTreeObserver observer = palette.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            boolean flag = false;

            @Override
            public void onGlobalLayout() {
                if (!flag) {

                    resetStage();
                    flag = true;

                    // Once data has been obtained, this listener is no longer needed, so remove it...
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        palette.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        palette.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            }
        });
    }

    @Override
    protected void popupsWillAppear() {
        super.popupsWillAppear();

        if (gameState == State.PLAYING) {
            updateGameState(State.PAUSE);
        }
    }

    @Override
    protected void popupsDidDisappear() {
        super.popupsDidDisappear();

        if (gameState == State.PAUSE) {
            updateGameState(State.PLAYING);
        }
    }

    @Override
    protected void fragmentRecieveActivityPause() {
        super.fragmentRecieveActivityPause();

        //ポーズポップアップが無ければ出す
        pushPausePopup();
    }

    @Override
    protected void fragmentRecieveActivityResume() {
        super.fragmentRecieveActivityResume();
    }


    //----------------------------- ゲーム管理

    // 状態更新
    private void updateGameState(State state) {

        switch (state) {

            case STANDBY:
                Common.logD("STANDBY");
                gameState = state;
                pauseButton.setEnabled(false);
                break;

            case PLAYING:
                if (gameState == State.PAUSE) {
                    Common.logD("PLAYING");
                    gameState = state;
                    pauseButton.setEnabled(true);
                    pauseButton.setSelected(true);
                    headerFragment.restartTimer();

                    if (headerFragment.isNearTimeLimit()) {
                        bgmName = BgmManager.BgmName.SOUSA_LIMIT;
                    } else {
                        bgmName = BgmManager.BgmName.SOUSA;
                    }
                    BgmManager.setBgm(bgmName);

                } else if (gameState == State.STANDBY) {
                    Common.logD("PLAYING");
                    gameState = state;
                    pauseButton.setEnabled(true);
                    pauseButton.setSelected(true);
                    headerFragment.startTimer();

                    bgmName = BgmManager.BgmName.SOUSA;
                    BgmManager.setBgm(bgmName);
                }
                break;
            case PAUSE:
                if (gameState == State.PLAYING) {
                    Common.logD("PAUSE");
                    gameState = state;
                    pauseButton.setEnabled(true);
                    pauseButton.setSelected(false);
                    headerFragment.pauseTimer();
                }
                break;
            case FINISHED:
                if (gameState == State.PLAYING) {
                    Common.logD("FINISHED");
                    pauseButton.setEnabled(false);
                    headerFragment.pauseTimer();
                    gameState = state;
                }
                break;
        }

    }

    // ステージリセット
    private void resetStage() {

        updateGameState(State.STANDBY);

        ArrayList<String> tutorialList = SaveManager.stringArray(SaveManager.KEY.TUTORIAL__stringList);
        if (!tutorialList.contains("jiken_sousa")) {
            SaveManager.updateStringArray(SaveManager.KEY.TUTORIAL__stringList, "jiken_5", false);
            SaveManager.updateStringArray(SaveManager.KEY.TUTORIAL__stringList, "jiken_6", false);
            tutorialList = SaveManager.stringArray(SaveManager.KEY.TUTORIAL__stringList);
        }

        //チュートリアル向け設定の必要
        tutorialSetup = !tutorialList.contains("jiken_5");

        thisTargetCount = tutorialSetup ? 3 : nanidoParam.getMono();

        //パレット
        paletteContainer.resetPalette();

        //ロケーションマスタ
        locationList = CsvManager.locationMaster(nanidoParam.getArea());

        //ものマスタ
        monoMap = CsvManager.monoMaster(nanidoParam.getArea());

        //エリアのもの全数
        ArrayList<Integer> targetIdList = new ArrayList<>();
        for (Integer targetID : monoMap.keySet()) {
            if (monoMap.get(targetID).fileName.indexOf("target") == 0) {
                targetIdList.add(targetID);
            }
        }

        //シャッフル
        Collections.shuffle(targetIdList);

        if (jikenParam != null) {
            ArrayList<String> zettaiIreruList = new ArrayList<>();

            if (jikenParam.fuseikaiBusshouList != null)
                zettaiIreruList.addAll(jikenParam.fuseikaiBusshouList);
            if (jikenParam.seikaiBusshou != null)
                zettaiIreruList.add(jikenParam.seikaiBusshou);

            if (jikenParam.suiriMachigaiItem != null)
                zettaiIreruList.addAll(jikenParam.suiriMachigaiItem);
            if (jikenParam.suiriSeikaiItem != null)
                zettaiIreruList.add(jikenParam.suiriSeikaiItem);

            if (jikenParam.hazureItemList != null)
                zettaiIreruList.addAll(jikenParam.hazureItemList);
            if (jikenParam.shoukohin != null)
                zettaiIreruList.add(jikenParam.shoukohin);

            Common.logD("zettaiIreruList:"+zettaiIreruList);

            for (Integer targetID : monoMap.keySet()) {
                MonoParam monoParam = monoMap.get(targetID);
                if (zettaiIreruList.contains(monoParam.name)) {
                    targetIdList.remove(targetID);
                    targetIdList.add(0, targetID);
                }
            }
        }


        //今回のターゲット一覧
        thisTargetList = new ArrayList<>();
        for (int i = 0; i < thisTargetCount; i++) {

            int monoId = targetIdList.get(i).intValue();

            //同じmonoIDのlocationを抽出
            ArrayList<LocationParam> onajiMonoLocationList = new ArrayList<>();

            for (LocationParam tp : locationList) {
                if (tp.target_id == monoId) {
                    onajiMonoLocationList.add(tp);
                }
            }
            //同じmonoIDのlocationからランダムで一つ選ぶ
            Collections.shuffle(onajiMonoLocationList);
            LocationParam param = onajiMonoLocationList.get(0);

            //今回のターゲットに追加
            param.mono_id = monoId;
            param.mono_file = monoMap.get(monoId).fileName;
            param.mono_name = monoMap.get(monoId).name;


            thisTargetList.add(param);
        }

        //シャッフル
        Collections.shuffle(thisTargetList);

        //チュートリアル時のターゲット指定
        if (tutorialSetup) {
            thisTargetList.clear();
            for (LocationParam param : locationList) {
                if (param.location_id == 89) {
                    param.mono_id = 22;
                    param.mono_file = monoMap.get(22).fileName;
                    param.mono_name = monoMap.get(22).name;
                    thisTargetList.add(param);
                }
            }
            for (LocationParam param : locationList) {
                if (param.location_id == 49) {
                    param.mono_id = 11;
                    param.mono_file = monoMap.get(11).fileName;
                    param.mono_name = monoMap.get(11).name;
                    thisTargetList.add(param);
                }
            }
            for (LocationParam param : locationList) {
                if (param.location_id == 29) {
                    param.mono_id = 7;
                    param.mono_file = monoMap.get(7).fileName;
                    param.mono_name = monoMap.get(7).name;
                    thisTargetList.add(param);
                }
            }
        }


        //背景画像
        String largeImagePath = CsvManager.areaImageFileFromAreaID(nanidoParam.getArea(), false);
        paletteBg.setImageBitmap(Common.decodedAssetBitmap(largeImagePath, 556, 315));

        //ターゲット配置
        boolean result = putTargets();

        if (!result && !reDownloadFlag) {

            reDownloadFlag = true;
            if (Settings.isDebug)
                SaveManager.updateBoolValue(SaveManager.KEY.DEBUG_REDOWNLOAD__boolean, true);

            //データ読み込み失敗
            MessagePopup popup = MessagePopup.newInstance("ダウンロードデータを読み込めません。<br>ファイルをダウンロードし直します。");
            popup.setPopupButtonListener(new BasePopup.PopupButtonListener() {
                @Override
                public void pushedPositiveClick(BasePopup popup) {

                }

                @Override
                public void pushedNegativeClick(BasePopup popup) {

                    APIRequest r = new APIRequest();
                    r.name = APIDialogFragment.APIName.FILE_DOWNLOAD;
                    r.params.put("zip", "1");

                    APIDialogFragment download = APIDialogFragment.newInstance(r);
                    download.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
                        @Override
                        public void getAPIResult(APIRequest request, Object object) {

                            if (UserInfoManager.jikenCleared("003", true)) {

                                APIRequest r = new APIRequest();
                                r.name = APIDialogFragment.APIName.FILE_DOWNLOAD;
                                r.params.put("zip", "2");

                                APIDialogFragment download = APIDialogFragment.newInstance(r);
                                download.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
                                    @Override
                                    public void getAPIResult(APIRequest request, Object object) {

                                        if (UserInfoManager.jikenCleared("157", true)) {

                                            APIRequest r = new APIRequest();
                                            r.name = APIDialogFragment.APIName.FILE_DOWNLOAD;
                                            r.params.put("zip", "2");

                                            APIDialogFragment download = APIDialogFragment.newInstance(r);
                                            download.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
                                                @Override
                                                public void getAPIResult(APIRequest request, Object object) {
                                                    resetStage();

                                                }
                                            });
                                            fireApi(download);
                                        } else {

                                            resetStage();
                                        }
                                    }
                                });
                                fireApi(download);

                            } else {
                                resetStage();
                            }

                        }
                    });
                    fireApi(download);
                }
            });
            showPopup(popup);

            return;
        }

        //配置して重なりあった上から順に取っていく
        Collections.reverse(thisTargetList);

        //ルール設定
        //順番
        effectOrderFlag = nanidoParam.getJun();
        //カラー
        effectMonochro(nanidoParam.getColor() > 0, false, 0);

        //反転
        if (nanidoParam.getRotate() > 0) {
            effectDirection(nanidoParam.getRotate(), false, 0);
        }

        //ヘッダー更新
        headerFragment.updateHeaderTargets(thisTargetList, effectOrderFlag);

        //タイマーリセット
        headerFragment.resetTimer(nanidoParam.getTime());

        //カードリセット
        resetCards();


        //チュートリアル
        if (tutorialSetup) {

            //チュートリアルセットアップ後にチュートリアル開始
            startTutorial("jiken_5");

        } else {

            SeManager.play(SeManager.SeName.START_SOUSA);

            SagashiSceneEffect effect = SagashiSceneEffect.newInstance(SagashiSceneEffect.Style.START);
            effect.setEffectListener(new BaseEffect.EffectListener() {
                @Override
                public void effectFinished() {
                    startGame();
                }
            });
            showEffect(effect);
        }
    }


    //---tutorial

    @Override
    public void didEndTutorial() {
        super.didEndTutorial();

        ArrayList<String> tutorialList = SaveManager.stringArray(SaveManager.KEY.TUTORIAL__stringList);
        if (!tutorialList.contains("jiken_6") && tutorialList.contains("jiken_5")) {

            //チュートリアル５終了ごにエフェクト
            SeManager.play(SeManager.SeName.START_SOUSA);

            SagashiSceneEffect effect = SagashiSceneEffect.newInstance(SagashiSceneEffect.Style.START);
            effect.setEffectListener(new BaseEffect.EffectListener() {
                @Override
                public void effectFinished() {
                    startTutorial("jiken_6");
                }
            });
            showEffect(effect);
        } else if (!tutorialList.contains("jiken_7") && tutorialList.contains("jiken_6")) {
            //
            startGame();
        } else if (tutorialList.contains("jiken_7") && tutorialList.contains("jiken_6") && tutorialList.contains("jiken_5")) {
            clearGame();
        }
    }

    @Override
    public void pushedTarget(TutorialParam param) {
        super.pushedTarget(param);

        if (param.sousaCode.indexOf("物証") != -1) {

            SeManager.play(SeManager.SeName.PUSH_CONTENT);
            //注射器タップしたことにする
            tapPallette(-1, -1);

        } else if (param.sousaCode.indexOf("ピンチアウト") != -1) {

        } else if (param.sousaCode.indexOf("ピンチイン") != -1) {
        }
    }

    //---


    //スタート
    private void startGame() {
        removeAllPopup();
        ((BaseActivity) getActivity()).removeNotap();
        updateGameState(State.PLAYING);
    }

    //タイムオーバー
    private void timeOverGame() {

        if (gameState != State.PLAYING) return;

        if (tutorialSetup) {

            //チュートリアルだとコナンくんがクリアしたことにしてくれる
            startTutorial("jiken_7");


        } else {

            updateGameState(State.FINISHED);

            SagashiSceneEffect effect = SagashiSceneEffect.newInstance(SagashiSceneEffect.Style.LOSE);
            effect.setEffectListener(new BaseEffect.EffectListener() {
                @Override
                public void effectFinished() {
                    pushRetryMessage();
                }
            });
            showEffect(effect);

            bgmName = BgmManager.BgmName.SOUSA_FAIL;
            BgmManager.setBgm(bgmName);
        }
    }

    //リトライしませんか
    private void pushRetryMessage() {

        String message = "";
        if (nanidoParam.isKunren)
            message = "もう一度、捜査に挑戦しますか？<br><img src=\"icon_heart\"> x 1消費します。";
        else
            message = "もう一度、捜査に挑戦しますか？<br><img src=\"icon_megane\"> x 1消費します。";

        MessagePopup messagePopup = MessagePopup.newInstance(
                message,
                "いいえ", "はい"
        );
        messagePopup.positiveNoClose = true;
        messagePopup.setPopupButtonListener(new BasePopup.PopupButtonListener() {

            @Override
            public void pushedPositiveClick(BasePopup popup) {

                //通貨足りるか
                boolean taritenaiFlag;

                if (nanidoParam.isKunren)
                    taritenaiFlag = UserInfoManager.heartCount() < 1;
                else
                    taritenaiFlag = UserInfoManager.meganeCount() < 1;

                //足りてないメッセージ
                if (taritenaiFlag) {
                    String message;
                    if (nanidoParam.isKunren)
                        message = "<img src=\"icon_heart\">ハートが足りません。<br><img src=\"icon_heart\">ハートを購入しますか？";
                    else
                        message = "<img src=\"icon_megane\">虫眼鏡が足りません。<br><img src=\"icon_megane\">虫眼鏡を購入しますか？";

                    MessagePopup kounyuuPopup = MessagePopup.newInstance(
                            message,
                            "やめる", "購入"
                    );
                    //kounyuuPopup.positiveNoClose = true;
                    kounyuuPopup.setPopupButtonListener(new BasePopup.PopupButtonListener() {

                        @Override
                        public void pushedPositiveClick(BasePopup popup) {

                            if (nanidoParam.isKunren) {
                                showPopup(HeartPopup.newInstance());
                            } else {
                                showPopup(MeganePopup.newInstance());
                            }
                        }

                        @Override
                        public void pushedNegativeClick(BasePopup popup) {
                            //pushRetryMessage();
                        }
                    });
                    showPopup(kounyuuPopup);
                }
                //足りている
                else {
                    //API
                    APIRequest transactionRequest = new APIRequest();
                    transactionRequest.name = APIDialogFragment.APIName.CONTENT_TRADE;
                    transactionRequest.params.put("proc_id", getString(nanidoParam.isKunren ? R.string.api_proc_trade_kunren : R.string.api_proc_trade_jiken));

                    APIDialogFragment transactionAPI = APIDialogFragment.newInstance(transactionRequest);
                    transactionAPI.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
                        @Override
                        public void getAPIResult(APIRequest request, Object object) {

                            //トランザクション
                            APIResponseParam param = (APIResponseParam) object;
                            String transaction = new Gson().toJson(param.item.transaction);
                            String procID = (String) request.params.get("proc_id");

                            //トランザクション消費
                            APIRequest transactionRequest = new APIRequest();
                            transactionRequest.name = APIDialogFragment.APIName.TRANSACTION_COMMIT;
                            transactionRequest.transactionString = transaction;
                            transactionRequest.params.put("proc_id", procID);

                            APIDialogFragment fireAPI = APIDialogFragment.newInstance(transactionRequest);
                            fireAPI.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
                                @Override
                                public void getAPIResult(APIRequest request, Object object) {

                                    //ゲーム再スタート
                                    resetStage();


                                }
                            });
                            fireApi(fireAPI);

                        }
                    });
                    fireApi(transactionAPI);
                }
            }

            @Override
            public void pushedNegativeClick(BasePopup popup) {
                popActivity();
            }
        });
        showPopup(messagePopup);
    }

    //タイムリミット間近
    private void timeLimitAlertGame() {
        bgmName = BgmManager.BgmName.SOUSA_LIMIT;
        BgmManager.setBgm(bgmName);
    }

    //タイムリミット間近改善
    private void timeLimitAlertClearGame() {
        bgmName = BgmManager.BgmName.SOUSA;
        BgmManager.setBgm(bgmName);
    }

    //クリアー
    private void clearGame() {

        updateGameState(State.FINISHED);

        bgmName = BgmManager.BgmName.SOUSA_CLEAR;
        BgmManager.setBgm(bgmName);

        SagashiSceneEffect effect = SagashiSceneEffect.newInstance(SagashiSceneEffect.Style.WIN);
        effect.setEffectListener(new BaseEffect.EffectListener() {
            @Override
            public void effectFinished() {


                //訓練クリアー
                if (nanidoParam.isKunren) {

                    //クリア通知
                    APIRequest clearRequest = new APIRequest();
                    clearRequest.name = APIDialogFragment.APIName.KUNREN_CLEAR;
                    clearRequest.params.put("area_id", nanidoParam.getArea());
                    clearRequest.params.put("level", nanidoParam.getLevel());

                    APIDialogFragment clearAPI = APIDialogFragment.newInstance(clearRequest);
                    clearAPI.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
                        @Override
                        public void getAPIResult(APIRequest request, Object object) {

                            final APIResponseParam responseParam = (APIResponseParam) object;

                            ArrayList<Integer> list = new ArrayList<>();
                            for (APIResponseParam.Item.Card card : responseParam.item.get_card) {
                                list.add(card.card_id);
                            }

                            //クリア報酬
                            KunrenRewardDialogFragment kunrenRewardDialogPopup = KunrenRewardDialogFragment.newInstance(
                                    list.get(0)
                            );
                            kunrenRewardDialogPopup.setPopupDisplayListener(new BasePopup.PopupDisplayListener() {

                                @Override
                                public void didShowPopup(BasePopup popup) {
                                    SeManager.play(SeManager.SeName.REWARD_KUNREN);
                                }

                                @Override
                                public void didClosePopup(BasePopup popup) {

                                    //ミッション報酬
                                    if (responseParam.item.clear_kunren.reward != null && responseParam.item.clear_kunren.reward.reward != null) {

                                        String areaName = CsvManager.areaNameFromAreaId(String.valueOf(nanidoParam.getArea()));

                                        String title = areaName + "Lv." + nanidoParam.getLevel() + "を" + responseParam.item.clear_kunren.clear_count + "回クリア";
                                        String item = responseParam.item.clear_kunren.reward.reward;

                                        if (item.indexOf("虫眼鏡") >= 0) {
                                            item = "<img src=\"icon_megane\">虫眼鏡 x " + responseParam.item.clear_kunren.reward.amount;
                                        } else if (item.indexOf("チケット") >= 0) {
                                            item = "<img src=\"icon_ticket\">ガチャチケット x " + responseParam.item.clear_kunren.reward.amount;
                                        } else if (item.indexOf("開放") >= 0) {
                                            areaName = CsvManager.areaNameFromAreaId(responseParam.item.clear_kunren.reward.kaihou_area);
                                            item = areaName + "Lv." + responseParam.item.clear_kunren.reward.kaihou_level;
                                        }

                                        MissionRewardDialogFragment missionRewardPopup = MissionRewardDialogFragment.newInstance(
                                                title,
                                                item
                                        );
                                        missionRewardPopup.setPopupDisplayListener(new BasePopup.PopupDisplayListener() {
                                            @Override
                                            public void didShowPopup(BasePopup popup) {
                                                SeManager.play(SeManager.SeName.REWARD_MISSION);
                                            }

                                            @Override
                                            public void didClosePopup(BasePopup popup) {
                                                popActivity();
                                            }
                                        });
                                        showPopup(missionRewardPopup);
                                    } else {
                                        popActivity();
                                    }
                                }
                            });
                            showPopup(kunrenRewardDialogPopup);

                        }

                    });
                    fireApi(clearAPI);
                }
                //事件クリアー
                else {

                    if (tutorialSetup) {

                        SaveManager.updateStringArray(SaveManager.KEY.TUTORIAL__stringList, "jiken_sousa", true);
                        SaveManager.updateJikenShinchoku("001", 2);
                        CacheManager.instance().jikenSousaParam = null;
                        popActivity();
                        return;
                    }
                    //進捗
                    SaveManager.updateJikenShinchoku(jikenParam.jikenID, 2);

                    //暗号型
                    if (jikenParam.angouChouhenFlag) {

                        //新たに暗号取得
                        ArrayList<String> newAngouList = SaveManager.getNewAngouList(jikenParam.jikenID, jikenParam.angouString, jikenParam.kakushiAngouString);

                        //暗号入手リザルト
                        if (newAngouList.size() > 0) {
                            AngouRewardPopup angouRewardPopup = AngouRewardPopup.newInstance(
                                    newAngouList
                            );
                            angouRewardPopup.setPopupDisplayListener(new BasePopup.PopupDisplayListener() {

                                @Override
                                public void didShowPopup(BasePopup popup) {

                                }

                                @Override
                                public void didClosePopup(BasePopup popup) {
                                    popActivity();
                                }
                            });
                            showPopup(angouRewardPopup);
                        } else {
                            popActivity();
                        }

                    }
                    //推理型
                    else if (jikenParam.suiriChouhenFlag) {

                        //聞き込み可能にする
                        SaveManager.updateCanKikikomi(jikenParam.jikenID, true);
                        popActivity();
                    }
                    //証言型
                    else if (jikenParam.shougenChouhenFlag) {

                        //聞き込み可能にする
                        SaveManager.updateCanKikikomi(jikenParam.jikenID, true);
                        popActivity();
                    }
                    //通常事件
                    else {
                        if (tutorialSetup) {
                            SaveManager.updateStringArray(SaveManager.KEY.TUTORIAL__stringList, "jiken_sousa", true);
                            SaveManager.updateJikenShinchoku("001", 2);
                        }
                        popActivity();
                    }
                }
            }
        });
        showEffect(effect);
    }

    //ギブアップ
    private void giveupGame() {
        popActivity();
    }

    //終了
    private void popActivity() {


        for (Fragment fragment : getChildFragmentManager().getFragments()) {
            if (fragment instanceof BasePopup) {
                ((BasePopup) fragment).hideSound = null;
            }
        }

        if (nanidoParam.isKunren) {
            ((BaseActivity) getActivity()).replaceViewController(KunrenFragment.newInstance());
        } else {
            ((BaseActivity) getActivity()).replaceViewController(JikenFragment.newInstance(jikenParam));
        }

    }


    //----------------------------- カード

    /**
     * カードの状態リセット
     */
    private void resetCards() {

        //デッキのカードシリアル
        ArrayList<Integer> serialIdList = SaveManager.deckListByDeckIndex(-1);

        //所有カード一覧
        ArrayList<APIResponseParam.Item.Card> myCardList = UserInfoManager.myCardList();

        Map<Integer, CardParam> cardParamMap = CsvManager.cardParamsWithSkillDetail();

        //左に表示するカード一覧
        cardList = new ArrayList<>();

        //所有カード一覧から表示カードデータを参照して効果情報を得る
        for (int serialID : serialIdList) {
            cardList.add(null);
            for (APIResponseParam.Item.Card card : myCardList) {
                if (card.id == serialID) {
                    cardList.remove(cardList.size() - 1);
                    cardList.add(cardParamMap.get(card.card_id));
                    break;
                }
            }
        }

        //カードフレームの状態をリセット
        for (int i = 0; i < 3; i++) {

            cardFrames[i].setAlpha(1.0f);

            final CardParam param = cardList.get(i);

            //ボタン
            BaseButton button = (BaseButton) cardFrames[i].findViewById(R.id.cell_egara);
            button.setAlpha(1f);
            button.setOnTouchListener(null);
            if (param != null) {

                //ボタン
                button.setEnabled(true);
                button.setImageBitmap(Common.decodedAssetBitmap("egara/180x254/" + param.cardID + ".jpg", 54, 76));
                final int finalI = i;

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (gameState != State.PLAYING) return;

                        //ポーズ
                        ((BaseActivity) getActivity()).addNotap();
                        headerFragment.pauseTimer();

                        v.setEnabled(false);
                        cardFrames[finalI].setAlpha(0.5f);


                        SeManager.play(SeManager.SeName.PUSH_BUTTON);


                        CardEffect cardEffect = CardEffect.newInstance(param.cardID);
                        cardEffect.setEffectListener(new BaseEffect.EffectListener() {
                            @Override
                            public void effectFinished() {

                                switch (param.skillType) {

                                    case Time:
                                        effectAddTime(param.skillValue);
                                        break;
                                    case Direction:
                                        effectDirection(0, true, param.skillValue);
                                        break;
                                    case Color:
                                        effectMonochro(false, true, param.skillValue);
                                        break;
                                    case Target:
                                        effectTarget(param.skillValue);
                                        break;
                                    case Number:
                                        effectOrderFlag = false;
                                        headerFragment.updateHeaderTargets(thisTargetList, effectOrderFlag);
                                        break;
                                }

                                headerFragment.restartTimer();
                                ((BaseActivity) getActivity()).removeNotap();

                            }
                        });
                        showEffect(cardEffect);

                    }
                });

                //フレーム
                cardFrames[i].findViewById(R.id.cell_frame).setVisibility(View.VISIBLE);

                //スキルアイコン
                ImageView skillImage = (ImageView) cardFrames[i].findViewById(R.id.cell_skill);
                skillImage.setVisibility(View.VISIBLE);
                switch (param.skillType) {
                    case Time:
                        skillImage.setImageBitmap(Common.decodedResource(R.mipmap.skill_time, 14, 14));
                        break;
                    case Direction:
                        skillImage.setImageBitmap(Common.decodedResource(R.mipmap.skill_direction, 14, 14));
                        break;
                    case Color:
                        skillImage.setImageBitmap(Common.decodedResource(R.mipmap.skill_color, 14, 14));
                        break;
                    case Target:
                        skillImage.setImageBitmap(Common.decodedResource(R.mipmap.skill_target, 14, 14));
                        break;
                    case Number:
                        skillImage.setImageBitmap(Common.decodedResource(R.mipmap.skill_order, 14, 14));
                        break;
                }
            } else {
                button.setEnabled(false);
                cardFrames[i].findViewById(R.id.cell_frame).setVisibility(View.INVISIBLE);
                cardFrames[i].findViewById(R.id.cell_skill).setVisibility(View.GONE);
            }
        }
    }


    //----------------------------- 効果

    // エフェクト時の画面フラッシュ
    private void effectFlash(boolean start) {

        if (start) {
            paletteFlash.setAlpha(1.0F);
            paletteFlash.setVisibility(View.VISIBLE);
        } else {
            AlphaAnimation feedout = new AlphaAnimation(1, 0);
            feedout.setDuration(300);
            feedout.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    paletteFlash.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            paletteFlash.startAnimation(feedout);
        }
    }

    // 効果：カラー
    private void effectMonochro(boolean effect, boolean animate, int effectValue) {

        if (animate) effectFlash(true);

        //モノクロはフィルタで対応

        ColorMatrixColorFilter filter = null;
        effectMonochroFlag = effect;

        //背景
        if (effect) {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            filter = new ColorMatrixColorFilter(matrix);
        }

        paletteBg.setColorFilter(filter);

        //もの
        for (int i = 0; i < palette.getChildCount(); ++i) {
            View child = palette.getChildAt(i);
            if (child.getClass().toString().equals(TargetMono.class.toString())) {
                ((TargetMono) child).setColorFilter(filter);
            }
        }

        //障害物
        for (int i = 0; i < shougaiPalette.getChildCount(); ++i) {
            View child = shougaiPalette.getChildAt(i);
            if (child.getClass().toString().equals(TargetMono.class.toString())) {
                ((TargetMono) child).setColorFilter(filter);
            }
        }

        if (animate) effectFlash(false);

        if (gameState == State.PLAYING || gameState == State.PAUSE) {
            if (effectMonochroFlag) {
                SeManager.play(SeManager.SeName.SKILL_MONOKURO);
            } else {
                SeManager.play(SeManager.SeName.SKILL_COLORED);
            }
        }

        headerFragment.setMonochromeTime(effectValue);
    }

    // 効果：向き
    private void effectDirection(int value, boolean animate, int effectValue) {

        if (animate) effectFlash(true);

        effectDirectionValue = value;

        paletteContainer.updateRotation(value);

        if (animate) effectFlash(false);


        if (gameState == State.PLAYING || gameState == State.PAUSE) {
            SeManager.play(SeManager.SeName.SKILL_ROTATION);
        }

        headerFragment.setRotateBackTime(effectValue);
    }


    // 効果：ターゲット強調
    private void effectTarget(int count) {

        for (int k = palette.getChildCount() - 1; k >= 0; --k) {
            TargetMono child = (TargetMono) palette.getChildAt(k);
            if (!child.param.targettedFlag) {
                child.param.targettedFlag = true;

                //ものと同じ位置の障害物レイヤーにターゲットアイコン表示
                TargetIcon view = new TargetIcon(getContext());
                Common.logD(" " + child.getX() + " " + child.getWidth() + " " + view.size.x);
                view.setX(child.getX() + child.getWidth() / 2.0f - view.size.x / 2);
                view.setY(child.getY() + child.getHeight() / 2.0f - view.size.y / 2);
                view.location_id = child.param.location_id;
                shougaiPalette.addView(view);
                count--;

                view.startAnimation();


            }
            if (count <= 0) break;
        }

        if (gameState == State.PLAYING || gameState == State.PAUSE) {
            SeManager.play(SeManager.SeName.SKILL_TARGET);
        }
    }

    // 効果：時間延長
    private void effectAddTime(int count) {

        headerFragment.addTimer(count);

        final ImageView imageView = new ImageView(getContext());
        if (count == -5) imageView.setImageResource(R.mipmap.effect_time_m5);
        else if (count == 5) imageView.setImageResource(R.mipmap.effect_time_5);
        else if (count == 10) imageView.setImageResource(R.mipmap.effect_time_10);
        else if (count == 15) imageView.setImageResource(R.mipmap.effect_time_15);
        else if (count == 20) imageView.setImageResource(R.mipmap.effect_time_20);
        else if (count == 25) imageView.setImageResource(R.mipmap.effect_time_25);
        else if (count == 30) imageView.setImageResource(R.mipmap.effect_time_30);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        Point size = new Point();
        size.x = (int) (60 * displayMetrics.density * 2);
        size.y = (int) (32 * displayMetrics.density * 2);
        FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(
                size.x, size.y
        );

        layout.gravity = Gravity.CENTER_HORIZONTAL;
        imageView.setLayoutParams(layout);

        float posY = (float) (paletteFrame.getHeight() / 2.0 - size.y);
        imageView.setY(posY);

        paletteFrame.addView(imageView);

        ViewCompat.animate(imageView)
                .alpha(0f)
                .setStartDelay(1000)
                .y(posY + 100)
                .setDuration(600)
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(View view) {
                        ((ImageView) view).setImageDrawable(null);
                        paletteFrame.removeView(view);
                        //view = null;
                    }
                })
                .start();

        if (gameState == State.PLAYING || gameState == State.PAUSE) {
            if (count > 0) {
                SeManager.play(SeManager.SeName.SKILL_TIME_PLUS);
            } else {
                SeManager.play(SeManager.SeName.SKILL_TIME_MINUS);
            }
        }

    }


    //----------------------------- 配置・調整

    // ターゲット配置
    private boolean putTargets() {

        float ratio = (float) palette.getWidth() / 847.0F;

        Common.logD("palette.getWidth() ::" + palette.getWidth());

        //全ターゲット・障害物削除
        shougaiPalette.removeAllViews();
        palette.removeAllViews();


        for (LocationParam param : thisTargetList) {

            //ターゲット配置
            TargetMono monoView = new TargetMono(getContext());
            param = monoView.setImage(param, ratio);

            palette.addView(monoView);

            if (param == null) {

                //画像がエラーで読み込めなかった
                return false;

            } else {


                //障害物配置
                if (param.obstacle_id > 0) {

                    for (LocationParam obParam : locationList) {
                        if (obParam.location_id == param.obstacle_id) {

                            //Common.logD("monoMap.get(obParam.target_id).fileName:"+monoMap.get(obParam.target_id).fileName);
                            obParam.obstacle_flag = true;
                            obParam.obstacle_file = monoMap.get(obParam.target_id).fileName;

                            TargetMono shougaiView = new TargetMono(getContext());
                            shougaiView.setImage(obParam, ratio);
                            shougaiPalette.addView(shougaiView);

                            break;
                        }
                    }

                }
            }
        }
        return true;
    }

}
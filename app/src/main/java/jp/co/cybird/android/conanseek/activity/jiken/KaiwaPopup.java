package jp.co.cybird.android.conanseek.activity.jiken;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.EventListener;

import jp.co.cybird.android.conanseek.common.BaseButton;
import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.manager.BgmManager;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.CsvManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.param.KaiwaParam;
import jp.souling.android.conanseek01.R;

/**
 * 会話シーン
 */
public class KaiwaPopup extends BasePopup {

    int currentPage = 0;
    ArrayList<KaiwaParam> kaiwaData;

    //読み進められるフラグ
    boolean canTapNext;


    FrameLayout paletteContainer;
    FrameLayout kaiwaContainer;

    ImageView dialogBgImage;
    ImageView leftCharaImage;
    ImageView centerCharaImage;
    ImageView rightCharaImage;
    ImageView leftKomadoImage;
    ImageView centerKomadoImage;
    ImageView rightKomadoImage;
    ImageView angouImage;
    String leftCharacterName;
    String centerCharacterName;
    String rightCharacterName;

    TextView hatsugenshaLabel;
    TextView serifuLabel;

    BaseButton skipButton;

    String kyouseiHaikeiName;


    //非発言者を暗くするフィルター
    ColorMatrixColorFilter darkerFilter;

    private String fileName;
    private String titleName;

    public static KaiwaPopup newInstance(String fileName, String title, String haikeiName) {

        Bundle args = new Bundle();

        args.putString("fileName", fileName);
        args.putString("title", title);
        args.putString("haikeiName", haikeiName);

        KaiwaPopup fragment = new KaiwaPopup();
        fragment.setArguments(args);
        return fragment;
    }

    public static KaiwaPopup newInstance(String fileName, String title) {
        return newInstance(fileName, title, null);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.popup_kaiwa, container, false);

        Bundle arg = getArguments();
        if (arg != null) {
            fileName = arg.getString("fileName");
            titleName = arg.getString("title");
            kyouseiHaikeiName = arg.getString("haikeiName");
        }

        ((TextView) view.findViewById(R.id.kaiwa_title_text)).setText(titleName);
        if (titleName == null) {
            view.findViewById(R.id.title_frame).setVisibility(View.GONE);
        }


        skipButton = (BaseButton) view.findViewById(R.id.dialog_close);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SeManager.play(SeManager.SeName.PUSH_BUTTON);
                if (buttonListener != null) {
                    buttonListener.pushedNegativeClick(KaiwaPopup.this);
                } else {
                    removeMe();
                }
            }
        });

        kaiwaData = CsvManager.kaiwaData(fileName);

        paletteContainer = (FrameLayout) view.findViewById(R.id.palette_container);
        paletteContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canTapNext) {

                    SeManager.play(SeManager.SeName.PUSH_CONTENT);
                    next();
                }
            }
        });
        kaiwaContainer = (FrameLayout) view.findViewById(R.id.kaiwa_container);

        dialogBgImage = (ImageView) view.findViewById(R.id.image_dialog_bg);

        leftCharaImage = (ImageView) view.findViewById(R.id.kaiwa_chara_1);
        centerCharaImage = (ImageView) view.findViewById(R.id.kaiwa_chara_2);
        rightCharaImage = (ImageView) view.findViewById(R.id.kaiwa_chara_3);

        leftKomadoImage = (ImageView) view.findViewById(R.id.komado_chara_1);
        centerKomadoImage = (ImageView) view.findViewById(R.id.komado_chara_2);
        rightKomadoImage = (ImageView) view.findViewById(R.id.komado_chara_3);

        angouImage = (ImageView) view.findViewById(R.id.angou);

        hatsugenshaLabel = (TextView) view.findViewById(R.id.kaiwa_hito_text);
        serifuLabel = (TextView) view.findViewById(R.id.kaiwa_message_text);

        ColorMatrix matrix = new ColorMatrix();
        float b = -50f;
        float c = 0.9f;
        matrix.set(new float[]{
                c, 0, 0, 0, b,
                0, c, 0, 0, b,
                0, 0, c, 0, b,
                0, 0, 0, 1, 0
        });
        darkerFilter = new ColorMatrixColorFilter(matrix);

        Common.logD("onActivityCreated");

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();

        if (fileName.contains("chouhen")) {
            BgmManager.setBgm(BgmManager.BgmName.KAIWA_CHOUHEN);
        } else {
            BgmManager.setBgm(BgmManager.BgmName.KAIWA_NORMAL);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (fileName.contains("jiken") || fileName.contains("chouhen")) {
            BgmManager.setBgm(BgmManager.BgmName.JIKEN);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        currentPage = -1;
        next();
    }

    public void next() {
        next(false);
    }

    public void next(boolean forced) {
        canTapNext = false;

        if (currentPage == -1) {
            currentPage++;
            update();
            return;
        }
        if (forced || detectShowFullSefifu()) {
            currentPage++;
            if (currentPage >= kaiwaData.size()) {
                currentPage = kaiwaData.size() - 1;
                if (buttonListener != null) {
                    buttonListener.pushedNegativeClick(KaiwaPopup.this);
                } else {
                    removeMe();
                }
            } else {
                update();
            }
        }
    }

    public void update() {

        canTapNext = true;

        KaiwaParam param = kaiwaData.get(currentPage);


        float imageSizeRatio = 0.5f;

        if (param.effectCode.length() > 0) {
            if (effectListener != null)
                effectListener.kaiwaEffectCode(param.effectCode);
            typingDone = true;
        }

        //背景
        if (param.haikeiName.length() > 0) {

            String areaID;
            String haikeiID;

            if (kyouseiHaikeiName != null && kyouseiHaikeiName.length() > 0) {
                haikeiID = CsvManager.haikeiIdFromHaikeiName(kyouseiHaikeiName);
                areaID = CsvManager.areaIdFromAreaName(kyouseiHaikeiName);
            } else {
                haikeiID = CsvManager.haikeiIdFromHaikeiName(param.haikeiName);
                areaID = CsvManager.areaIdFromAreaName(param.haikeiName);
            }
            Common.logD("haikeiID:" + haikeiID + " areaID:" + areaID);

            Bitmap bitmap = null;
            if (haikeiID != null && haikeiID.length() > 0) {
                bitmap = Common.decodedAssetBitmap("haikei/" + haikeiID + ".jpg", 540, 310, imageSizeRatio);
            }
            if (bitmap == null && areaID != null && areaID.length() > 0) {
                bitmap = Common.decodedAssetBitmap("area/" + areaID + ".jpg", 540, 310, imageSizeRatio);
            }
            dialogBgImage.setImageBitmap(bitmap);

        }

        boolean machineFlag = param.hatsugensha.indexOf("探偵体験マシーン") == 0;
        boolean prologueFlag = fileName.indexOf("0/chutorial") != -1;

        if (machineFlag) {
            kaiwaContainer.setVisibility(View.GONE);
        } else {
            kaiwaContainer.setVisibility(View.VISIBLE);
        }

        //立ち絵
        if (param.centerChara.name.length() > 0) {
            centerCharacterName = "";
            if (param.centerChara.name.equals("非表示")) {
                centerCharaImage.setImageBitmap(null);
                centerKomadoImage.setImageBitmap(null);
                angouImage.setImageBitmap(null);
            } else if (param.centerChara.komadoFlag) {


                if (param.centerChara.name.equals("暗号")) {

                    angouImage.setImageBitmap(Common.decodedBitmap(
                            CsvManager.komadoBitmapPathFromName(param.centerChara.name),
                            300, 200
                    ));
                    centerKomadoImage.setImageBitmap(null);

                } else {

                    centerKomadoImage.setImageBitmap(Common.decodedBitmap(
                            CsvManager.komadoBitmapPathFromName(param.centerChara.name),
                            100, 100
                    ));
                    angouImage.setImageBitmap(null);
                }

                centerCharaImage.setImageBitmap(null);
            } else {

                centerCharaImage.setImageBitmap(Common.decodedBitmap(
                        CsvManager.bitmapImagePath("chara", String.valueOf(param.centerChara.charaID), String.valueOf(param.centerChara.hyoujouID), "png"),
                        500, 300,
                        imageSizeRatio
                ));
                centerKomadoImage.setImageBitmap(null);
                centerCharacterName = param.centerChara.name;
            }
        }
        if (param.leftChara.name.length() > 0) {
            leftCharacterName = "";
            if (param.leftChara.name.equals("非表示")) {
                leftKomadoImage.setImageBitmap(null);
                leftCharaImage.setImageBitmap(null);
            } else if (param.leftChara.komadoFlag) {
                leftKomadoImage.setImageBitmap(Common.decodedBitmap(
                        CsvManager.komadoBitmapPathFromName(param.leftChara.name),
                        100, 100
                ));
                leftCharaImage.setImageBitmap(null);
            } else {
                leftCharaImage.setImageBitmap(Common.decodedBitmap(
                        CsvManager.bitmapImagePath("chara", String.valueOf(param.leftChara.charaID), String.valueOf(param.leftChara.hyoujouID), "png"),
                        500, 300,
                        imageSizeRatio
                ));
                leftKomadoImage.setImageBitmap(null);
                leftCharacterName = param.leftChara.name;
            }
        }
        if (param.rightChara.name.length() > 0) {
            rightCharacterName = "";
            if (param.rightChara.name.equals("非表示")) {
                rightKomadoImage.setImageBitmap(null);
                rightCharaImage.setImageBitmap(null);
            } else if (param.rightChara.komadoFlag) {
                rightKomadoImage.setImageBitmap(Common.decodedBitmap(
                        CsvManager.komadoBitmapPathFromName(param.rightChara.name),
                        100, 100
                ));
                rightCharaImage.setImageBitmap(null);
            } else {
                Common.logD("name:" + param.rightChara.name + " id:" + param.rightChara.charaID);
                rightCharaImage.setImageBitmap(Common.decodedBitmap(
                        CsvManager.bitmapImagePath("chara", String.valueOf(param.rightChara.charaID), String.valueOf(param.rightChara.hyoujouID), "png"),
                        500, 300,
                        imageSizeRatio
                ));
                rightKomadoImage.setImageBitmap(null);
                rightCharacterName = param.rightChara.name;
            }
        }

        //発言者
        hatsugenshaLabel.setText(param.hatsugensha);

        if (param.hatsugensha.equals(leftCharacterName)) {
            leftCharaImage.setColorFilter(null);
            if (!prologueFlag) {
                centerCharaImage.setColorFilter(darkerFilter);
                rightCharaImage.setColorFilter(darkerFilter);
            }
            leftCharaImage.bringToFront();
        } else if (param.hatsugensha.equals(centerCharacterName)) {
            centerCharaImage.setColorFilter(null);
            if (!prologueFlag) {
                leftCharaImage.setColorFilter(darkerFilter);
                rightCharaImage.setColorFilter(darkerFilter);
            }
            centerCharaImage.bringToFront();
        } else if (param.hatsugensha.equals(rightCharacterName)) {
            rightCharaImage.setColorFilter(null);
            if (!prologueFlag) {
                leftCharaImage.setColorFilter(darkerFilter);
                centerCharaImage.setColorFilter(darkerFilter);
            }
            rightCharaImage.bringToFront();
        }


        //セリフ
        updateSerifu(param.serifu);
        //serifuLabel.setText(param.serifu);

        //エフェクト
        AlphaAnimation animation = null;

        if (param.fadeIn) {
            animation = new AlphaAnimation(0, 1);
        }
        if (param.fadeOut) {
            animation = new AlphaAnimation(1, 0);
        }
        if (animation != null) {

            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    kaiwaContainer.setVisibility(View.GONE);
                    skipButton.setEnabled(false);
                    canTapNext = false;
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    kaiwaContainer.setVisibility(View.VISIBLE);
                    skipButton.setEnabled(true);
                    canTapNext = true;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            animation.setDuration(1300);
            paletteContainer.setAnimation(animation);
        } else {
            canTapNext = true;
        }
    }

    //------------------ 文字タイピング


    private String serifuFullString;
    private boolean typingDone;
    private int serifuIndex = 0;
    private String putSerifuString;

    private void updateSerifu(String serifu) {
        serifuFullString = serifu;
        serifuIndex = 0;
        typingDone = false;
        putSerifuString = "";

        typingHandler.sendEmptyMessage(1);
    }

    private boolean detectShowFullSefifu() {

        if (typingDone) return true;

        serifuLabel.setText(serifuFullString);
        serifuIndex = serifuFullString.length();
        typingDone = true;

        canTapNext = true;

        return false;
    }

    private Handler typingHandler = new Handler() {


        @Override
        public void dispatchMessage(Message message) {

            if (canTapNext) {


                char data[] = serifuFullString.toCharArray();

                int arr_num = data.length;

                if (serifuIndex < arr_num) {
                    if (message.what == 1) {
                        String word = String.valueOf(data[serifuIndex]);
                        putSerifuString += word;

                        serifuLabel.setText(putSerifuString);
                        typingHandler.sendEmptyMessageDelayed(1, 10);
                        serifuIndex++;
                    } else {
                        super.dispatchMessage(message);
                    }
                } else {
                    typingDone = true;
                }
            } else {
                typingHandler.sendEmptyMessageDelayed(1, 10);
            }
        }
    };


    public interface KaiwaEffectListener extends EventListener {
        void kaiwaEffectCode(String code);
    }

    protected KaiwaEffectListener effectListener = null;

    public void setKaiwaEffectListener(KaiwaEffectListener l) {
        effectListener = l;
    }
}

package jp.co.cybird.android.conanseek.activity.jiken;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.common.MessagePopup;
import jp.co.cybird.android.conanseek.manager.BgmManager;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.CsvManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.param.APIResponseParam;
import jp.co.cybird.android.conanseek.param.JikenParam;
import jp.souling.android.conanseek01.R;

/**
 * 推理：結果
 */
public class SuiriResultPopup extends BasePopup {

    private String myRewardString;

    private String jikenID;
    private boolean result;
    private String yougisha;
    private String busshou;
    private String douki;
    private String reward;

    public static SuiriResultPopup newInstance(String jikenID, boolean result, String yougisha, String busshou, String douki, Object reward) {

        Bundle args = new Bundle();

        args.putString("jikenID", jikenID);
        args.putBoolean("result", result);
        args.putString("yougisha", yougisha);
        args.putString("busshou", busshou);
        args.putString("douki", douki);
        args.putBoolean("tutorial", false);

        if (reward != null && reward.toString().indexOf("0") != 0) {

            APIResponseParam.Item.JikenClear param = new Gson().fromJson(reward.toString(), new TypeToken<APIResponseParam.Item.JikenClear>() {
            }.getType());

            args.putString("reward", param.reward);
        }

        SuiriResultPopup fragment = new SuiriResultPopup();
        fragment.setArguments(args);
        return fragment;
    }

    public static SuiriResultPopup newInstance(String jikenID, boolean result, String yougisha, String busshou, String douki, String reward, boolean tutorial) {

        Bundle args = new Bundle();

        args.putString("jikenID", jikenID);
        args.putBoolean("result", result);
        args.putString("yougisha", yougisha);
        args.putString("busshou", busshou);
        args.putString("douki", douki);
        args.putString("reward", reward);
        args.putBoolean("tutorial", tutorial);

        SuiriResultPopup fragment = new SuiriResultPopup();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onStart() {
        super.onStart();

        if (result) {
            BgmManager.setBgm(BgmManager.BgmName.JIKEN_CLEAR);
        } else {
            BgmManager.setBgm(BgmManager.BgmName.JIKEN_FAIL);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (result) {
            showSound = SeManager.SeName.CLEAR_SOUSA;
        } else {
            showSound = SeManager.SeName.FAIL_SOUSA;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.popup_suiri_result, container, false);


        Bundle arg = getArguments();
        if (arg != null) {
            jikenID = arg.getString("jikenID");
            result = arg.getBoolean("result");
            yougisha = arg.getString("yougisha");
            busshou = arg.getString("busshou");
            douki = arg.getString("douki");
            reward = arg.getString("reward");
        }


        view.findViewById(R.id.btn_ending).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                SeManager.play(SeManager.SeName.PUSH_BUTTON);

                if (myRewardString != null) {


                    MessagePopup messagePopup = MessagePopup.newInstance(myRewardString);
                    messagePopup.setPopupDisplayListener(new PopupDisplayListener() {

                        @Override
                        public void didShowPopup(BasePopup popup) {

                        }

                        @Override
                        public void didClosePopup(BasePopup popup) {
                            if (buttonListener != null)
                                buttonListener.pushedPositiveClick(SuiriResultPopup.this);
                        }
                    });
                    showPopupFromPopup(messagePopup);

                } else {

                    if (buttonListener != null) {
                        buttonListener.pushedPositiveClick(SuiriResultPopup.this);
                    }
                }
            }
        });

        ArrayList<JikenParam> jikenList = CsvManager.jikenMaster();

        for (JikenParam param : jikenList) {
            if (param.jikenID.equals(jikenID)) {

                //事件マスタに事件の詳細情報付加
                param = CsvManager.addJikenDetail(param);

                //title
                ((TextView) view.findViewById(R.id.dialog_title_text)).setText(param.jikenName);

                //background
                String areaID = CsvManager.areaIdFromAreaName(param.sagashiStage);
                String largeImagePath = CsvManager.areaImageFileFromAreaID(Common.parseInt(areaID), false);

                ImageView dialogBgImage = (ImageView) view.findViewById(R.id.image_dialog_bg);

                dialogBgImage.setImageBitmap(Common.decodedAssetBitmap(largeImagePath, 80, 80));

                //iraisha
                String tachieID = CsvManager.tachieIdFromCharacterName(param.iraishaName);
                ((ImageView) view.findViewById(R.id.tachie_image)).setImageBitmap(Common.decodedBitmap(
                        CsvManager.bitmapImagePath("chara", tachieID, "1", "png"),
                        300, 180,
                        0.5f
                ));

                if (param.angouChouhenFlag) {
                    ((TextView) view.findViewById(R.id.iraisha_text)).setText("出題者 / " + param.iraishaName);
                } else {
                    ((TextView) view.findViewById(R.id.iraisha_text)).setText("依頼者 / " + param.iraishaName);
                }

                //result:won
                if (result) {

                    //負け用デザイン排除
                    view.findViewById(R.id.content_lose).setVisibility(View.GONE);

                    TextView rewardText = (TextView) view.findViewById(R.id.reward_text);
                    ImageView rewardImage = (ImageView) view.findViewById(R.id.reward_icon);

                    if (reward != null && reward.length() > 2) {

                        String message = "";
                        String imageHTML = "";

                        if (reward.indexOf("チケット") == 0) {
                            String amount = reward.substring(4);
                            imageHTML = "<img src=\"icon_ticket\">";
                            message = "ガチャチケット x " + amount;
                            rewardImage.setImageResource(R.mipmap.icon_ticket);
                        } else if (reward.indexOf("虫眼鏡") == 0) {
                            String amount = reward.substring(3);
                            imageHTML = "<img src=\"icon_megane\">";
                            message = "虫眼鏡 x " + amount;
                            rewardImage.setImageResource(R.mipmap.icon_megane);
                        }

                        rewardText.setText(Html.fromHtml(
                                message,
                                new Common.ResouroceImageGetter(getContext()), null));


                        myRewardString = imageHTML + message + "<br>" + "をプレゼントボックスに送りました。";


                    } else {
                        rewardText.setText("なし");
                    }
                }
                //result:lose
                else {
                    //負け用タイトル
                    ((ImageView) view.findViewById(R.id.result_text)).setImageResource(R.mipmap.text_mistake_1);
                }


                FrameLayout hitoFrame = (FrameLayout) view.findViewById(R.id.suiri_block_hito);
                ImageView hitoTitle = (ImageView) view.findViewById(R.id.title_hito);
                ImageView hitoArrow = (ImageView) view.findViewById(R.id.suiri_arrow_hito);
                ImageView hitoKomado = (ImageView) view.findViewById(R.id.komado_hito);

                FrameLayout kotoFrame = (FrameLayout) view.findViewById(R.id.suiri_block_koto);
                ImageView kotoArrow = (ImageView) view.findViewById(R.id.suiri_arrow_koto);
                TextView kotoText = (TextView) view.findViewById(R.id.label_koto);

                FrameLayout monoFrame = (FrameLayout) view.findViewById(R.id.suiri_block_mono);
                ImageView monoKomado = (ImageView) view.findViewById(R.id.komado_mono);


                monoFrame.setVisibility(View.VISIBLE);

                //長編証言 :: 容疑者 / 動機 / 物証
                if (param.shougenChouhenFlag) {
                    hitoFrame.setVisibility(View.VISIBLE);
                    hitoArrow.setVisibility(View.VISIBLE);
                    hitoTitle.setImageResource(R.mipmap.title_suspect_blue);

                    kotoFrame.setVisibility(View.VISIBLE);
                    kotoArrow.setVisibility(View.VISIBLE);
                    kotoText.setText(douki);
                }
                //長編テスト :: 物証
                else if (param.suiriChouhenFlag) {
                    hitoFrame.setVisibility(View.GONE);
                    hitoArrow.setVisibility(View.GONE);

                    kotoFrame.setVisibility(View.GONE);
                    kotoArrow.setVisibility(View.GONE);
                }
                //長編 :: 出題者 / 物証
                else if (param.angouChouhenFlag) {
                    hitoFrame.setVisibility(View.VISIBLE);
                    hitoArrow.setVisibility(View.VISIBLE);
                    hitoTitle.setImageResource(R.mipmap.title_shutsudai);

                    kotoFrame.setVisibility(View.GONE);
                    kotoArrow.setVisibility(View.GONE);
                }
                //事件 :: 容疑者 / 物証
                else {
                    hitoFrame.setVisibility(View.VISIBLE);
                    hitoArrow.setVisibility(View.VISIBLE);
                    hitoTitle.setImageResource(R.mipmap.title_suspect_blue);

                    kotoFrame.setVisibility(View.GONE);
                    kotoArrow.setVisibility(View.GONE);
                }


                monoKomado.setImageBitmap(Common.decodedBitmap(
                        CsvManager.komadoBitmapPathFromName(busshou),
                        72, 72
                ));

                if (yougisha != null && yougisha.length() > 0) {

                    hitoKomado.setImageBitmap(Common.decodedBitmap(
                            CsvManager.komadoBitmapPathFromName(yougisha),
                            72, 72
                    ));
                }

                break;
            }
            //index++;
        }

        return view;
    }

}

package jp.co.cybird.android.conanseek.activity.kunren;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

import jp.co.cybird.android.conanseek.activity.sagashi.KakuninPopup;
import jp.co.cybird.android.conanseek.activity.sagashi.SagashiFragment;
import jp.co.cybird.android.conanseek.activity.shop.HeartPopup;
import jp.co.cybird.android.conanseek.common.BaseActivity;
import jp.co.cybird.android.conanseek.common.BaseButton;
import jp.co.cybird.android.conanseek.common.BaseFragment;
import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.common.MessagePopup;
import jp.co.cybird.android.conanseek.manager.APIDialogFragment;
import jp.co.cybird.android.conanseek.manager.APIRequest;
import jp.co.cybird.android.conanseek.manager.CacheManager;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.CsvManager;
import jp.co.cybird.android.conanseek.manager.SaveManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.manager.UserInfoManager;
import jp.co.cybird.android.conanseek.param.APIResponseParam;
import jp.co.cybird.android.conanseek.param.KunrenParam;
import jp.co.cybird.android.conanseek.param.NanidoParam;
import jp.souling.android.conanseek01.R;
import jp.souling.android.conanseek01.Settings;

/**
 * 訓練レベル選択
 */
public class KunrenLevelPopup extends BasePopup {

    private int areaID;
    private ArrayList<Integer> levelList;
    private String areaName;

    public static KunrenLevelPopup newInstance(int areaID, String areaName, ArrayList<Integer> levelList) {

        Bundle args = new Bundle();
        args.putInt("areaID", areaID);
        args.putString("areaName", areaName);
        args.putIntegerArrayList("levelList", levelList);

        KunrenLevelPopup fragment = new KunrenLevelPopup();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.popup_kunren_level, container, false);

        Bundle args = getArguments();
        if (args != null) {
            this.areaID = args.getInt("areaID");
            this.areaName = args.getString("areaName");
            this.levelList = args.getIntegerArrayList("levelList");
        }

        //このポップアップを再表示する為の基本情報をキャッシュに保存
        if (this.areaName != null) {
            CacheManager.instance().kunrenLevelAreaID = areaID;
            CacheManager.instance().kunrenLevelAreaName = areaName;
        }

        ArrayList<NanidoParam> nanidoParamArrayList = CsvManager.nanidoList();

        //close
        view.findViewById(R.id.dialog_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeManager.play(SeManager.SeName.PUSH_BACK);

                //このポップアップを再表示する為のキャッシュをクリア
                CacheManager.instance().kunrenLevelAreaID = 0;
                CacheManager.instance().kunrenLevelAreaName = null;
                CacheManager.instance().kunrenKakuninInLevel = 0;
                CacheManager.instance().kunrenKakuninTitle = null;

                removeMe();
            }
        });

        //background
        String largeImagePath = CsvManager.areaImageFileFromAreaID(areaID, false);
        String smallImagePath = CsvManager.areaImageFileFromAreaID(areaID, false);

        ImageView dialogBgImage = (ImageView) view.findViewById(R.id.image_dialog_bg);
        dialogBgImage.setImageBitmap(Common.decodedAssetBitmap(largeImagePath, 540, 310, 0.1f));

        for (int i = 0; i < 3; i++) {

            NanidoParam nanidoParam = null;

            final int level = levelList.get(i);

            for (NanidoParam param : nanidoParamArrayList) {
                if (param.getArea() == areaID && param.getLevel() == level) {
                    nanidoParam = param;
                    break;
                }
            }

            int identifier = Common.myAppContext.getResources().getIdentifier("level_cell_" + (i + 1), "id", getActivity().getPackageName());

            //name
            ((TextView) view.findViewById(identifier).findViewById(R.id.cell_area)).setText(areaName + "Lv." + (i + 1));

            //clear
            int clearCount = UserInfoManager.kunrenClearCount(nanidoParam.getArea(), nanidoParam.getLevel());
            ((TextView) view.findViewById(identifier).findViewById(R.id.cell_clear_count)).setText("" + clearCount);

            //heart
            //1固定

            //time
            String timeString = Common.secondsToMinutes(nanidoParam.getTime());
            ((TextView) view.findViewById(identifier).findViewById(R.id.cell_time)).setText(timeString);

            //thumbnail
            ImageView thumbnailImage = (ImageView) view.findViewById(identifier).findViewById(R.id.cell_thumbnail);
            thumbnailImage.setImageBitmap(Common.decodedAssetBitmap(smallImagePath, 64, 36, 0.7f));

            //card
            ((TextView) view.findViewById(identifier).findViewById(R.id.cell_card_count)).setText("" + nanidoParam.getCard());

            //target
            ((TextView) view.findViewById(identifier).findViewById(R.id.cell_target_count)).setText("" + nanidoParam.getMono());

            //direction
            if (nanidoParam.getRotate() > 0) {
                ImageView imageView = (ImageView) view.findViewById(identifier).findViewById(R.id.cell_rule_direction);
                imageView.setImageResource(R.mipmap.icon_direction_stageselection);
            }

            //color
            if (nanidoParam.getColor() > 0) {
                ImageView imageView = (ImageView) view.findViewById(identifier).findViewById(R.id.cell_rule_color);
                imageView.setImageResource(R.mipmap.icon_monochrome_stageselection);
            }

            //order
            if (nanidoParam.getJun()) {
                ImageView imageView = (ImageView) view.findViewById(identifier).findViewById(R.id.cell_rule_order);
                imageView.setImageResource(R.mipmap.icon_turn_stageselection);
            }

            //reward
            String rewardString = "N  100%\n\n\n";
            if (i == 1) {
                rewardString = "N   80%\nHN  20%\n\n";
            } else if (i == 2) {
                rewardString = "N   75%\nHN  20%\nR    5%\n";
            }
            ((TextView) view.findViewById(identifier).findViewById(R.id.cell_reward)).setText(rewardString);

            //button/open
            BaseButton button = (BaseButton) view.findViewById(identifier).findViewById(R.id.cell_bg_button);
            FrameLayout cover = (FrameLayout) view.findViewById(identifier).findViewById(R.id.cell_cover);

            //開放条件：nullなら開放済み
            String joukenString = CsvManager.kunrenKaihouJouken(areaID, level, areaName);

            if (joukenString == null || (Settings.isDebug && SaveManager.boolValue(SaveManager.KEY.DEBUG_KUNREN_ALL_SHOW__boolean, false))) {
                button.setEnabled(true);
                cover.setVisibility(View.INVISIBLE);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SeManager.play(SeManager.SeName.PUSH_BUTTON);
                        pushKakuninPopup(areaID, level, true);
                    }
                });

            } else {

                button.setEnabled(false);
                cover.setVisibility(View.VISIBLE);

                //条件
                ((TextView) view.findViewById(identifier).findViewById(R.id.kaihou_label)).setText("開放条件\n" + joukenString);

            }
        }


        return view;
    }

    protected void pushKakuninPopup (int areaID, int level, boolean animate) {

        NanidoParam nanidoParam = null;
        ArrayList<NanidoParam> nanidoParamArrayList = CsvManager.nanidoList();

        for (NanidoParam param : nanidoParamArrayList) {
            if (param.getArea() == areaID && param.getLevel() == level) {
                nanidoParam = param;
                break;
            }
        }

        final NanidoParam finalNanidoParam = nanidoParam;

        KakuninPopup kakuninPopup = KakuninPopup.newinstance(nanidoParam.getArea(), nanidoParam.getLevel(), null, true, null);

        if (!animate) {
            kakuninPopup.noShowAnimation = true;
            kakuninPopup.showSound = null;
        }
        kakuninPopup.setPopupButtonListener(new PopupButtonListener() {
            @Override
            public void pushedPositiveClick(BasePopup popup) {

                //訓練購入可能チェック
                int heartCount = UserInfoManager.heartCount();

                //ハート足りない
                if (heartCount < 1) {

                    MessagePopup messagePopup = MessagePopup.newInstance(
                            "<img src=\"icon_heart\">ハートが足りません。<br>" + "<img src=\"icon_heart\">ハートを購入しますか？",
                            "やめる", "購入"
                    );
                    messagePopup.setPopupButtonListener(new PopupButtonListener() {
                        @Override
                        public void pushedPositiveClick(BasePopup childPopup) {
                            //ハート購入
                            showPopupFromPopup(HeartPopup.newInstance());
                        }

                        @Override
                        public void pushedNegativeClick(BasePopup childPopup) {

                        }
                    });
                    showPopupFromPopup(messagePopup);
                }
                //ハート足りた
                else {

                    //消費確認
                    MessagePopup messagePopup = MessagePopup.newInstance(
                            "ハートを1つ使用します。<br>"
                                    + "<img src=\"icon_heart\">" + heartCount + " → <img src=\"icon_heart\">" + (heartCount - 1) + "<br>"
                                    + "よろしいですか？",
                            "いいえ", "はい"
                    );
                    messagePopup.setPopupButtonListener(new PopupButtonListener() {
                        @Override
                        public void pushedPositiveClick(BasePopup childPopup) {

                            //訓練購入
                            //API：コンテンツトレード
                            APIRequest tradeRequest = new APIRequest();
                            tradeRequest.name = APIDialogFragment.APIName.CONTENT_TRADE;
                            tradeRequest.params.put("proc_id", getString(R.string.api_proc_trade_kunren));

                            APIDialogFragment tradeAPI = APIDialogFragment.newInstance(tradeRequest);
                            tradeAPI.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
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

                                    APIDialogFragment transactionAPI = APIDialogFragment.newInstance(transactionRequest);
                                    transactionAPI.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {

                                        @Override
                                        public void getAPIResult(APIRequest request, Object object) {

                                            //ユーザーインフォ更新
                                            APIRequest userInfoRequest = new APIRequest();
                                            userInfoRequest.name = APIDialogFragment.APIName.USER_INFO;

                                            APIDialogFragment userInfoApi = APIDialogFragment.newInstance(userInfoRequest);
                                            userInfoApi.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
                                                @Override
                                                public void getAPIResult(APIRequest request, Object object) {

                                                    //ヘッダー更新
                                                    ((BaseFragment) getParentFragment()).updateMyHeaderStatus();

                                                    //このポップアップを再表示する為のキャッシュをクリア
                                                    CacheManager.instance().kunrenLevelAreaID = 0;
                                                    CacheManager.instance().kunrenLevelAreaName = null;
                                                    CacheManager.instance().kunrenKakuninInLevel = 0;
                                                    CacheManager.instance().kunrenKakuninTitle = null;

                                                    finalNanidoParam.isKunren = true;

                                                    ((BaseActivity) getActivity())
                                                            .replaceViewController(SagashiFragment.newInstance(finalNanidoParam, null));

                                                }
                                            });
                                            fireApiFromPopup(userInfoApi);

                                        }
                                    });
                                    fireApiFromPopup(transactionAPI);

                                }
                            });
                            fireApiFromPopup(tradeAPI);
                        }

                        @Override
                        public void pushedNegativeClick(BasePopup childPopup) {

                        }
                    });
                    showPopupFromPopup(messagePopup);
                }
            }

            @Override
            public void pushedNegativeClick(BasePopup popup) {

            }
        });
        showPopupFromPopup(kakuninPopup);
    }
}

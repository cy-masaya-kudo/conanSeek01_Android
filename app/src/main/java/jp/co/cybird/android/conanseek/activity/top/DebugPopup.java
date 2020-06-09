package jp.co.cybird.android.conanseek.activity.top;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import jp.co.cybird.android.conanseek.common.BaseActivity;
import jp.co.cybird.android.conanseek.common.BaseFragment;
import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.manager.APIDialogFragment;
import jp.co.cybird.android.conanseek.manager.APIRequest;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.SaveManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.souling.android.conanseek01.R;
import jp.souling.android.conanseek01.SplashFragment;

/**
 * デバッグ
 */
public class DebugPopup extends BasePopup implements View.OnClickListener {

    private ListView listView;
    private String[] rows;


    public static DebugPopup newInstance() {
        
        Bundle args = new Bundle();
        
        DebugPopup fragment = new DebugPopup();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.popup_debug, container, false);

        view.findViewById(R.id.dialog_close).setOnClickListener(this);

        rows = Common.myAppContext.getResources().getStringArray(R.array.debug_labels);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_list_item_multiple_choice,
                rows
        );

        listView = (ListView) view.findViewById(R.id.listview);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                updateCheckbox(position);

                String label = rows[position];

                if (label.indexOf("強制スプラッシュ戻り") == 0) {

                    ((BaseActivity) getActivity()).replaceViewController(new SplashFragment());

                } else if (label.equals("聞き込み履歴全削除")) {

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                    alertDialogBuilder.setTitle(label);
                    alertDialogBuilder.setPositiveButton("削除",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SaveManager.updateStringValue(SaveManager.KEY.SHOUGEN__string, "");
                                    SeManager.play(SeManager.SeName.SHOP_MEGANE);
                                }
                            });
                    alertDialogBuilder.setNegativeButton("キャンセル", null);
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                } else if (label.equals("本日TOP初回表示履歴削除")) {

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                    alertDialogBuilder.setTitle(label);
                    alertDialogBuilder.setPositiveButton("削除",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SaveManager.updateStringValue(SaveManager.KEY.TOP_SHOW_DATE__string, "");
                                    SeManager.play(SeManager.SeName.GACHA_KICK);
                                }
                            });
                    alertDialogBuilder.setNegativeButton("キャンセル", null);
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                } else if (label.indexOf("通貨操作") == 0) {

                    int tsuukaType = 0;
                    if (label.indexOf("チケット") != -1) tsuukaType = 1;
                    if (label.indexOf("ハート") != -1) tsuukaType = 2;
                    if (label.indexOf("メガネ") != -1) tsuukaType = 3;
                    final int finalTsuukaType = tsuukaType;

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                    alertDialogBuilder.setTitle(label);
                    alertDialogBuilder.setPositiveButton("マックス",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    APIRequest request = new APIRequest();
                                    request.name = APIDialogFragment.APIName.POINT_FUYO;
                                    switch (finalTsuukaType) {
                                        case 1:
                                            request.params.put("proc_id", getString(R.string.api_proc_debug_ticketmax));
                                            break;
                                        case 2:
                                            request.params.put("proc_id", getString(R.string.api_proc_debug_heartmax));
                                            break;
                                        case 3:
                                            request.params.put("proc_id", getString(R.string.api_proc_debug_meganemax));
                                            break;
                                        default:
                                            request.params.put("proc_id", getString(R.string.api_proc_debug_coinmax));
                                            break;
                                    }
                                    pushPointFuyoAPI(request);
                                }
                            });
                    alertDialogBuilder.setNeutralButton("プラス1",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    APIRequest request = new APIRequest();
                                    request.name = APIDialogFragment.APIName.POINT_FUYO;
                                    switch (finalTsuukaType) {
                                        case 1:
                                            request.params.put("proc_id", getString(R.string.api_proc_debug_ticket1));
                                            break;
                                        case 2:
                                            request.params.put("proc_id", getString(R.string.api_proc_debug_heart1));
                                            break;
                                        case 3:
                                            request.params.put("proc_id", getString(R.string.api_proc_debug_megane1));
                                            break;
                                        default:
                                            request.params.put("proc_id", getString(R.string.api_proc_debug_coin1));
                                            break;
                                    }
                                    pushPointFuyoAPI(request);
                                }
                            });
                    alertDialogBuilder.setNegativeButton("ゼロ",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    APIRequest request = new APIRequest();
                                    request.name = APIDialogFragment.APIName.POINT_FUYO;
                                    switch (finalTsuukaType) {
                                        case 1:
                                            request.params.put("proc_id", getString(R.string.api_proc_debug_ticket0));
                                            break;
                                        case 2:
                                            request.params.put("proc_id", getString(R.string.api_proc_debug_heart0));
                                            break;
                                        case 3:
                                            request.params.put("proc_id", getString(R.string.api_proc_debug_megane0));
                                            break;
                                        default:
                                            request.params.put("proc_id", getString(R.string.api_proc_debug_coin0));
                                            break;
                                    }

                                    pushPointFuyoAPI(request);
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }
        });


        updateCheckbox(-1);

        return view;
    }

    private void pushPointFuyoAPI(APIRequest request) {

        APIDialogFragment fuyoAPI = APIDialogFragment.newInstance(request);
        fuyoAPI.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
            @Override
            public void getAPIResult(APIRequest request, Object object) {

                APIRequest uiRequest = new APIRequest();
                uiRequest.name = APIDialogFragment.APIName.USER_INFO;

                APIDialogFragment uiAPI = APIDialogFragment.newInstance(uiRequest);
                uiAPI.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
                    @Override
                    public void getAPIResult(APIRequest request, Object object) {

                        ((BaseFragment) getParentFragment()).updateMyHeaderStatus();
                    }
                });
                fireApiFromPopup(uiAPI);
            }
        });
        fireApiFromPopup(fuyoAPI);
    }


    private void updateCheckbox(int position) {

        if (position < 0) {

            for (Integer i = 0; i < rows.length; i++) {
                updateCheckboxValue(i, -1);
            }

        } else {

            SparseBooleanArray checked = listView.getCheckedItemPositions();
            updateCheckboxValue(position, checked.get(position) ? 1 : 0);

        }
    }

    private void updateCheckboxValue(Integer position, int value) {

        String label = rows[position];
        boolean flag = (value == 1);

        if (label.equals("聞き込み無条件開放")) {

            if (value < 0) {
                flag = SaveManager.boolValue(SaveManager.KEY.DEBUG_KIKIKOMI_ALWAYS__boolean, false);
            } else {
                SaveManager.updateBoolValue(SaveManager.KEY.DEBUG_KIKIKOMI_ALWAYS__boolean, flag);
            }

        } else if (label.indexOf("チュートリアル") != -1) {

            int index = label.indexOf("：") + 1;
            String slug = label.substring(index);

            if (value < 0) {

                ArrayList<String> strings = SaveManager.stringArray(SaveManager.KEY.TUTORIAL__stringList);
                flag = strings.contains(slug);

            } else {

                SaveManager.updateStringArray(SaveManager.KEY.TUTORIAL__stringList, slug, flag);
            }
        } else {
            SaveManager.KEY key = SaveManager.KEY.DEBUG_CARD_ALL_HAVE__boolean;

            if (label.equals("図鑑全表示")) {
                key = SaveManager.KEY.DEBUG_CARD_ALL_HAVE__boolean;
            } else if (label.equals("事件全開放")) {
                key = SaveManager.KEY.DEBUG_JIKEN_ALL_SHOW__boolean;
            } else if (label.equals("訓練全開放")) {
                key = SaveManager.KEY.DEBUG_KUNREN_ALL_SHOW__boolean;
            } else if (label.indexOf("初回会話") != -1) {
                key = SaveManager.KEY.FIRST_KAIWA__boolena;
            } else if (label.equals("再ダウンロード発生")) {
                key = SaveManager.KEY.DEBUG_REDOWNLOAD__boolean;
            }

            if (value < 0) {
                flag = SaveManager.boolValue(key, false);
            } else {
                SaveManager.updateBoolValue(key, flag);
            }
        }

        listView.setItemChecked(position, flag);


    }

    @Override
    public void onClick(View v) {
        removeMe();
    }
}

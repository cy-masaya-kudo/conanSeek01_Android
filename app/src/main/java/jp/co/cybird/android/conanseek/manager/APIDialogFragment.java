package jp.co.cybird.android.conanseek.manager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gency.aid.GencyAID;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import jp.co.cybird.android.conanseek.common.BaseActivity;
import jp.co.cybird.android.conanseek.common.BaseDialogFragment;
import jp.co.cybird.android.conanseek.common.BaseFragment;
import jp.co.cybird.android.conanseek.common.MessagePopup;
import jp.co.cybird.android.conanseek.param.APIResponseParam;
import jp.souling.android.conanseek01.R;
import jp.souling.android.conanseek01.Settings;

/**
 * APIフラグメント
 */
public class APIDialogFragment extends BaseDialogFragment {

    //-- APIリスナー

    private APIDialogListener apiListener = null;


    public interface APIDialogListener {
        void getAPIResult(APIRequest request, Object object);
    }

    public void setAPIDialogListener(APIDialogListener l) {
        apiListener = l;
    }

    //API名
    public enum APIName {
        VERSION_CHECK,
        ADD_TOKUTEN,
        USER_CREATE,
        USER_INFO,
        GACHA_LIST,
        GACHA_FIRE,
        PRESENT_LIST,
        PRESENT_FIRE,
        POINT_FUYO,
        COIN_CHARGE,
        POINT_TRADE,
        FILE_DOWNLOAD,
        CONTENT_TRADE,
        TRANSACTION_COMMIT,
        KUNREN_CLEAR,
        JIKEN_CLEAR,
    }

    //リクエストパラメーター
    private APIRequest myRequest;

    //ダウンロードフラグメント
    private APIDownloadingFragment downloadingFragment;

    //インスタンス
    public static APIDialogFragment newInstance(APIRequest request) {
        APIDialogFragment fragment = new APIDialogFragment();
        fragment.myRequest = request;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;

        view = inflater.inflate(R.layout.fragment_api, container);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //FILEダウンロードはフラグメント立ち上げ
        if (myRequest.name == APIName.FILE_DOWNLOAD) {
            downloadingFragment = APIDownloadingFragment.newInstance();

            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            ft.add(downloadingFragment, null);
            ft.commitAllowingStateLoss();

            //downloadingFragment.show(getChildFragmentManager(), null);

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!myRequest.connecting) {

            //必要なパラメーターが不足しているとnullが返る
            String urlString = myRequest.getRequestURL();

            if (urlString == null) {
                pushErrorDialog("リクエストパラメーター不正");
                dismiss();
            } else {
                makeConnect(urlString);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Common.apiLog("API Dialog onDestroy:" + myRequest.name);
    }

    /**
     * エラーダイアログ表示
     */
    private void pushErrorDialog(String errorMessage) {
        if (myRequest != null)
            myRequest.connecting = false;
        if (getParentFragment() != null) {
            ((BaseFragment) getParentFragment()).showPopup(MessagePopup.newInstance(errorMessage));
        } else {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setTitle(errorMessage);
            alertDialogBuilder.setNegativeButton("OK", null);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }


    /**
     * 通信結果
     */
    private void getConnectResult(String resultString) {

        Gson gson = new Gson();

        //バージョンチェック
        if (myRequest.name == APIName.VERSION_CHECK) {
            /*
             * バージョンチェックはレスポンスのフォーマットが違う
             */
            APIResponseParam.Version param = gson.fromJson(resultString, APIResponseParam.Version.class);
            if (apiListener != null)
                apiListener.getAPIResult(myRequest, param);

        }
        //ファイルダウンロード
        else if (myRequest.name == APIName.FILE_DOWNLOAD) {
            /*
             * ファイルダウンロードはZIPファイルを直接もらう
             */

            //解凍
            unzip(resultString);

            //if (apiListener != null) apiListener.getAPIResult(myRequest, null);
            return;

        }
        //通常API
        else {

            Common.apiLog("Raw Result:" + resultString);
            APIResponseParam param = gson.fromJson(resultString, APIResponseParam.class);

            //エラー発生
            if (param.state.code > 0) {
                //エラー表示
                pushErrorDialog(param.state.message);

            }
            //エラーなし
            else {

                //ユーザー作成
                if (myRequest.name == APIName.USER_CREATE) {
                    //もらったapUUIDを記録
                    SaveManager.updateStringValue(SaveManager.KEY.AP_UUID__string, param.item.user.ap_uuid);
                }

                //userInfoが付随しているAPI
                else if (myRequest.name == APIName.USER_INFO
                        || myRequest.name == APIName.GACHA_FIRE
                        || myRequest.name == APIName.PRESENT_FIRE
                        || myRequest.name == APIName.JIKEN_CLEAR
                        || myRequest.name == APIName.COIN_CHARGE
                        || myRequest.name == APIName.KUNREN_CLEAR
                        || myRequest.name == APIName.ADD_TOKUTEN
                        || myRequest.name == APIName.TRANSACTION_COMMIT) {

                    //userInfo更新
                    UserInfoManager.updateUserInfoString(gson.toJson(param));


                }

                if (apiListener != null)
                    apiListener.getAPIResult(myRequest, param);
            }

        }
        dismiss();
    }


    /**
     * 通信開始
     */
    public void makeConnect(final String urlString) {

        ConnectivityManager connectivityManager;
        connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        final Handler handler = new Handler();

        if (networkInfo != null && networkInfo.isConnected()) {
            Common.apiLog("Can Connect");
        } else {
            Common.apiLog("Cannot Connect");
            pushErrorDialog("サーバーに接続できません。\n" +
                    "通信環境の良い所で、再度お試しください。");
            dismiss();
            return;
        }

        if (myRequest.connecting) {
            Common.apiLog("Already Connectiong");
            pushErrorDialog("既に接続中です。\n" +
                    "少し時間をおいて、再度お試しください。");
            dismiss();
            return;
        }
        myRequest.connecting = true;

        new Thread(new Runnable() {

            @Override
            public void run() {

                HttpURLConnection connection = null;
                InputStream inputStream = null;

                String postDataString;

                /**
                 * リクエストデータは
                 * $_POST['data'] = {AES暗号Base64化JSON}形式になるように作る
                 */
                Map<String, Object> requestMap = new HashMap<>();
                Gson gson = new Gson();

                requestMap.put("app_id", Common.myAppContext.getPackageName());
                requestMap.put("ver", Common.getVersionName());
                requestMap.put("os", Settings.osName);
                requestMap.put("os_ver", Build.VERSION.RELEASE);
                requestMap.put("device", Build.MODEL);
                requestMap.put("market", Settings.storeName);
                requestMap.put("ap_uuid", SaveManager.stringValue(SaveManager.KEY.AP_UUID__string, ""));
//                String cyUUID = SaveManager.cyUUID();
//                if (cyUUID.length() <= 0) {
//                    //cyUUID = UUID.randomUUID().toString();
//                    //SaveManager.updateStringValue(SaveManager.KEY.CY_UUID__string, cyUUID);
//                }
                String cyUUID = null;
                try {
                    cyUUID = GencyAID.getGencyAID(getContext());
                }catch (Exception exception) {
                    cyUUID = "";
                }
                requestMap.put("cy_uuid", cyUUID);

                //トランザクションの中身が文字列にならないようにjson化
                if (myRequest.transactionString != null) {

                    Map<String, Integer> json = new Gson().fromJson(myRequest.transactionString, new TypeToken<Map<String, String>>() {
                    }.getType());

                    myRequest.params.put("transaction", json);
                }
                //レシピの中身が文字列にならないようにjson化
                if (myRequest.receiptMap != null) {
                    myRequest.params.put("receipt", myRequest.receiptMap);
                }
                //リクエストにパラメーター追加
                if (myRequest.params.size() > 0) {
                    requestMap.put("request", myRequest.params);
                }

                Common.apiLog("requestMap:" + requestMap);

                //リクエスト暗号化
                String dataString = gson.toJson(requestMap);
                postDataString = "data=" + AesCrypt.encrypt(dataString, Settings.AES_KEY, Settings.AES_IV);

                try {

                    connection = (HttpURLConnection) new URL(urlString).openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    //connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                    Common.apiLog("connect to:" + urlString);

                    //connection.setFixedLengthStreamingMode(postDataString.getBytes().length);


                    if (myRequest.name == APIName.FILE_DOWNLOAD) {

                        /**
                         * ZIPはテンポラリファイルにダウンロード
                         */
                        // テンポラリファイルの設定
                        File outputFile = File.createTempFile("sample", ".zip");
                        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

                        PrintStream printStream = new PrintStream(connection.getOutputStream());
                        printStream.print(postDataString);
                        printStream.close();

                        // ダウンロード開始
                        InputStream is = connection.getInputStream();
                        byte[] buffer = new byte[1024];
                        int len;

                        int fileLength = connection.getContentLength();
                        int totalLength = 0;
                        int percent = 0;

                        while ((len = is.read(buffer)) != -1) {
                            totalLength += len;
                            fileOutputStream.write(buffer, 0, len);

                            Common.apiLog("DOWNLOADING : " + totalLength + "/" + fileLength);

                            int per = Math.round((float) totalLength / (float) fileLength * 100.0F);
                            if (per != percent) {
                                percent = per;
                                final int finalPercent = percent;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        downloadingFragment.updateMeter(true, finalPercent);
                                    }
                                });
                            }
                        }

                        fileOutputStream.close();
                        is.close();

                        //ダウンロードしたZIPのパス
                        final String pathText = outputFile.getPath();
                        Common.apiLog("zipPath:" + pathText);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                getConnectResult(pathText);
                            }
                        });

                    } else {

                        /**
                         * 通常は文字列
                         */

                        PrintStream printStream = new PrintStream(connection.getOutputStream());
                        printStream.print(postDataString);
                        printStream.close();

                        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        br.close();

                        //JSONテキスト
                        final String text = sb.toString();
                        Common.apiLog("resultRaw:" + text);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                getConnectResult(text);
                            }
                        });
                    }


                } catch (Exception e) {

                    final String str = e.toString();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            pushErrorDialog(str);
                        }
                    });
                    dismiss();

                } finally {
                    try {
                        if (connection != null)
                            connection.disconnect();
                        if (inputStream != null)
                            inputStream.close();
                    } catch (Exception e) {
                        pushErrorDialog(e.toString());
                        dismiss();
                    }
                }
            }
        }).start();
    }


    /**
     * ZIP解凍
     */
    private void unzip(final String filename) {

        final Handler handler = new Handler();

        Common.logD("start unzip");

        new Thread(new Runnable() {

            @Override
            public void run() {

                ZipInputStream in = null;
                //BufferedOutputStream out;

                ZipEntry zipEntry;

                List<String> list = new ArrayList<>();

                try {

                    FileInputStream fi = new FileInputStream(filename);
                    ZipFile zf = new ZipFile(filename);
                    in = new ZipInputStream(fi);

                    int totalFiles = zf.size();
                    int percent = 0;

                    String parentPath = getContext().getFilesDir() + "";
                    //File outDir = new File(parentPath);

                    // ZIPファイルに含まれるエントリに対して順にアクセス
                    while ((zipEntry = in.getNextEntry()) != null) {

                        if (!zipEntry.isDirectory()) {

                            String relativePath = getContext().getFilesDir() + "/" + zipEntry.getName().replace("/", "__");
                            File outFile = new File(relativePath);
                            //File parentFile = outFile.getParentFile();
                            //parentFile.mkdirs();


                            FileOutputStream fileOut = new FileOutputStream(outFile);
                            byte[] buf = new byte[256];
                            int size = 0;
                            while ((size = in.read(buf)) > 0) {
                                fileOut.write(buf, 0, size);
                            }
                            fileOut.close();
                            fileOut = null;

                            list.add(outFile.getPath());

                            int per = Math.round((float) list.size() / (float) totalFiles * 100.0F);
                            if (per != percent) {
                                percent = per;
                                final int finalPercent = percent;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        downloadingFragment.updateMeter(false, finalPercent);
                                    }
                                });
                                //updateMeter(false, finalPercent);
                            }
                        }
                    }

                    zf.close();
                    fi.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    //e.printStackTrace();
                    Common.logD("IOException:"+e.toString());
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            downloadingFragment.dismissAllowingStateLoss();
                            if (apiListener != null) apiListener.getAPIResult(myRequest, null);
                            APIDialogFragment.this.dismissAllowingStateLoss();
                        }
                    });
                }
            }
        }).start();
    }

}

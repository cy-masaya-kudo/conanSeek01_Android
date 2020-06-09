package jp.co.cybird.android.conanseek.activity.shop;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;
import com.tapjoy.Tapjoy;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import jp.co.cybird.android.conanseek.common.BaseActivity;
import jp.co.cybird.android.conanseek.common.BaseFragment;
import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.common.MessagePopup;
import jp.co.cybird.android.conanseek.manager.APIDialogFragment;
import jp.co.cybird.android.conanseek.manager.APIRequest;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.manager.UserInfoManager;
import jp.co.cybird.android.minors.MinorsDialogListener.OnAgreeListener;
import jp.co.cybird.android.minors.MinorsDialogListener.OnCancelListener;
import jp.co.cybird.android.minors.MinorsDialogListener.OnDeclineListener;
import jp.co.cybird.android.minors.MinorsDialogListener.OnDismissListener;
import jp.co.cybird.android.support.v4.minors.MinorsDialogManager;
import jp.souling.android.conanseek01.R;
import jp.souling.android.conanseek01.Settings;
import jp.souling.android.conanseek01.util.IabBroadcastReceiver;
import jp.souling.android.conanseek01.util.IabHelper;
import jp.souling.android.conanseek01.util.IabResult;
import jp.souling.android.conanseek01.util.Inventory;
import jp.souling.android.conanseek01.util.Purchase;

/**
 * コイン購入
 */
public class CoinPopup extends BasePopup implements View.OnClickListener, IabBroadcastReceiver.IabBroadcastListener, OnAgreeListener, OnCancelListener, OnDeclineListener, OnDismissListener {

    // The helper object
    private IabHelper mHelper;

    // Provides purchase notification while this app is running
    private IabBroadcastReceiver mBroadcastReceiver;

    //値段チェック済み
    private boolean nedanChecked;

    //sku
    final String SKU_PREFIX = "jp.co.cybird.android.app.conanseek01.";
    //final String SKU_COIN1 = "android.test.purchased";
    final String SKU_COIN1 = SKU_PREFIX + "coin1";
    final String SKU_COIN5 = SKU_PREFIX + "coin5";
    final String SKU_COIN12 = SKU_PREFIX + "coin12";
    final String SKU_COIN24 = SKU_PREFIX + "coin24";
    final String SKU_COIN50 = SKU_PREFIX + "coin50";
    final String SKU_COIN100 = SKU_PREFIX + "coin100";

    private MinorsDialogManager mMinorsDialogManager;

    //123gfagfjfkfkjfjgfkfm;sdfmdpsojfopsadjfposdaj

    final String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmc4vqd7zgzl/+RZ1Wm2pb/d0yj+fddLBc7RZ0dzIqp9WddTSrtU+lejzxPLEBCACit3ZbNyahY+rXED/r4CdmUDzn/nf1Uva3bYeiSRmV6R14E0HSs1yQ+pOz8XJU1/8tmM6nY2QIQw2W6wi1if5VeKIz+UulwFUfKOFWbOYacdX+8SliHOAXxL67piDrbwXR9402MIU8yrOzzL/isREYstHQHCgHBl4a/tGaO5dNC+/jeDtLHkY9p3c/GGfcmS/syGz0HZQATe2p8h+LqALQ3hF/oxHIUFXZS8F6seadMQZ36GrLAhA5bzFxMJIkN/GmFDx7Df8VDdv/CkJ9amOawIDAQAB";

    //タップ不可
    boolean notapflag = false;


    public static CoinPopup newInstance() {

        Bundle args = new Bundle();

        CoinPopup fragment = new CoinPopup();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.popup_coin, container, false);

        view.findViewById(R.id.dialog_close).setOnClickListener(this);
        view.findViewById(R.id.btn_dialog_coin_1).setOnClickListener(this);
        view.findViewById(R.id.btn_dialog_coin_5).setOnClickListener(this);
        view.findViewById(R.id.btn_dialog_coin_12).setOnClickListener(this);
        view.findViewById(R.id.btn_dialog_coin_24).setOnClickListener(this);
        view.findViewById(R.id.btn_dialog_coin_50).setOnClickListener(this);
        view.findViewById(R.id.btn_dialog_coin_100).setOnClickListener(this);


        //souling


        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        getActivity().bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        // Create the helper, passing it our context and the public key to verify signatures with
        mHelper = new IabHelper(getContext(), base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(true);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {

                if (!result.isSuccess()) {
                    return;
                }

                if (mHelper == null) return;


                mBroadcastReceiver = new IabBroadcastReceiver(CoinPopup.this);

                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);

                getActivity().registerReceiver(mBroadcastReceiver, broadcastFilter);

                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });

        return view;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mService != null) {
            getActivity().unbindService(mServiceConn);
        }

    }

    IInAppBillingService mService;

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            Common.logD("onServiceConnected");
            mService = IInAppBillingService.Stub.asInterface(service);
            loadAddonDetails();
        }
    };


    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {


        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Common.logD("Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                Common.logD("Error purchasing. result.isFailure.");
                //setWaitScreen(false);
                ((BaseActivity)getActivity()).removeNotap();
                showPopupFromPopup(MessagePopup.newInstance("<img src=\"icon_coin\">コインの購入に失敗しました"), true);
                notapflag = false;
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                Common.logD("Error purchasing. Authenticity verification failed.");
                ((BaseActivity)getActivity()).removeNotap();
                showPopupFromPopup(MessagePopup.newInstance("<img src=\"icon_coin\">コインの購入に失敗しました"), true);
                //setWaitScreen(false);
                notapflag = false;
                return;
            }

            Common.logD("Purchase successful.");

            //APIでレシート検証

            //消費
            shouhiPurchase(purchase);
        }
    };

    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Common.logD("Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            // We know this is the "gas" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                Common.logD("Consumption successful. Provisioning.");
                //saveData();
                //alert("You filled 1/4 tank. Your tank is now " + String.valueOf(mTank) + "/4 full!");


                SeManager.play(SeManager.SeName.SHOP_COIN);

                int orderCoinCount = 0;
                switch (purchase.getDeveloperPayload()) {
                    case SKU_COIN1:
                        orderCoinCount = 1;
                        break;
                    case SKU_COIN5:
                        orderCoinCount = 5;
                        break;
                    case SKU_COIN12:
                        orderCoinCount = 12;
                        break;
                    case SKU_COIN24:
                        orderCoinCount = 24;
                        break;
                    case SKU_COIN50:
                        orderCoinCount = 50;
                        break;
                    case SKU_COIN100:
                        orderCoinCount = 100;
                        break;
                    default:
                        break;
                }
                Common.logD("purchase" + purchase + " result" + result);


                showPopupFromPopup(MessagePopup.newInstance("<img src=\"icon_coin\">コイン x " + orderCoinCount + "個<br/>購入しました。", null, null), true);


            } else {
                Common.logD("Error while consuming: " + result);

                showPopupFromPopup(MessagePopup.newInstance("<img src=\"icon_coin\">コインの購入に失敗しました", null, null), true);
            }
            //updateUi();
            //setWaitScreen(false);
            ((BaseActivity)getActivity()).removeNotap();
            notapflag = false;
            Common.logD("End consumption flow.");
        }
    };


    boolean verifyDeveloperPayload(Purchase p) {
        //String payload = p.getDeveloperPayload();
        return true;
    }


    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Common.logD("Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                Common.logD("Failed to query inventory: " + result);
                return;
            }

            Common.logD("Query inventory was successful." + inventory);

            //未消費消化
            shouhiPurchase(inventory.getPurchase(SKU_COIN1));
            shouhiPurchase(inventory.getPurchase(SKU_COIN5));
            shouhiPurchase(inventory.getPurchase(SKU_COIN12));
            shouhiPurchase(inventory.getPurchase(SKU_COIN24));
            shouhiPurchase(inventory.getPurchase(SKU_COIN50));
            shouhiPurchase(inventory.getPurchase(SKU_COIN100));

            //updateUi();
            //setWaitScreen(false);
            ((BaseActivity)getActivity()).removeNotap();
            Common.logD("Initial inventory query finished; enabling main UI.");
        }
    };


    //消費
    private void shouhiPurchase(final Purchase purchase) {

        if (null != purchase) {

            Common.logD("shouhiPurchase:" + purchase);

            APIRequest chargeRequest = new APIRequest();
            chargeRequest.name = APIDialogFragment.APIName.COIN_CHARGE;
            chargeRequest.params.put("proc_id", purchase.getSku());
            chargeRequest.receiptMap = new HashMap<>();
            chargeRequest.receiptMap.put("packageName", purchase.getPackageName());
            chargeRequest.receiptMap.put("orderId", purchase.getOrderId());
            chargeRequest.receiptMap.put("productId", purchase.getSku());
            chargeRequest.receiptMap.put("developerPayload", purchase.getDeveloperPayload());
            chargeRequest.receiptMap.put("purchaseTime", String.valueOf(purchase.getPurchaseTime()));
            chargeRequest.receiptMap.put("purchaseState", String.valueOf(purchase.getPurchaseState()));
            chargeRequest.receiptMap.put("purchaseTime", String.valueOf(purchase.getPurchaseTime()));
            chargeRequest.receiptMap.put("purchaseToken", purchase.getToken());

            APIDialogFragment chargeAPI = APIDialogFragment.newInstance(chargeRequest);
            chargeAPI.setAPIDialogListener(new APIDialogFragment.APIDialogListener() {
                @Override
                public void getAPIResult(APIRequest request, Object object) {

                    ((BaseFragment) getParentFragment()).updateMyHeaderStatus();

                    if (mHelper != null)
                        mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                    else {
                        showPopupFromPopup(MessagePopup.newInstance("エラーが発生しました。再度処理を行ってください。", null, null));
                        notapflag = false;
                    }
                }
            });

            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            ft.add(chargeAPI, null);
            ft.commitAllowingStateLoss();

            //fireApiFromPopup(chargeAPI);
        }

        /*
        APIConnect *connect = [APIConnect new];
        connect.delegate = self;
        connect.storeTransaction = transaction;
        connect.request.apiName = APINameCoinCharge;
        [connect.request addParameterValue:base64receipt key:@"receipt"];
        [connect.request addParameterValue:[NSString stringWithUTF8String:PROCIDS_COIN_CHARGE[chagedCoinName]] key:@"proc_id"];
        [connect startConnect];
        */
    }


    @Override
    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        Common.logD("Received broadcast notification. Querying inventory.");
        if (mHelper != null)
            mHelper.queryInventoryAsync(mGotInventoryListener);
        else {
            showPopupFromPopup(MessagePopup.newInstance("エラーが発生しました。再度処理を行ってください。", null, null));
            notapflag = false;
        }
    }

    //-------------


    private void loadAddonDetails() {

        ArrayList<String> skuList = new ArrayList<>();
        skuList.add(SKU_COIN1);
        skuList.add(SKU_COIN5);
        skuList.add(SKU_COIN12);
        skuList.add(SKU_COIN24);
        skuList.add(SKU_COIN50);
        skuList.add(SKU_COIN100);
        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

        try {

            Bundle skuDetails = mService.getSkuDetails(3, getActivity().getPackageName(), "inapp", querySkus);

            int response = skuDetails.getInt("RESPONSE_CODE");


            if (response == 0) {

                ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");

                for (String thisResponse : responseList) {

                    JSONObject object = new JSONObject(thisResponse);
                    String sku = object.getString("productId");
                    String price = object.getString("price");
                    if (sku.equals(SKU_COIN1)) {
                        ((TextView) getView().findViewById(R.id.label_dialog_coin_1)).setText(price);
                    } else if (sku.equals(SKU_COIN5)) {
                        ((TextView) getView().findViewById(R.id.label_dialog_coin_5)).setText(price);
                    } else if (sku.equals(SKU_COIN12)) {
                        ((TextView) getView().findViewById(R.id.label_dialog_coin_12)).setText(price);
                    } else if (sku.equals(SKU_COIN24)) {
                        ((TextView) getView().findViewById(R.id.label_dialog_coin_24)).setText(price);
                    } else if (sku.equals(SKU_COIN50)) {
                        ((TextView) getView().findViewById(R.id.label_dialog_coin_50)).setText(price);
                    } else if (sku.equals(SKU_COIN100)) {
                        ((TextView) getView().findViewById(R.id.label_dialog_coin_100)).setText(price);
                        nedanChecked = true;
                    }
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            //e.printStackTrace();
        }
    }



    public void receiveOnActivityResult(int requestCode, int resultCode, Intent data) {

        Common.logD("onActivityResult(" + requestCode + "," + resultCode + "," + data);

        if (mHelper == null) return;


        //TapJoy
        // get reciept here.
        if (resultCode == Activity.RESULT_OK) {
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            try {
                JSONObject purchaseDataJson = new JSONObject(purchaseData);
                String productId = purchaseDataJson.getString("productId");

                // getSkuDetails
                ArrayList<String> skuList = new ArrayList<>();
                skuList.add(productId);
                Bundle querySkus = new Bundle();
                querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
                Bundle skuDetails = mService.getSkuDetails(3, Common.myAppContext.getPackageName(), "inapp", querySkus);
                ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");

                Tapjoy.trackPurchase(responseList.get(0), purchaseData, dataSignature, null);

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Common.logD("onActivityResult handled by IABUtil.");
        }
    }

    @Override
    public void onClick(View v) {

        if (notapflag) return;

        if (v.getId() == R.id.dialog_close) {

            SeManager.play(SeManager.SeName.PUSH_BACK);
            removeMe();

        } else {

            ((BaseActivity)getActivity()).addNotap();

            //年齢確認
            if (mMinorsDialogManager == null) {
                int eulaVersion = getResources().getInteger(R.integer.MinorEulaVersion);
                mMinorsDialogManager = new MinorsDialogManager(getActivity(), eulaVersion, getFragmentManager());
                mMinorsDialogManager.setEulaVer(eulaVersion);
                // 各イベントを取得できるようにリスナーを設定
                mMinorsDialogManager.setOnCancelListener(this);
                mMinorsDialogManager.setOnDeclineListener(this);
                mMinorsDialogManager.setOnAgreeListener(this);
                mMinorsDialogManager.setOnDismissListener(this);
            }

            if (!mMinorsDialogManager.isAgreement()) {
                mMinorsDialogManager.show();
                ((BaseActivity)getActivity()).removeNotap();
                return;
            }

            SeManager.play(SeManager.SeName.PUSH_BUTTON);

            if (!nedanChecked) {
                MessagePopup messagePopup = MessagePopup.newInstance("ログインしてください。");
                showPopupFromPopup(messagePopup);
                return;
            }

            notapflag = true;
            int count = 0;
            String skuString = SKU_COIN1;

            if (v.getId() == R.id.btn_dialog_coin_1) {
                count = 1;
                skuString = SKU_COIN1;
            }
            if (v.getId() == R.id.btn_dialog_coin_5) {
                count = 5;
                skuString = SKU_COIN5;
            }
            if (v.getId() == R.id.btn_dialog_coin_12) {
                count = 12;
                skuString = SKU_COIN12;
            }
            if (v.getId() == R.id.btn_dialog_coin_24) {
                count = 24;
                skuString = SKU_COIN24;
            }
            if (v.getId() == R.id.btn_dialog_coin_50) {
                count = 50;
                skuString = SKU_COIN50;
            }
            if (v.getId() == R.id.btn_dialog_coin_100) {
                count = 100;
                skuString = SKU_COIN100;
            }

            if (UserInfoManager.coinCount() + count >= 99999) {
                //コイン限界突破か
                showPopupFromPopup(MessagePopup.newInstance("これ以上<img src=\"icon_coin\">コインを持てません", null, null));
                notapflag = false;

            } else {

                //購入
                Common.logD("launchPurchaseFlow:" + skuString + " mHelper:"+mHelper);
                if (mHelper != null) {
                    mHelper.launchPurchaseFlow(getActivity(), skuString, Settings.RC_REQUEST, mPurchaseFinishedListener, skuString);
                }
                else {
                    showPopupFromPopup(MessagePopup.newInstance("エラーが発生しました。再度処理を行ってください。", null, null));
                    notapflag = false;
                }
            }

        }
    }

    @Override
    public void onAgree() {
        Common.logD("AgreementDEMO onAgree()");
    }

    @Override
    public void onCancel() {
        Common.logD("AgreementDEMO onCancel()");
    }

    @Override
    public void onDecline() {
        Common.logD("AgreementDEMO onDecline()");
    }

    @Override
    public void onDismiss() {
        Common.logD("AgreementDEMO onDismiss()");
    }
}

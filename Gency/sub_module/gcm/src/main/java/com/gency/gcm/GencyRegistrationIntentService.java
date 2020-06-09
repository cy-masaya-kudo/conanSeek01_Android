package com.gency.gcm;
/**
 *
 * Copyright 2015 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.gency.crypto.aes.GencyAES;
import com.gency.crypto.aes.GencyAESUtility;
import com.gency.commons.log.GencyDLog;
import com.gency.util.Utils;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class GencyRegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};

    private static int RetryCounterInstanceID = 5;

    public GencyRegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(GencyParameterLoader.getString("LIB_GCM_SENDERID", this),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            GencyDLog.i(TAG, "GCM Registration Token: " + token);
            // 新しくもらったregistration idをローカルに保存。来るたびに保存する
            saveRegistrationID(token);
            sendRegistrationToServer(this);

            // [START subscribe_topics]
            // Subscribe to topic channels
            // Topic Messagingを使用したい場合
            // subscribeTopics(token);
            // [END subscribe_topics]

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            sharedPreferences.edit().putBoolean(GencyQuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();
            // [END register_for_gcm]
        } catch (IOException e) {
            // After user install "GooglePlayService" for the first time, it takes quite a while for initialization
            // So it may cause TIME OUT for several times. Gency will retry for RetryCounterInstanceID times.
            if (RetryCounterInstanceID-- > 0) {
                Intent retryIntent = new Intent(getApplicationContext(), GencyRegistrationIntentService.class);
                getApplicationContext().startService(retryIntent);
                // the new instance of intentService will share the previous instance.
            }
        } catch (Exception e) {
            GencyDLog.e(TAG, "Failed to complete token refresh :"+ e.toString());
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(GencyQuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply();
        }
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     */
    private void sendRegistrationToServer(Context context) {
        // TODO: 自社プッシュ管理システムに飛ばしてください。(send any registration to your app's servers.)
        try {
            GencyGCMUtilitiesE.sendRegistrationInfo(context, GencyGCMUtilitiesE.getAllowCustomizationEveryLaunch());
        } catch (Exception e) {
            GencyDLog.e(TAG, e.toString());
        }
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        for (String topic : TOPICS) {
            GcmPubSub pubSub = GcmPubSub.getInstance(this);
//            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]

    /**
     * ローカルにRegistrationIdを保存する
     * @param regId
     */
    private void saveRegistrationID(String regId){
    	try {
    		String savedRegistrationId = GencyGCMUtilitiesE.getLocalRegistrationID(getApplicationContext());
            if ("".equals(savedRegistrationId) ==  false) {
    			savedRegistrationId = GencyAESUtility.encodeToStringByUTF8(GencyAES.decrypt(GencyAESUtility.decodeToByteByBase64(savedRegistrationId), Utils.c(GencyGCMConst.GCM_AES_KEY), Utils.c(GencyGCMConst.GCM_AES_IV)));
    		}
            if (!savedRegistrationId.equals(regId)) {
            	// 保存したregistration IdとGoogleからもらったIDが違う場合、
            	// ローカルIdを更新して、Cybirdサーバーに送信のはintiGCM()後行いますので、いま要らない
            	String encRegId = GencyAESUtility.encodeToStringByBase64(GencyAES.encrypt(GencyAESUtility.decodeToByteByUTF8(regId), Utils.c(GencyGCMConst.GCM_AES_KEY), Utils.c(GencyGCMConst.GCM_AES_IV)));
            	SharedPreferences prefs = getApplicationContext().getSharedPreferences(GencyGCMConst.PREF_FILE_NAME, Context.MODE_PRIVATE);
                Editor editor = prefs.edit();
                editor.putString(GencyGCMConst.PREF_KEY_REGISTRATION_ID, encRegId);
                editor.commit();
            }
    	} catch (InvalidKeyException e) {
			GencyDLog.e(TAG, e.toString());
		} catch (NoSuchAlgorithmException e) {
			GencyDLog.e(TAG, e.toString());
		} catch (NoSuchPaddingException e) {
			GencyDLog.e(TAG, e.toString());
		} catch (InvalidAlgorithmParameterException e) {
			GencyDLog.e(TAG, e.toString());
		} catch (IllegalBlockSizeException e) {
			GencyDLog.e(TAG, e.toString());
		} catch (BadPaddingException e) {
			GencyDLog.e(TAG, e.toString());
		}
    }
}

/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
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

package com.gency.gcm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;

import com.gency.commons.log.GencyDLog;
import com.google.android.gms.gcm.GcmListenerService;

import java.io.IOException;

public class GencyGcmListenerService extends GcmListenerService {

    private static final String TAG = "GencyGcmListenerService";
    /**
     * Called when message is received.
     *
     * @param msgId SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @SuppressWarnings("deprecation")
	// [START receive_message]
    @Override
    public void onMessageReceived(String msgId, Bundle data) {
        String message = data.getString("msg");
        GencyDLog.d(TAG, "From: " + msgId);
        GencyDLog.d(TAG, "Message: " + message);

        // [START subscribe_topics]
        // Topic Messagingを使用したい場合
//        if (msgId.startsWith("/topics/")) {
//            // message received from some topic.
//        } else {
//            // normal downstream message.
//        }
        // [END subscribe_topics]

        // 追加分 2015/08/25 by a
        // メッセージ受け取り時の処理
        SharedPreferences prefs = this.getApplicationContext().getSharedPreferences(GencyGCMConst.PREF_FILE_NAME, Context.MODE_PRIVATE);
        boolean willSendNotification = prefs.getBoolean(GencyGCMConst.PREF_KEY_WILLSENDNOTIFICATION, true);
        // プッシュ取得許諾がオンかどうか。
        if (willSendNotification == true) {
            PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
            // カスタムプッシュを起動するかどうか。
            if(data.containsKey("customDialog")){
                if (!pm.isScreenOn()) { // リッチプッシュ起動条件。端末がスリープ状態時のみ起動。
                    GencyGCMUtilitiesE.IS_CUSTOM_PUSH = true;
                    GencyGCMUtilitiesE.NOTIFICATION_PARAM = data;
                    Intent customDialogIntent = new Intent(this.getApplicationContext().getApplicationContext(), com.gency.gcm.GencyCustomDialogActivity.class);
                    customDialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    customDialogIntent.putExtra("packageName", this.getApplicationContext().getPackageName());
                    customDialogIntent.putExtra("dialogUrl", data.getString("dialogUrl"));
                    customDialogIntent.putExtra("notificationParam", data);
                    // カスタムプッシュのダイアログアクティビティ起動。
                    startActivity(customDialogIntent);
                    return;
                }
            }
            // 通常プッシュの場合、もしくはリッチプッシュ条件を満たしていない場合。
            GencyGCMUtilitiesE.addNotification(getApplicationContext(), data);
        } else {
            // user refuse notifications but still get notifications
            try {
                GencyGCMUtilitiesE.unregisterGCM(this.getApplicationContext());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
}

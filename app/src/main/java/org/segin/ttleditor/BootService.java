package org.segin.ttleditor;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

/*
 * Copyright 2014-2015 Kirn Gill II <segin2005@gmail.com>
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

public class BootService extends IntentService {
    public BootService() {
        super("BootService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "boot_service";
            NotificationChannel channel = new NotificationChannel(channelId, "Boot Service", NotificationManager.IMPORTANCE_MIN);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
            Notification notification = new Notification.Builder(this, channelId)
                    .setContentTitle("Applying TTL Settings")
                    .setContentText("Please wait...")
                    .setSmallIcon(android.R.drawable.ic_menu_rotate)
                    .build();
            startForeground(1, notification);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences myPreference = PreferenceManager.getDefaultSharedPreferences(this);
        if(myPreference.getBoolean("onboot", false)) {
            String iface = myPreference.getString("iface", "rmnet_sdio0");
            String ttl = myPreference.getString("ttl", "64");
            TTLEditor.changeTTL(this.getApplicationContext(), iface, ttl);
        }
    }
}

package org.segin.ttleditor;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Kirn on 7/19/14.
 */
public class BootService extends IntentService {
    public BootService() {
        super("BootService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences myPreference= PreferenceManager.getDefaultSharedPreferences(this);
        if(myPreference.getBoolean("onboot", false)) {
            String iface = myPreference.getString("iface", "rmnet_sdio0");
            int ttl = myPreference.getInt("ttl",64);

        }

    }
}

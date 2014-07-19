package org.segin.ttleditor;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by Kirn on 7/19/14.
 */
public class BootService extends IntentService {
    public BootService() {
        super("BootService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long endTime = System.currentTimeMillis() + 5*1000;
        while (System.currentTimeMillis() < endTime) {
            synchronized (this) {
                try {
                    wait(endTime - System.currentTimeMillis());
                } catch (Exception e) {
                }
            }
        }
    }
}

package com.team420.kekstore.nearby;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import com.team420.kekstore.views.main.NearbyViewBinder;

public class UsbDeviceMediaMountedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        String action = intent.getAction();
        if (Environment.MEDIA_BAD_REMOVAL.equals(action)
                || Environment.MEDIA_MOUNTED.equals(action)
                || Environment.MEDIA_REMOVED.equals(action)
                || Environment.MEDIA_EJECTING.equals(action)) {
            NearbyViewBinder.updateUsbOtg(context);
        }
    }
}

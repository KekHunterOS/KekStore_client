package org.fdroid.kekstore.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import org.fdroid.kekstore.Utils;
import org.fdroid.kekstore.net.WifiStateChangeService;

public class WifiStateChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "WifiStateChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            WifiStateChangeService.start(context, intent);
        } else {
            Utils.debugLog(TAG, "received unsupported Intent: " + intent);
        }
    }
}

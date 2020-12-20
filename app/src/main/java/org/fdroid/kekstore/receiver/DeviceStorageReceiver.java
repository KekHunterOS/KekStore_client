package org.fdroid.kekstore.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.fdroid.kekstore.CleanCacheService;
import org.fdroid.kekstore.DeleteCacheService;
import org.fdroid.kekstore.Utils;

public class DeviceStorageReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (Intent.ACTION_DEVICE_STORAGE_LOW.equals(action)) {
            int percentageFree = Utils.getPercent(Utils.getImageCacheDirAvailableMemory(context),
                    Utils.getImageCacheDirTotalMemory(context));
            if (percentageFree > 2) {
                CleanCacheService.start(context);
            } else {
                DeleteCacheService.deleteAll(context);
            }
        }
    }
}

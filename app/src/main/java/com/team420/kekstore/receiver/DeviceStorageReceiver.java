package com.team420.kekstore.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.team420.kekstore.DeleteCacheService;
import com.team420.kekstore.Utils;
import com.team420.kekstore.work.CleanCacheWorker;

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
                CleanCacheWorker.schedule(context);
            } else {
                DeleteCacheService.deleteAll(context);
            }
        }
    }
}

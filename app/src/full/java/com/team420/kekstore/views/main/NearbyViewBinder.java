package com.team420.kekstore.views.main;

import android.Manifest;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.team420.kekstore.R;
import com.team420.kekstore.Utils;
import com.team420.kekstore.nearby.SDCardScannerService;
import com.team420.kekstore.nearby.SwapService;
import com.team420.kekstore.nearby.TreeUriScannerIntentService;

import java.io.File;
import java.util.List;

/**
 * A splash screen encouraging people to start the swap process. The swap
 * process is quite heavy duty in that it fires up Bluetooth and/or WiFi
 * in  order to scan for peers. As such, it is quite convenient to have a
 * more lightweight view to show in the main navigation that doesn't
 * automatically start doing things when the user touches the navigation
 * menu in the bottom navigation.
 * <p>
 * Lots of pieces of the nearby/swap functionality require that the user grant
 * F-Droid permissions at runtime on {@code android-23} and higher. On devices
 * that have a removable SD Card that is currently mounted, this will request
 * permission to read it, so that F-Droid can look for repos on the SD Card.
 * <p>
 * Once {@link Manifest.permission#READ_EXTERNAL_STORAGE} or
 * {@link Manifest.permission#WRITE_EXTERNAL_STORAGE} is granted for F-Droid,
 * then it can read any file on an SD Card and no more prompts are needed. For
 * USB OTG drives, the only way to get read permissions is to prompt the user
 * via {@link Intent#ACTION_OPEN_DOCUMENT_TREE}.
 * <p>
 * For write permissions, {@code android-19} and {@code android-20} devices are
 * basically screwed here.  {@link Intent#ACTION_OPEN_DOCUMENT_TREE} was added
 * in {@code android-21}, and there does not seem to be any other way to get
 * write access to the the removable storage.
 *
 * @see TreeUriScannerIntentService
 * @see com.team420.kekstore.nearby.SDCardScannerService
 * <p>
 * TODO use {@link StorageManager#registerStorageVolumeCallback(Executor, StorageManager.StorageVolumeCallback)}
 */
public class NearbyViewBinder {
    public static final String TAG = "NearbyViewBinder";

    private static File externalStorage = null;
    private static View swapView;

    NearbyViewBinder(final AppCompatActivity activity, FrameLayout parent) {
        swapView = activity.getLayoutInflater().inflate(R.layout.main_tab_swap, parent, true);

        TextView subtext = swapView.findViewById(R.id.both_parties_need_fdroid_text);
        subtext.setText(activity.getString(R.string.nearby_splash__both_parties_need_fdroid,
                activity.getString(R.string.app_name)));

        ImageView nearbySplash = swapView.findViewById(R.id.image);

        Button startButton = swapView.findViewById(R.id.find_people_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String coarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION;
                if (Build.VERSION.SDK_INT >= 23
                        && PackageManager.PERMISSION_GRANTED
                        != ContextCompat.checkSelfPermission(activity, coarseLocation)) {
                    ActivityCompat.requestPermissions(activity, new String[]{coarseLocation},
                            MainActivity.REQUEST_LOCATION_PERMISSIONS);
                } else {
                    ContextCompat.startForegroundService(activity, new Intent(activity, SwapService.class));
                }
            }
        });

        if (Build.VERSION.SDK_INT >= 21) {
            File[] dirs = activity.getExternalFilesDirs("");
            if (dirs != null) {
                for (File dir : dirs) {
                    if (dir != null && Environment.isExternalStorageRemovable(dir)) {
                        String state = Environment.getExternalStorageState(dir);
                        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)
                                || Environment.MEDIA_MOUNTED.equals(state)) {
                            // remove Android/data/com.team420.kekstore/files to get root
                            externalStorage = dir.getParentFile().getParentFile().getParentFile().getParentFile();
                            break;
                        }
                    }
                }
            }
        } else if (Environment.isExternalStorageRemovable() &&
                (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY)
                        || Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))) {
            Log.i(TAG, "<21 isExternalStorageRemovable MEDIA_MOUNTED");
            externalStorage = Environment.getExternalStorageDirectory();
        }

        final String writeExternalStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE;

        if (externalStorage != null
                || PackageManager.PERMISSION_GRANTED
                != ContextCompat.checkSelfPermission(activity, writeExternalStorage)) {
            nearbySplash.setVisibility(View.GONE);
            TextView readExternalStorageText = swapView.findViewById(R.id.read_external_storage_text);
            readExternalStorageText.setVisibility(View.VISIBLE);
            Button requestReadExternalStorage = swapView.findViewById(R.id.request_read_external_storage_button);
            requestReadExternalStorage.setVisibility(View.VISIBLE);
            requestReadExternalStorage.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = 21)
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= 23
                            && (externalStorage == null || !externalStorage.canRead())
                            && PackageManager.PERMISSION_GRANTED
                            != ContextCompat.checkSelfPermission(activity, writeExternalStorage)) {
                        ActivityCompat.requestPermissions(activity, new String[]{writeExternalStorage},
                                MainActivity.REQUEST_STORAGE_PERMISSIONS);
                    } else {
                        Toast.makeText(activity,
                                activity.getString(R.string.scan_removable_storage_toast, externalStorage),
                                Toast.LENGTH_SHORT).show();
                        SDCardScannerService.scan(activity);
                    }
                }
            });
        }

        updateUsbOtg(activity);
    }

    public static void updateUsbOtg(final Context context) {
        if (Build.VERSION.SDK_INT < 24) {
            return;
        }
        if (swapView == null) {
            Utils.debugLog(TAG, "swapView == null");
            return;
        }
        TextView storageVolumeText = swapView.findViewById(R.id.storage_volume_text);
        Button requestStorageVolume = swapView.findViewById(R.id.request_storage_volume_button);
        storageVolumeText.setVisibility(View.GONE);
        requestStorageVolume.setVisibility(View.GONE);

        final StorageManager storageManager = ContextCompat.getSystemService(context, StorageManager.class);
        for (final StorageVolume storageVolume : storageManager.getStorageVolumes()) {
            if (storageVolume.isRemovable() && !storageVolume.isPrimary()) {
                Log.i(TAG, "StorageVolume: " + storageVolume);
                Intent tmpIntent = null;
                if (Build.VERSION.SDK_INT < 29) {
                    tmpIntent = storageVolume.createAccessIntent(null);
                } else {
                    tmpIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    tmpIntent.putExtra(DocumentsContract.EXTRA_INITIAL_URI,
                            Uri.parse("content://"
                                    + TreeUriScannerIntentService.EXTERNAL_STORAGE_PROVIDER_AUTHORITY
                                    + "/tree/"
                                    + storageVolume.getUuid()
                                    + "%3A/document/"
                                    + storageVolume.getUuid()
                                    + "%3A"));
                }
                if (tmpIntent == null) {
                    Utils.debugLog(TAG, "Got null Storage Volume access Intent");
                    return;
                }
                final Intent intent = tmpIntent;

                storageVolumeText.setVisibility(View.VISIBLE);

                String text = storageVolume.getDescription(context);
                if (!TextUtils.isEmpty(text)) {
                    requestStorageVolume.setText(text);
                    UsbDevice usb = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (usb != null) {
                        text = String.format("%s (%s %s)", text, usb.getManufacturerName(), usb.getProductName());
                        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
                    }
                }

                requestStorageVolume.setVisibility(View.VISIBLE);
                requestStorageVolume.setOnClickListener(new View.OnClickListener() {
                    @Override
                    @RequiresApi(api = 24)
                    public void onClick(View v) {
                        List<UriPermission> list = context.getContentResolver().getPersistedUriPermissions();
                        if (list != null) for (UriPermission uriPermission : list) {
                            Uri uri = uriPermission.getUri();
                            if (uri.getPath().equals(String.format("/tree/%s:", storageVolume.getUuid()))) {
                                intent.setData(uri);
                                TreeUriScannerIntentService.onActivityResult(context, intent);
                                return;
                            }
                        }

                        AppCompatActivity activity = null;
                        if (context instanceof AppCompatActivity) {
                            activity = (AppCompatActivity) context;
                        } else if (swapView != null && swapView.getContext() instanceof AppCompatActivity) {
                            activity = (AppCompatActivity) swapView.getContext();
                        }

                        if (activity != null) {
                            activity.startActivityForResult(intent, MainActivity.REQUEST_STORAGE_ACCESS);
                        } else {
                            // scan in the background without requesting permissions
                            Toast.makeText(context.getApplicationContext(),
                                    context.getString(R.string.scan_removable_storage_toast, externalStorage),
                                    Toast.LENGTH_SHORT).show();
                            SDCardScannerService.scan(context);
                        }
                    }
                });
            }
        }
    }
}

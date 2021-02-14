package com.team420.kekstore.installer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import org.apache.commons.io.FileUtils;
import com.team420.kekstore.data.Apk;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * An {@link IntentService} subclass for installing {@code .obf} and {@code .obf.zip}
 * map files into OsmAnd.  This will unzip the {@code .obf}
 */
public class ObfInstallerService extends IntentService {
    private static final String TAG = "ObfInstallerService";

    private static final String ACTION_INSTALL_OBF = "com.team420.kekstore.installer.action.INSTALL_OBF";

    private static final String EXTRA_OBF_PATH = "com.team420.kekstore.installer.extra.OBF_PATH";

    public ObfInstallerService() {
        super("ObfInstallerService");
    }

    public static void install(Context context, Uri canonicalUri, Apk apk, File path) {
        Intent intent = new Intent(context, ObfInstallerService.class);
        intent.setAction(ACTION_INSTALL_OBF);
        intent.putExtra(com.team420.kekstore.net.Downloader.EXTRA_CANONICAL_URL, canonicalUri.toString());
        intent.putExtra(Installer.EXTRA_APK, apk);
        intent.putExtra(EXTRA_OBF_PATH, path.getAbsolutePath());
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null || !ACTION_INSTALL_OBF.equals(intent.getAction())) {
            Log.e(TAG, "received invalid intent: " + intent);
            return;
        }
        Uri canonicalUri = Uri.parse(intent.getStringExtra(com.team420.kekstore.net.Downloader.EXTRA_CANONICAL_URL));
        final Apk apk = intent.getParcelableExtra(Installer.EXTRA_APK);
        final String path = intent.getStringExtra(EXTRA_OBF_PATH);
        final String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        if ("obf".equals(extension)) {
            sendPostInstallAndCompleteIntents(canonicalUri, apk, new File(path));
            return;
        }
        if (!"zip".equals(extension)) {
            sendBroadcastInstall(Installer.ACTION_INSTALL_INTERRUPTED, canonicalUri, apk,
                    "Only .obf and .zip files are supported: " + path);
            return;
        }
        try {
            File zip = new File(path);
            ZipFile zipFile = new ZipFile(zip);
            if (zipFile.size() < 1) {
                sendBroadcastInstall(Installer.ACTION_INSTALL_INTERRUPTED, canonicalUri, apk,
                        "Corrupt or empty ZIP file!");
            }
            ZipEntry zipEntry = zipFile.entries().nextElement();
            File extracted = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    zipEntry.getName());
            FileUtils.copyInputStreamToFile(zipFile.getInputStream(zipEntry), extracted);
            zip.delete();
            sendPostInstallAndCompleteIntents(canonicalUri, apk, extracted);
        } catch (IOException e) {
            e.printStackTrace();
            sendBroadcastInstall(Installer.ACTION_INSTALL_INTERRUPTED, canonicalUri, apk, e.getMessage());
        }
    }

    private void sendBroadcastInstall(String action, Uri canonicalUri, Apk apk, String msg) {
        Installer.sendBroadcastInstall(this, canonicalUri, action, apk, null, msg);
    }

    /**
     * Once the file is downloaded and installed, send an {@link Intent} to
     * let map apps know that the file is available for install.
     * <p>
     * When this was written, OsmAnd only supported importing OBF files via a
     * {@code file:///} URL, so this disables {@link android.os.FileUriExposedException}.
     */
    void sendPostInstallAndCompleteIntents(Uri canonicalUri, Apk apk, File file) {
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("obf");
        if (TextUtils.isEmpty(mimeType)) {
            mimeType = "application/octet-stream";
        }
        intent.setDataAndType(Uri.fromFile(file), mimeType);
        if (Build.VERSION.SDK_INT >= 23) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        if (intent != null && intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.i(TAG, "No AppCompatActivity available to handle " + intent);
        }
        sendBroadcastInstall(Installer.ACTION_INSTALL_COMPLETE, canonicalUri, apk, null);
    }
}
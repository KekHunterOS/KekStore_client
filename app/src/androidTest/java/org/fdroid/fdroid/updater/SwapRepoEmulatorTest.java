package org.fdroid.kekstore.updater;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.text.TextUtils;
import android.util.Log;
import org.fdroid.kekstore.BuildConfig;
import org.fdroid.kekstore.FDroidApp;
import org.fdroid.kekstore.Hasher;
import org.fdroid.kekstore.IndexUpdater;
import org.fdroid.kekstore.Preferences;
import org.fdroid.kekstore.Utils;
import org.fdroid.kekstore.data.Apk;
import org.fdroid.kekstore.data.ApkProvider;
import org.fdroid.kekstore.data.App;
import org.fdroid.kekstore.data.AppProvider;
import org.fdroid.kekstore.data.Repo;
import org.fdroid.kekstore.data.RepoProvider;
import org.fdroid.kekstore.data.Schema;
import org.fdroid.kekstore.localrepo.LocalRepoKeyStore;
import org.fdroid.kekstore.localrepo.LocalRepoManager;
import org.fdroid.kekstore.localrepo.LocalRepoService;
import org.fdroid.kekstore.net.LocalHTTPD;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.security.cert.Certificate;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@LargeTest
public class SwapRepoEmulatorTest {
    public static final String TAG = "SwapRepoEmulatorTest";

    /**
     * @see org.fdroid.kekstore.net.WifiStateChangeService.WifiInfoThread#run()
     */
    @Test
    public void testSwap()
            throws IOException, LocalRepoKeyStore.InitException, IndexUpdater.UpdateException, InterruptedException {
        Looper.prepare();
        LocalHTTPD localHttpd = null;
        try {
            Log.i(TAG, "REPO: " + FDroidApp.repo);
            final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
            Preferences.setupForTests(context);

            FDroidApp.initWifiSettings();
            assertNull(FDroidApp.repo.address);

            final CountDownLatch latch = new CountDownLatch(1);
            new Thread() {
                @Override
                public void run() {
                    while (FDroidApp.repo.address == null) {
                        try {
                            Log.i(TAG, "Waiting for IP address... " + FDroidApp.repo.address);
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            // ignored
                        }
                    }
                    latch.countDown();
                }
            }.start();
            latch.await(10, TimeUnit.MINUTES);
            assertNotNull(FDroidApp.repo.address);

            LocalRepoService.runProcess(context, new String[]{context.getPackageName()});
            Log.i(TAG, "REPO: " + FDroidApp.repo);
            File indexJarFile = LocalRepoManager.get(context).getIndexJar();
            assertTrue(indexJarFile.isFile());

            localHttpd = new LocalHTTPD(
                    context,
                    null,
                    FDroidApp.port,
                    LocalRepoManager.get(context).getWebRoot(),
                    false);
            localHttpd.start();
            Thread.sleep(100); // give the server some tine to start.
            assertTrue(localHttpd.isAlive());

            LocalRepoKeyStore localRepoKeyStore = LocalRepoKeyStore.get(context);
            Certificate localCert = localRepoKeyStore.getCertificate();
            String signingCert = Hasher.hex(localCert);
            assertFalse(TextUtils.isEmpty(signingCert));
            assertFalse(TextUtils.isEmpty(Utils.calcFingerprint(localCert)));

            Repo repoToDelete = RepoProvider.Helper.findByAddress(context, FDroidApp.repo.address);
            while (repoToDelete != null) {
                Log.d(TAG, "Removing old test swap repo matching this one: " + repoToDelete.address);
                RepoProvider.Helper.remove(context, repoToDelete.getId());
                repoToDelete = RepoProvider.Helper.findByAddress(context, FDroidApp.repo.address);
            }

            ContentValues values = new ContentValues(4);
            values.put(Schema.RepoTable.Cols.SIGNING_CERT, signingCert);
            values.put(Schema.RepoTable.Cols.ADDRESS, FDroidApp.repo.address);
            values.put(Schema.RepoTable.Cols.NAME, FDroidApp.repo.name);
            values.put(Schema.RepoTable.Cols.IS_SWAP, true);
            final String lastEtag = UUID.randomUUID().toString();
            values.put(Schema.RepoTable.Cols.LAST_ETAG, lastEtag);
            RepoProvider.Helper.insert(context, values);
            Repo repo = RepoProvider.Helper.findByAddress(context, FDroidApp.repo.address);
            assertTrue(repo.isSwap);
            assertNotEquals(-1, repo.getId());
            assertTrue(repo.name.startsWith(FDroidApp.repo.name));
            assertEquals(lastEtag, repo.lastetag);
            assertNull(repo.lastUpdated);

            assertTrue(isPortInUse(FDroidApp.ipAddressString, FDroidApp.port));
            Thread.sleep(100);
            IndexUpdater updater = new IndexUpdater(context, repo);
            updater.update();
            assertTrue(updater.hasChanged());

            repo = RepoProvider.Helper.findByAddress(context, FDroidApp.repo.address);
            final Date lastUpdated = repo.lastUpdated;
            assertTrue("repo lastUpdated should be updated", new Date(2019, 5, 13).compareTo(repo.lastUpdated) > 0);

            App app = AppProvider.Helper.findSpecificApp(context.getContentResolver(),
                    context.getPackageName(), repo.getId());
            assertEquals(context.getPackageName(), app.packageName);

            List<Apk> apks = ApkProvider.Helper.findByRepo(context, repo, Schema.ApkTable.Cols.ALL);
            assertEquals(1, apks.size());
            for (Apk apk : apks) {
                Log.i(TAG, "Apk: " + apk);
                assertEquals(context.getPackageName(), apk.packageName);
                assertEquals(BuildConfig.VERSION_NAME, apk.versionName);
                assertEquals(BuildConfig.VERSION_CODE, apk.versionCode);
                assertEquals(app.repoId, apk.repoId);
            }

            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities(mainIntent, 0);
            HashSet<String> packageNames = new HashSet<>();
            for (ResolveInfo resolveInfo : resolveInfoList) {
                if (!isSystemPackage(resolveInfo)) {
                    Log.i(TAG, "resolveInfo: " + resolveInfo);
                    packageNames.add(resolveInfo.activityInfo.packageName);
                }
            }
            LocalRepoService.runProcess(context, packageNames.toArray(new String[0]));

            updater = new IndexUpdater(context, repo);
            updater.update();
            assertTrue(updater.hasChanged());
            assertTrue("repo lastUpdated should be updated", lastUpdated.compareTo(repo.lastUpdated) < 0);

            for (String packageName : packageNames) {
                assertNotNull(ApkProvider.Helper.findByPackageName(context, packageName));
            }
        } finally {
            if (localHttpd != null) {
                localHttpd.stop();
            }
        }
        if (localHttpd != null) {
            assertFalse(localHttpd.isAlive());
        }
    }

    private boolean isPortInUse(String host, int port) {
        boolean result = false;

        try {
            (new Socket(host, port)).close();
            result = true;
        } catch (IOException e) {
            // Could not connect.
            e.printStackTrace();
        }
        return result;
    }

    private boolean isSystemPackage(ResolveInfo resolveInfo) {
        return (resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }
}

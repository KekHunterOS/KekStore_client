package org.fdroid.kekstore.localrepo;

import android.content.Context;
import org.fdroid.kekstore.FDroidApp;
import org.fdroid.kekstore.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowLog;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


@RunWith(RobolectricTestRunner.class)
public class LocalHTTPDManagerTest {

    @Test
    public void testStartStop() throws InterruptedException {
        ShadowLog.stream = System.out;
        Context context = RuntimeEnvironment.application;

        final String host = "localhost";
        final int port = 8888;
        assertFalse(Utils.isServerSocketInUse(port));
        LocalHTTPDManager.stop(context);

        FDroidApp.ipAddressString = host;
        FDroidApp.port = port;

        LocalHTTPDManager.start(context, false);
        final CountDownLatch startLatch = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Utils.isServerSocketInUse(port)) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        fail();
                    }
                }
                startLatch.countDown();
            }
        }).start();
        assertTrue(startLatch.await(30, TimeUnit.SECONDS));
        assertTrue(Utils.isServerSocketInUse(port));
        assertTrue(Utils.canConnectToSocket(host, port));

        LocalHTTPDManager.stop(context);
        final CountDownLatch stopLatch = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Utils.isServerSocketInUse(port)) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        fail();
                    }
                }
                stopLatch.countDown();
            }
        }).start();
        assertTrue(stopLatch.await(10, TimeUnit.SECONDS));
    }
}

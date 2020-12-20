package org.fdroid.kekstore.localrepo;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import org.fdroid.kekstore.FDroidApp;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class BonjourManagerTest {

    private static final String NAME = "Robolectric-test";
    private static final String LOCALHOST = "localhost";
    private static final int PORT = 8888;

    @Test
    public void testStartStop() throws InterruptedException {
        Context context = InstrumentationRegistry.getTargetContext();

        FDroidApp.ipAddressString = LOCALHOST;
        FDroidApp.port = PORT;

        final CountDownLatch addedLatch = new CountDownLatch(1);
        final CountDownLatch resolvedLatch = new CountDownLatch(1);
        final CountDownLatch removedLatch = new CountDownLatch(1);
        BonjourManager.start(context, NAME, false,
                new ServiceListener() {
                    @Override
                    public void serviceAdded(ServiceEvent serviceEvent) {
                        System.out.println("Service added: " + serviceEvent.getInfo());
                        if (NAME.equals(serviceEvent.getName())) {
                            addedLatch.countDown();
                        }
                    }

                    @Override
                    public void serviceRemoved(ServiceEvent serviceEvent) {
                        System.out.println("Service removed: " + serviceEvent.getInfo());
                        removedLatch.countDown();
                    }

                    @Override
                    public void serviceResolved(ServiceEvent serviceEvent) {
                        System.out.println("Service resolved: " + serviceEvent.getInfo());
                        if (NAME.equals(serviceEvent.getName())) {
                            resolvedLatch.countDown();
                        }
                    }
                }, getBlankServiceListener());
        BonjourManager.setVisible(context, true);
        assertTrue(addedLatch.await(30, TimeUnit.SECONDS));
        assertTrue(resolvedLatch.await(30, TimeUnit.SECONDS));
        BonjourManager.setVisible(context, false);
        assertTrue(removedLatch.await(30, TimeUnit.SECONDS));
        BonjourManager.stop(context);
    }

    @Test
    public void testRestart() throws InterruptedException {
        Context context = InstrumentationRegistry.getTargetContext();

        FDroidApp.ipAddressString = LOCALHOST;
        FDroidApp.port = PORT;

        BonjourManager.start(context, NAME, false, getBlankServiceListener(), getBlankServiceListener());

        final CountDownLatch addedLatch = new CountDownLatch(1);
        final CountDownLatch resolvedLatch = new CountDownLatch(1);
        final CountDownLatch removedLatch = new CountDownLatch(1);
        BonjourManager.restart(context, NAME, false,
                new ServiceListener() {
                    @Override
                    public void serviceAdded(ServiceEvent serviceEvent) {
                        System.out.println("Service added: " + serviceEvent.getInfo());
                        if (NAME.equals(serviceEvent.getName())) {
                            addedLatch.countDown();
                        }
                    }

                    @Override
                    public void serviceRemoved(ServiceEvent serviceEvent) {
                        System.out.println("Service removed: " + serviceEvent.getInfo());
                        removedLatch.countDown();
                    }

                    @Override
                    public void serviceResolved(ServiceEvent serviceEvent) {
                        System.out.println("Service resolved: " + serviceEvent.getInfo());
                        if (NAME.equals(serviceEvent.getName())) {
                            resolvedLatch.countDown();
                        }
                    }
                }, getBlankServiceListener());
        BonjourManager.setVisible(context, true);
        assertTrue(addedLatch.await(30, TimeUnit.SECONDS));
        assertTrue(resolvedLatch.await(30, TimeUnit.SECONDS));
        BonjourManager.setVisible(context, false);
        assertTrue(removedLatch.await(30, TimeUnit.SECONDS));
        BonjourManager.stop(context);
    }

    private ServiceListener getBlankServiceListener() {
        return new ServiceListener() {
            @Override
            public void serviceAdded(ServiceEvent serviceEvent) {
            }

            @Override
            public void serviceRemoved(ServiceEvent serviceEvent) {
            }

            @Override
            public void serviceResolved(ServiceEvent serviceEvent) {
            }
        };
    }
}

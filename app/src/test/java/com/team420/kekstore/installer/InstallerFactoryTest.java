package com.team420.kekstore.installer;

import android.content.ContextWrapper;
import androidx.test.core.app.ApplicationProvider;
import com.team420.kekstore.Preferences;
import com.team420.kekstore.data.Apk;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class InstallerFactoryTest {

    private ContextWrapper context;

    @Before
    public final void setUp() {
        context = ApplicationProvider.getApplicationContext();
        Preferences.setupForTests(context);
    }

    @Test
    public void testApkInstallerInstance() {
        for (String filename : new String[]{"test.apk", "A.APK", "b.ApK"}) {
            Apk apk = new Apk();
            apk.apkName = filename;
            apk.packageName = "test";
            Installer installer = InstallerFactory.create(context, apk);
            assertEquals(filename + " should use a DefaultInstaller",
                    DefaultInstaller.class,
                    installer.getClass());
        }
    }

    @Test
    public void testFileInstallerInstance() {
        for (String filename : new String[]{"com.team420.kekstore.privileged.ota_2110.zip", "test.ZIP"}) {
            Apk apk = new Apk();
            apk.apkName = filename;
            apk.packageName = "cafe0088";
            Installer installer = InstallerFactory.create(context, apk);
            assertEquals("should be a FileInstaller",
                    FileInstaller.class,
                    installer.getClass());
        }
    }
}

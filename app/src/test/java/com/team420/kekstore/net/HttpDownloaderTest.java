package com.team420.kekstore.net;

import android.net.Uri;

import org.apache.commons.net.util.SubnetUtils;
import com.team420.kekstore.FDroidApp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@SuppressWarnings("LineLength")
public class HttpDownloaderTest {

    @Test
    public void testIsSwapUri() throws MalformedURLException {
        FDroidApp.subnetInfo = new SubnetUtils("192.168.0.112/24").getInfo();
        String urlString = "http://192.168.0.112:8888/fdroid/repo?fingerprint=113F56CBFA967BA825DD13685A06E35730E0061C6BB046DF88A";
        assertTrue(HttpDownloader.isSwapUrl("192.168.0.112", 8888)); // NOPMD
        assertTrue(HttpDownloader.isSwapUrl(Uri.parse(urlString)));
        assertTrue(HttpDownloader.isSwapUrl(new URL(urlString)));

        assertFalse(HttpDownloader.isSwapUrl("192.168.1.112", 8888)); // NOPMD
        assertFalse(HttpDownloader.isSwapUrl("192.168.0.112", 80)); // NOPMD
        assertFalse(HttpDownloader.isSwapUrl(Uri.parse("https://malware.com:8888")));
        assertFalse(HttpDownloader.isSwapUrl(new URL("https://www.google.com")));
    }
}

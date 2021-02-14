package com.team420.kekstore.data;

import android.content.ContentResolver;

import androidx.test.core.app.ApplicationProvider;

import com.team420.kekstore.TestUtils;
import com.team420.kekstore.data.Schema.InstalledAppTable;
import com.team420.kekstore.mock.MockApk;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

import static com.team420.kekstore.Assert.assertInvalidUri;
import static com.team420.kekstore.Assert.assertValidUri;

@RunWith(RobolectricTestRunner.class)
@SuppressWarnings("LineLength")
public class ProviderUriTests {

    private static final String CONTENT_URI_BASE = "content://" + FDroidProvider.AUTHORITY;
    private static final String APK_PROVIDER_URI_BASE = CONTENT_URI_BASE + ".ApkProvider";
    private static final String APP_PROVIDER_URI_BASE = CONTENT_URI_BASE + ".AppProvider";
    private static final String TEMP_APP_PROVIDER_URI_BASE = CONTENT_URI_BASE + ".TempAppProvider";

    private ContentResolver resolver;

    @Before
    public void setup() {
        resolver = ApplicationProvider.getApplicationContext().getContentResolver();
    }

    @After
    public void teardown() {
        DBHelper.clearDbHelperSingleton();
    }

    @Test
    public void invalidInstalledAppProviderUris() {
        TestUtils.registerContentProvider(InstalledAppProvider.getAuthority(), InstalledAppProvider.class);
        assertInvalidUri(resolver, InstalledAppProvider.getAuthority());
        assertInvalidUri(resolver, "blah");
    }

    @Test
    public void validInstalledAppProviderUris() {
        TestUtils.registerContentProvider(InstalledAppProvider.getAuthority(), InstalledAppProvider.class);
        String[] projection = new String[]{InstalledAppTable.Cols._ID};
        assertValidUri(resolver, InstalledAppProvider.getContentUri(), projection);
        assertValidUri(resolver, InstalledAppProvider.getAppUri("org.example.app"), projection);
        assertValidUri(resolver, InstalledAppProvider.getSearchUri("blah"), projection);
        assertValidUri(resolver, InstalledAppProvider.getSearchUri("\"blah\""), projection);
        assertValidUri(resolver, InstalledAppProvider.getSearchUri("blah & sneh"), projection);
        assertValidUri(resolver, InstalledAppProvider.getSearchUri("http://blah.example.com?sneh=\"sneh\""), projection);
    }

    @Test
    public void invalidRepoProviderUris() {
        TestUtils.registerContentProvider(RepoProvider.getAuthority(), RepoProvider.class);
        assertInvalidUri(resolver, RepoProvider.getAuthority());
        assertInvalidUri(resolver, "blah");
    }

    @Test
    public void validRepoProviderUris() {
        TestUtils.registerContentProvider(RepoProvider.getAuthority(), RepoProvider.class);
        String[] projection = new String[]{Schema.RepoTable.Cols._ID};
        assertValidUri(resolver, RepoProvider.getContentUri(), projection);
        assertValidUri(resolver, RepoProvider.getContentUri(10000L), projection);
        assertValidUri(resolver, RepoProvider.allExceptSwapUri(), projection);
    }

    @Test
    public void invalidAppProviderUris() {
        TestUtils.registerContentProvider(AppProvider.getAuthority(), AppProvider.class);
        assertInvalidUri(resolver, AppProvider.getAuthority());
        assertInvalidUri(resolver, "blah");
    }

    @Test
    public void validAppProviderUris() {
        TestUtils.registerContentProvider(AppProvider.getAuthority(), AppProvider.class);
        String[] projection = new String[]{Schema.AppMetadataTable.Cols._ID};
        assertValidUri(resolver, AppProvider.getContentUri(), APP_PROVIDER_URI_BASE, projection);
        assertValidUri(resolver, AppProvider.getSearchUri("'searching!'", null), APP_PROVIDER_URI_BASE + "/search/'searching!'", projection);
        assertValidUri(resolver, AppProvider.getSearchUri("'searching!'", "Games"), APP_PROVIDER_URI_BASE + "/search/'searching!'/Games", projection);
        assertValidUri(resolver, AppProvider.getSearchUri("/", null), APP_PROVIDER_URI_BASE + "/search/%2F", projection);
        assertValidUri(resolver, AppProvider.getSearchUri("/", "Games"), APP_PROVIDER_URI_BASE + "/search/%2F/Games", projection);
        assertValidUri(resolver, AppProvider.getSearchUri("", null), APP_PROVIDER_URI_BASE, projection);
        assertValidUri(resolver, AppProvider.getSearchUri("", "Games"), APP_PROVIDER_URI_BASE + "/category/Games", projection);
        assertValidUri(resolver, AppProvider.getCategoryUri("Games"), APP_PROVIDER_URI_BASE + "/category/Games", projection);
        assertValidUri(resolver, AppProvider.getSearchUri((String) null, null), APP_PROVIDER_URI_BASE, projection);
        assertValidUri(resolver, AppProvider.getSearchUri((String) null, "Games"), APP_PROVIDER_URI_BASE + "/category/Games", projection);
        assertValidUri(resolver, AppProvider.getInstalledUri(), APP_PROVIDER_URI_BASE + "/installed", projection);
        assertValidUri(resolver, AppProvider.getCanUpdateUri(), APP_PROVIDER_URI_BASE + "/canUpdate", projection);

        App app = new App();
        app.repoId = 1;
        app.packageName = "com.team420.kekstore";

        assertValidUri(resolver, AppProvider.getSpecificAppUri(app.packageName, app.repoId),
                APP_PROVIDER_URI_BASE + "/app/1/com.team420.kekstore", projection);
    }

    @Test
    public void validTempAppProviderUris() {
        TestUtils.registerContentProvider(TempAppProvider.getAuthority(), TempAppProvider.class);
        String[] projection = new String[]{Schema.AppMetadataTable.Cols._ID};

        // Required so that the `assertValidUri` calls below will indeed have a real temp_fdroid_app
        // table to query.
        TempAppProvider.Helper.init(TestUtils.createContextWithContentResolver(resolver), 123);

        List<String> packageNames = new ArrayList<>(2);
        packageNames.add("com.team420.kekstore");
        packageNames.add("com.example.com");

        assertValidUri(resolver, TempAppProvider.getAppsUri(packageNames, 1),
                TEMP_APP_PROVIDER_URI_BASE + "/apps/1/com.team420.kekstore%2Ccom.example.com", projection);
        assertValidUri(resolver, TempAppProvider.getContentUri(), TEMP_APP_PROVIDER_URI_BASE, projection);
    }

    @Test
    public void invalidApkProviderUris() {
        TestUtils.registerContentProvider(ApkProvider.getAuthority(), ApkProvider.class);
        assertInvalidUri(resolver, ApkProvider.getAuthority());
        assertInvalidUri(resolver, "blah");
    }

    @Test
    public void validApkProviderUris() {
        TestUtils.registerContentProvider(ApkProvider.getAuthority(), ApkProvider.class);
        String[] projection = new String[]{Schema.ApkTable.Cols._ID};

        List<Apk> apks = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            apks.add(new MockApk("com.example." + i, i));
        }

        assertValidUri(resolver, ApkProvider.getContentUri(),
                APK_PROVIDER_URI_BASE, projection);
        assertValidUri(resolver, ApkProvider.getAppUri("com.team420.kekstore"),
                APK_PROVIDER_URI_BASE + "/app/com.team420.kekstore", projection);
        assertValidUri(resolver, ApkProvider.getApkFromAnyRepoUri(new MockApk("com.team420.kekstore", 100)),
                APK_PROVIDER_URI_BASE + "/apk-any-repo/100/com.team420.kekstore", projection);
        assertValidUri(resolver, ApkProvider.getApkFromAnyRepoUri("com.team420.kekstore", 100, null),
                APK_PROVIDER_URI_BASE + "/apk-any-repo/100/com.team420.kekstore", projection);
        assertValidUri(resolver, ApkProvider.getRepoUri(1000),
                APK_PROVIDER_URI_BASE + "/repo/1000", projection);
    }
}

package com.team420.kekstore.views;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import com.team420.kekstore.Assert;
import com.team420.kekstore.Preferences;
import com.team420.kekstore.R;
import com.team420.kekstore.data.Apk;
import com.team420.kekstore.data.App;
import com.team420.kekstore.data.AppProviderTest;
import com.team420.kekstore.data.DBHelper;
import com.team420.kekstore.data.FDroidProviderTest;
import com.team420.kekstore.data.Repo;
import com.team420.kekstore.data.RepoProviderTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@Config(application = Application.class)
@RunWith(RobolectricTestRunner.class)
public class AppDetailsAdapterTest extends FDroidProviderTest {

    private App app;
    private Context themeContext;

    @Before
    public void setup() {
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(context));
        Preferences.setupForTests(context);

        Repo repo = RepoProviderTest.insertRepo(context, "http://www.example.com/fdroid/repo", "", "", "Test Repo");
        app = AppProviderTest.insertApp(contentResolver, context, "com.example.app", "Test App",
                new ContentValues(), repo.getId());

        themeContext = new ContextThemeWrapper(ApplicationProvider.getApplicationContext(), R.style.AppBaseThemeDark);
    }

    @After
    public void teardown() {
        ImageLoader.getInstance().destroy();
        DBHelper.clearDbHelperSingleton();
    }

    @Test
    public void appWithNoVersionsOrScreenshots() {
        AppDetailsRecyclerViewAdapter adapter = new AppDetailsRecyclerViewAdapter(context, app, dummyCallbacks);
        populateViewHolders(adapter);

        assertEquals(3, adapter.getItemCount());
    }

    @Test
    public void appWithScreenshots() {
        app.phoneScreenshots = new String[]{"screenshot1.png", "screenshot2.png"};

        AppDetailsRecyclerViewAdapter adapter = new AppDetailsRecyclerViewAdapter(context, app, dummyCallbacks);
        populateViewHolders(adapter);

        assertEquals(4, adapter.getItemCount());

    }

    @Test
    public void appWithVersions() {
        Assert.insertApk(context, app, 1);
        Assert.insertApk(context, app, 2);
        Assert.insertApk(context, app, 3);

        AppDetailsRecyclerViewAdapter adapter = new AppDetailsRecyclerViewAdapter(context, app, dummyCallbacks);
        populateViewHolders(adapter);

        // Starts collapsed, now showing versions at all.
        assertEquals(3, adapter.getItemCount());

        adapter.setShowVersions(true);
        assertEquals(6, adapter.getItemCount());

        adapter.setShowVersions(false);
        assertEquals(3, adapter.getItemCount());
    }

    /**
     * Ensures that every single item in the adapter gets its view holder created and bound.
     * Doesn't care about what type of holder it should be, the adapter is able to figure all that
     * out for us .
     */
    private void populateViewHolders(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        ViewGroup parent = (ViewGroup) LayoutInflater.from(themeContext).inflate(R.layout.app_details2_links, null);
        for (int i = 0; i < adapter.getItemCount(); i++) {
            RecyclerView.ViewHolder viewHolder = adapter.createViewHolder(parent, adapter.getItemViewType(i));
            adapter.bindViewHolder(viewHolder, i);
        }
    }

    private final AppDetailsRecyclerViewAdapter.AppDetailsRecyclerViewAdapterCallbacks dummyCallbacks = new AppDetailsRecyclerViewAdapter.AppDetailsRecyclerViewAdapterCallbacks() { // NOCHECKSTYLE LineLength
        @Override
        public boolean isAppDownloading() {
            return false;
        }

        @Override
        public void enableAndroidBeam() {

        }

        @Override
        public void disableAndroidBeam() {

        }

        @Override
        public void openUrl(String url) {

        }

        @Override
        public void installApk() {

        }

        @Override
        public void installApk(Apk apk) {

        }

        @Override
        public void uninstallApk() {

        }

        @Override
        public void installCancel() {

        }

        @Override
        public void launchApk() {

        }
    };

}

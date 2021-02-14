package com.team420.kekstore.updater;

import android.content.ContentValues;

import com.team420.kekstore.IndexUpdater;
import com.team420.kekstore.data.App;
import com.team420.kekstore.data.AppProvider;
import com.team420.kekstore.data.Repo;
import com.team420.kekstore.data.RepoProvider;
import com.team420.kekstore.data.Schema;
import org.hamcrest.text.MatchesPattern;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertThat;

/**
 * Check whether app icons are loaded from the correct repository. The repository with the
 * highest priority should be where we decide to load icons from.
 */
@RunWith(RobolectricTestRunner.class)
@SuppressWarnings("LineLength")
public class AppIconsTest extends MultiIndexUpdaterTest {

    private static final int HIGH_PRIORITY = 2;
    private static final int LOW_PRIORITY = 1;

    @Before
    public void setupMainAndArchiveRepo() {
        createRepo(REPO_MAIN, REPO_MAIN_URI, context);
        createRepo(REPO_ARCHIVE, REPO_ARCHIVE_URI, context);
    }

    @Test
    public void mainRepo() throws IndexUpdater.UpdateException {
        setRepoPriority(REPO_MAIN_URI, HIGH_PRIORITY);
        setRepoPriority(REPO_ARCHIVE_URI, LOW_PRIORITY);

        updateMain();
        updateArchive();

        assertIconUrl("^https://f-droid\\.org/repo/icons-[0-9]{3}/org\\.adaway\\.54\\.png$");
    }

    @Test
    public void archiveRepo() throws IndexUpdater.UpdateException {
        setRepoPriority(REPO_MAIN_URI, LOW_PRIORITY);
        setRepoPriority(REPO_ARCHIVE_URI, HIGH_PRIORITY);

        updateMain();
        updateArchive();

        assertIconUrl("^https://f-droid\\.org/archive/icons-[0-9]{3}/org\\.adaway\\.54.png$");
    }

    private void setRepoPriority(String repoUri, int priority) {
        ContentValues values = new ContentValues(1);
        values.put(Schema.RepoTable.Cols.PRIORITY, priority);

        Repo repo = RepoProvider.Helper.findByAddress(context, repoUri);
        RepoProvider.Helper.update(context, repo, values);
    }

    private void assertIconUrl(String expectedUrl) {
        App app = AppProvider.Helper.findHighestPriorityMetadata(context.getContentResolver(),
                "org.adaway", new String[]{
                        Schema.AppMetadataTable.Cols.ICON_URL,
                        Schema.AppMetadataTable.Cols.ICON,
                        Schema.AppMetadataTable.Cols.REPO_ID,
                });
        assertThat(app.getIconUrl(context), MatchesPattern.matchesPattern(expectedUrl));
    }
}

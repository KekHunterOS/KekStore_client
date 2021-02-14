/*
 * Copyright (C) 2016 Blue Jay Wireless
 * Copyright (C) 2014-2016 Hans-Christoph Steiner <hans@eds.org>
 * Copyright (C) 2014-2016 Peter Serwylo <peter@serwylo.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */

package com.team420.kekstore.data;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import androidx.annotation.Nullable;

import com.team420.kekstore.R;
import com.team420.kekstore.Utils;
import com.team420.kekstore.data.Schema.RepoTable;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@Config(application = Application.class)
@RunWith(RobolectricTestRunner.class)
public class RepoProviderTest extends FDroidProviderTest {

    private static final String[] COLS = RepoTable.Cols.ALL;

    /**
     * Returns the number of repos in app/src/main/res/default_repo.xml
     */
    public static int getDefaultRepoCount(Context context) {
        int itemCount = context.getResources().getStringArray(R.array.default_repos).length;
        return itemCount / DBHelper.REPO_XML_ITEM_COUNT;
    }

    /**
     * Set to random time zone to make sure that the dates are properly parsed.
     */
    @BeforeClass
    public static void setRandomTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(String.format("GMT-%d:%02d",
                System.currentTimeMillis() % 12, System.currentTimeMillis() % 60)));
        System.out.println("TIME ZONE for this test: " + TimeZone.getDefault());
    }

    @Test
    public void countEnabledRepos() {

        // By default, f-droid is enabled.
        assertEquals(1, RepoProvider.Helper.countEnabledRepos(context));

        Repo gpRepo = RepoProvider.Helper.findByAddress(context, "https://guardianproject.info/fdroid/repo");
        gpRepo = setEnabled(gpRepo, true);
        assertEquals(2, RepoProvider.Helper.countEnabledRepos(context));

        Repo fdroidRepo = RepoProvider.Helper.findByAddress(context, "https://f-droid.org/repo");
        setEnabled(fdroidRepo, false);
        setEnabled(gpRepo, false);

        assertEquals(0, RepoProvider.Helper.countEnabledRepos(context));
    }

    @Test
    public void lastUpdated() {
        assertNull(RepoProvider.Helper.lastUpdate(context));

        Repo gpRepo = RepoProvider.Helper.findByAddress(context, "https://guardianproject.info/fdroid/repo");

        // Set date to 2017-04-05 11:56:38
        setLastUpdate(gpRepo, new Date(1491357408643L));

        // GP is not yet enabled, so it is not counted.
        assertNull(RepoProvider.Helper.lastUpdate(context));

        // Set date to 2017-04-04 11:56:38
        Repo fdroidRepo = RepoProvider.Helper.findByAddress(context, "https://f-droid.org/repo");
        setLastUpdate(fdroidRepo, new Date(1491357408643L - (1000 * 60 * 60 * 24)));
        assertEquals("2017-04-04", Utils.formatDate(RepoProvider.Helper.lastUpdate(context), null));

        setEnabled(gpRepo, true);
        assertEquals("2017-04-05", Utils.formatDate(RepoProvider.Helper.lastUpdate(context), null));
    }

    private Repo setLastUpdate(Repo repo, Date date) {
        ContentValues values = new ContentValues(1);
        values.put(RepoTable.Cols.LAST_UPDATED, Utils.formatTime(date, null));
        RepoProvider.Helper.update(context, repo, values);
        return RepoProvider.Helper.findByAddress(context, repo.address);
    }

    @Test
    public void findByUrl() {

        Repo fdroidRepo = RepoProvider.Helper.findByAddress(context, "https://f-droid.org/repo");
        Repo fdroidArchiveRepo = RepoProvider.Helper.findByAddress(context, "https://f-droid.org/archive");

        String[] noRepos = {
                "https://not-a-repo.example.com",
                "https://f-droid.org",
                "https://f-droid.org/",
        };

        for (String url : noRepos) {
            assertNull(RepoProvider.Helper.findByUrl(context, Uri.parse(url), COLS));
        }

        String[] fdroidRepoUrls = {
                "https://f-droid.org/repo/index.jar",
                "https://f-droid.org/repo/index.jar?random-junk-in-query=yes",
                "https://f-droid.org/repo/index.jar?random-junk-in-query=yes&more-junk",
                "https://f-droid.org/repo/icons/com.team420.kekstore.100.png",
                "https://f-droid.org/repo/icons-640/com.team420.kekstore.100.png",
        };

        assertUrlsBelongToRepo(fdroidRepoUrls, fdroidRepo);

        String[] fdroidArchiveUrls = {
                "https://f-droid.org/archive/index.jar",
                "https://f-droid.org/archive/index.jar?random-junk-in-query=yes",
                "https://f-droid.org/archive/index.jar?random-junk-in-query=yes&more-junk",
                "https://f-droid.org/archive/icons/com.team420.kekstore.100.png",
                "https://f-droid.org/archive/icons-640/com.team420.kekstore.100.png",
        };

        assertUrlsBelongToRepo(fdroidArchiveUrls, fdroidArchiveRepo);
    }

    private void assertUrlsBelongToRepo(String[] urls, Repo expectedRepo) {
        for (String url : urls) {
            Repo actualRepo = RepoProvider.Helper.findByUrl(context, Uri.parse(url), COLS);
            assertNotNull("No repo matching URL " + url, actualRepo);
            assertEquals("Invalid repo for URL [" + url + "]. Expected [" + expectedRepo.address + "] but got ["
                    + actualRepo.address + "]", expectedRepo.id, actualRepo.id);
        }

    }

    /**
     * The {@link DBHelper} class populates the default repos when it first creates a database.
     * The names/URLs/signing certificates for these repos are all hard coded in the source/res.
     */
    @Test
    public void defaultRepos() {
        List<Repo> defaultRepos = RepoProvider.Helper.all(context);
        assertEquals(defaultRepos.size(), getDefaultRepoCount(context));

        String[] reposFromXml = context.getResources().getStringArray(R.array.default_repos);
        if (reposFromXml.length % DBHelper.REPO_XML_ITEM_COUNT != 0) {
            throw new IllegalArgumentException(
                    "default_repo.xml array does not have the right number of elements");
        }
        for (int i = 0; i < reposFromXml.length / DBHelper.REPO_XML_ITEM_COUNT; i++) {
            int offset = i * DBHelper.REPO_XML_ITEM_COUNT;
            assertRepo(
                    defaultRepos.get(i),
                    reposFromXml[offset + 1], // address
                    reposFromXml[offset + 2].replaceAll("\\s+", " "), // description
                    Utils.calcFingerprint(reposFromXml[offset + 7]), // pubkey
                    reposFromXml[offset]      // name
            );
        }
    }

    @Test
    public void canAddRepo() {

        int defaultRepoCount = getDefaultRepoCount(context);

        assertEquals(defaultRepoCount, RepoProvider.Helper.all(context).size());

        Repo mock1 = insertRepo(
                context,
                "https://mock-repo-1.example.com/fdroid/repo",
                "Just a made up repo",
                "ABCDEF1234567890",
                "Mock Repo 1"
        );

        Repo mock2 = insertRepo(
                context,
                "http://mock-repo-2.example.com/fdroid/repo",
                "Mock repo without a name",
                "0123456789ABCDEF"
        );

        assertEquals(defaultRepoCount + 2, RepoProvider.Helper.all(context).size());

        assertRepo(
                mock1,
                "https://mock-repo-1.example.com/fdroid/repo",
                "Just a made up repo",
                "ABCDEF1234567890",
                "Mock Repo 1"
        );

        assertRepo(
                mock2,
                "http://mock-repo-2.example.com/fdroid/repo",
                "Mock repo without a name",
                "0123456789ABCDEF",
                "mock-repo-2.example.com/fdroid/repo"
        );
    }

    private static void assertRepo(Repo actualRepo, String expectedAddress, String expectedDescription,
                                   String expectedFingerprint, String expectedName) {
        assertEquals(expectedAddress, actualRepo.address);
        assertEquals(expectedDescription, actualRepo.description);
        assertEquals(expectedFingerprint, actualRepo.fingerprint);
        assertEquals(expectedName, actualRepo.name);
    }

    @Test
    public void canDeleteRepo() {
        Repo mock1 = insertRepo(
                context,
                "https://mock-repo-1.example.com/fdroid/repo",
                "Just a made up repo",
                "ABCDEF1234567890",
                "Mock Repo 1"
        );

        Repo mock2 = insertRepo(
                context,
                "http://mock-repo-2.example.com/fdroid/repo",
                "Mock repo without a name",
                "0123456789ABCDEF"
        );

        int defaultRepoCount = getDefaultRepoCount(context);

        List<Repo> beforeDelete = RepoProvider.Helper.all(context);
        assertEquals(defaultRepoCount + 2, beforeDelete.size());
        assertEquals(mock1.id, beforeDelete.get(defaultRepoCount).id);
        assertEquals(mock2.id, beforeDelete.get(defaultRepoCount + 1).id);

        RepoProvider.Helper.remove(context, mock1.getId());

        List<Repo> afterDelete = RepoProvider.Helper.all(context);
        assertEquals(defaultRepoCount + 1, afterDelete.size());
        assertEquals(mock2.id, afterDelete.get(defaultRepoCount).id);
    }

    public Repo insertRepo(Context context, String address, String description, String fingerprint) {
        return insertRepo(context, address, description, fingerprint, null);
    }

    public static Repo insertRepo(Context context, String address, String description,
                                  String fingerprint, @Nullable String name) {
        return insertRepo(context, address, description, fingerprint, name, false);
    }

    public static Repo insertRepo(Context context, String address, String description,
                                  String fingerprint, @Nullable String name, boolean isSwap) {
        ContentValues values = new ContentValues();
        values.put(RepoTable.Cols.ADDRESS, address);
        values.put(RepoTable.Cols.DESCRIPTION, description);
        values.put(RepoTable.Cols.FINGERPRINT, fingerprint);
        values.put(RepoTable.Cols.NAME, name);
        values.put(RepoTable.Cols.IS_SWAP, isSwap);

        RepoProvider.Helper.insert(context, values);
        return RepoProvider.Helper.findByAddress(context, address);
    }
}

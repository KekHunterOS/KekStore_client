package com.team420.kekstore.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.ContextWrapper;

import androidx.test.core.app.ApplicationProvider;

import com.team420.kekstore.TestUtils;
import org.junit.After;
import org.junit.Before;

public abstract class FDroidProviderTest { // NOPMD This abstract class does not have any abstract methods

    protected ContentResolver contentResolver;
    protected ContextWrapper context;

    @Before
    public final void setupBase() {
        contentResolver = ApplicationProvider.getApplicationContext().getContentResolver();
        context = TestUtils.createContextWithContentResolver(contentResolver);
        TestUtils.registerContentProvider(AppProvider.getAuthority(), AppProvider.class);
    }

    @After
    public final void tearDownBase() {
        CategoryProvider.Helper.clearCategoryIdCache();
        DBHelper.clearDbHelperSingleton();
    }

    protected Repo setEnabled(Repo repo, boolean enabled) {
        ContentValues enable = new ContentValues(1);
        enable.put(Schema.RepoTable.Cols.IN_USE, enabled);
        RepoProvider.Helper.update(context, repo, enable);
        return RepoProvider.Helper.findByAddress(context, repo.address);
    }
}

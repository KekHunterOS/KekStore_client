package com.team420.kekstore.views.main;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.team420.kekstore.Preferences;
import com.team420.kekstore.R;
import com.team420.kekstore.UpdateService;
import com.team420.kekstore.Utils;
import com.team420.kekstore.data.CategoryProvider;
import com.team420.kekstore.data.Schema;
import com.team420.kekstore.views.apps.AppListActivity;
import com.team420.kekstore.views.categories.CategoryAdapter;
import com.team420.kekstore.views.categories.CategoryController;
import com.team420.kekstore.panic.HidingManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Responsible for ensuring that the categories view is inflated and then populated correctly.
 * Will start a loader to get the list of categories from the database and populate a recycler
 * view with relevant info about each.
 */
class CategoriesViewBinder implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 429820532;

    private final CategoryAdapter categoryAdapter;
    private final AppCompatActivity activity;
    private final TextView emptyState;
    private final RecyclerView categoriesList;

    CategoriesViewBinder(final AppCompatActivity activity, FrameLayout parent) {
        this.activity = activity;

        View categoriesView = activity.getLayoutInflater().inflate(R.layout.main_tab_categories, parent, true);

        categoryAdapter = new CategoryAdapter(activity, activity.getSupportLoaderManager());

        emptyState = (TextView) categoriesView.findViewById(R.id.empty_state);

        categoriesList = (RecyclerView) categoriesView.findViewById(R.id.category_list);
        categoriesList.setHasFixedSize(true);
        categoriesList.setLayoutManager(new LinearLayoutManager(activity));
        categoriesList.setAdapter(categoryAdapter);

        final SwipeRefreshLayout swipeToRefresh =
                (SwipeRefreshLayout) categoriesView.findViewById(R.id.swipe_to_refresh);
        Utils.applySwipeLayoutColors(swipeToRefresh);
        swipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeToRefresh.setRefreshing(false);
                UpdateService.updateNow(activity);
            }
        });

        FloatingActionButton searchFab = (FloatingActionButton) categoriesView.findViewById(R.id.fab_search);
        searchFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.startActivity(new Intent(activity, AppListActivity.class));
            }
        });
        searchFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (Preferences.get().hideOnLongPressSearch()) {
                    HidingManager.showHideDialog(activity);
                    return true;
                } else {
                    return false;
                }
            }
        });

        activity.getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id != LOADER_ID) {
            return null;
        }

        return new CursorLoader(
                activity,
                CategoryProvider.getAllCategories(),
                Schema.CategoryTable.Cols.ALL,
                null,
                null,
                null
        );
    }

    /**
     * Reads all categories from the cursor and stores them in memory to provide to the {@link CategoryAdapter}.
     *
     * It does this so it is easier to deal with localized/unlocalized categories without having
     * to store the localized version in the database. It is not expected that the list of categories
     * will grow so large as to make this a performance concern. If it does in the future, the
     * {@link CategoryAdapter} can be reverted to wrap the cursor again, and localized category
     * names can be stored in the database (allowing sorting in their localized form).
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() != LOADER_ID || cursor == null) {
            return;
        }

        List<String> categoryNames = new ArrayList<>(cursor.getCount());
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            categoryNames.add(cursor.getString(cursor.getColumnIndex(Schema.CategoryTable.Cols.NAME)));
            cursor.moveToNext();
        }

        Collections.sort(categoryNames, new Comparator<String>() {
            @Override
            public int compare(String categoryOne, String categoryTwo) {
                String localizedCategoryOne = CategoryController.translateCategory(activity, categoryOne);
                String localizedCategoryTwo = CategoryController.translateCategory(activity, categoryTwo);
                return localizedCategoryOne.compareTo(localizedCategoryTwo);
            }
        });

        categoryAdapter.setCategories(categoryNames);

        if (categoryAdapter.getItemCount() == 0) {
            emptyState.setVisibility(View.VISIBLE);
            categoriesList.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            categoriesList.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() != LOADER_ID) {
            return;
        }

        categoryAdapter.setCategories(Collections.<String>emptyList());
    }

}

package com.team420.kekstore.views.updates.items;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Used as a common base class for all data types in the {@link
 * com.team420.kekstore.views.updates.UpdatesAdapter}. Doesn't have any
 * functionality of its own, but allows the {@link
 * com.team420.kekstore.views.updates.UpdatesAdapter#delegatesManager}
 * to specify a data type more specific than just {@link Object}.
 */
public abstract class AppUpdateData { // NOPMD This abstract class does not have any abstract methods
    public final AppCompatActivity activity;

    public AppUpdateData(AppCompatActivity activity) {
        this.activity = activity;
    }
}

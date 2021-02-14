package com.team420.kekstore.views.main;

import android.annotation.TargetApi;
import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.team420.kekstore.R;
import com.team420.kekstore.views.PreferencesFragment;

@SuppressWarnings("LineLength")
/**
 * When attached to the window, the {@link PreferencesFragment} will be added. When detached from
 * the window, the fragment will be removed.
 * <p>
 * Based on code from https://github.com/lsjwzh/RecyclerViewPager/blob/master/lib/src/main/java/com/lsjwzh/widget/recyclerviewpager/FragmentStatePagerAdapter.java
 * licensed under the Apache 2.0 license (https://github.com/lsjwzh/RecyclerViewPager/blob/master/LICENSE).
 *
 * @see FragmentStatePagerAdapter Much of the code here was ported from this class.
 */
public class SettingsView extends FrameLayout {

    private FragmentTransaction currentTransaction;

    public SettingsView(Context context) {
        super(context);
        setId(R.id.preference_fragment_parent);
    }

    public SettingsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setId(R.id.preference_fragment_parent);
    }

    public SettingsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setId(R.id.preference_fragment_parent);
    }

    @TargetApi(21)
    public SettingsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setId(R.id.preference_fragment_parent);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        AppCompatActivity activity = (AppCompatActivity) getContext();
        if (activity == null) {
            throw new IllegalArgumentException("Cannot add a SettingsView to activities which are not an AppCompatActivity");
        }

        if (currentTransaction == null) {
            currentTransaction = activity.getSupportFragmentManager().beginTransaction();
        }

        currentTransaction.replace(getId(), new PreferencesFragment(), "preferences-fragment");
        currentTransaction.commitAllowingStateLoss();
        currentTransaction = null;
        activity.getSupportFragmentManager().executePendingTransactions();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        AppCompatActivity activity = (AppCompatActivity) getContext();
        if (activity == null) {
            throw new IllegalArgumentException("Cannot add a SettingsView to activities which are not an AppCompatActivity");
        }

        Fragment existingFragment = activity.getSupportFragmentManager().findFragmentByTag("preferences-fragment");
        if (existingFragment == null) {
            return;
        }

        if (currentTransaction == null) {
            currentTransaction = activity.getSupportFragmentManager().beginTransaction();
        }
        currentTransaction.remove(existingFragment);
        currentTransaction.commitAllowingStateLoss();
        currentTransaction = null;
        activity.getFragmentManager().executePendingTransactions();
    }

}

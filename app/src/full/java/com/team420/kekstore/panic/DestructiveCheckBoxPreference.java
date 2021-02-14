package com.team420.kekstore.panic;

import android.content.Context;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;
import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceViewHolder;

import com.team420.kekstore.FDroidApp;
import com.team420.kekstore.R;

public class DestructiveCheckBoxPreference extends CheckBoxPreference {
    public DestructiveCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DestructiveCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public DestructiveCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DestructiveCheckBoxPreference(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        if (!holder.itemView.isEnabled()) {
            return;
        }
        if (FDroidApp.isAppThemeLight()) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.panic_destructive_light));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.panic_destructive_dark));
        }
    }
}

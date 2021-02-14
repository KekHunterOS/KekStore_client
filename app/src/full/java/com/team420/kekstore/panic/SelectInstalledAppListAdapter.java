package com.team420.kekstore.panic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import com.team420.kekstore.Preferences;
import com.team420.kekstore.R;
import com.team420.kekstore.views.installed.InstalledAppListAdapter;
import com.team420.kekstore.views.installed.InstalledAppListItemController;

import java.util.Set;

public class SelectInstalledAppListAdapter extends InstalledAppListAdapter {
    private final Set<String> selectedApps;

    SelectInstalledAppListAdapter(AppCompatActivity activity) {
        super(activity);
        Preferences prefs = Preferences.get();
        selectedApps = prefs.getPanicWipeSet();
        prefs.setPanicTmpSelectedSet(selectedApps);
    }

    @NonNull
    @Override
    public InstalledAppListItemController onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = activity.getLayoutInflater().inflate(R.layout.installed_app_list_item, parent, false);
        return new SelectInstalledAppListItemController(activity, view, selectedApps);
    }
}

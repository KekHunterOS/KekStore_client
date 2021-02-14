package com.team420.kekstore.panic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import com.team420.kekstore.AppUpdateStatusManager;
import com.team420.kekstore.data.App;
import com.team420.kekstore.views.apps.AppListItemState;
import com.team420.kekstore.views.installed.InstalledAppListItemController;

import java.util.Set;

/**
 * Shows the currently installed apps as a selectable list.
 */
public class SelectInstalledAppListItemController extends InstalledAppListItemController {

    private final Set<String> selectedApps;

    public SelectInstalledAppListItemController(AppCompatActivity activity, View itemView, Set<String> selectedApps) {
        super(activity, itemView);
        this.selectedApps = selectedApps;
    }

    @NonNull
    @Override
    protected AppListItemState getCurrentViewState(
            @NonNull App app, @Nullable AppUpdateStatusManager.AppUpdateStatus appStatus) {
        return new AppListItemState(app).setCheckBoxStatus(selectedApps.contains(app.packageName));
    }

    @Override
    protected void onActionButtonPressed(App app) {
        super.onActionButtonPressed(app);
    }
}

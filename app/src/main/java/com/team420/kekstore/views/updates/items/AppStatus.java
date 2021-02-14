package com.team420.kekstore.views.updates.items;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate;
import com.team420.kekstore.AppUpdateStatusManager;
import com.team420.kekstore.R;
import com.team420.kekstore.data.App;

import java.util.List;

/**
 * Apps which we want to show some more substantial information about.
 *
 * @see R.layout#updateable_app_status_item The view that this binds to
 * @see AppStatusListItemController Used for binding the {@link App} to the
 * {@link R.layout#updateable_app_status_item}.
 */
public class AppStatus extends AppUpdateData {

    public final AppUpdateStatusManager.AppUpdateStatus status;

    public AppStatus(AppCompatActivity activity, AppUpdateStatusManager.AppUpdateStatus status) {
        super(activity);
        this.status = status;
    }

    public static class Delegate extends AdapterDelegate<List<AppUpdateData>> {

        private final AppCompatActivity activity;

        public Delegate(AppCompatActivity activity) {
            this.activity = activity;
        }

        @Override
        protected boolean isForViewType(@NonNull List<AppUpdateData> items, int position) {
            return items.get(position) instanceof AppStatus;
        }

        @NonNull
        @Override
        protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
            return new AppStatusListItemController(activity, activity.getLayoutInflater()
                    .inflate(R.layout.updateable_app_status_item, parent, false));
        }

        @Override
        protected void onBindViewHolder(@NonNull List<AppUpdateData> items, int position,
                                        @NonNull RecyclerView.ViewHolder holder, @NonNull List<Object> payloads) {
            AppStatus app = (AppStatus) items.get(position);
            ((AppStatusListItemController) holder).bindModel(app.status.app);
        }
    }

}

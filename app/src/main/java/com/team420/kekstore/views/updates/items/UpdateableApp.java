package com.team420.kekstore.views.updates.items;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate;
import com.team420.kekstore.R;
import com.team420.kekstore.data.App;

import java.util.List;

/**
 * List of all apps which can be updated, but have not yet been downloaded.
 *
 * @see UpdateableApp The data that is bound to this view.
 * @see R.layout#updateable_app_list_item The view that this binds to.
 * @see UpdateableAppListItemController Used for binding the {@link App} to
 *      the {@link R.layout#updateable_app_list_item}
 */
public class UpdateableApp extends AppUpdateData {

    public final App app;

    public UpdateableApp(AppCompatActivity activity, App app) {
        super(activity);
        this.app = app;
    }

    public static class Delegate extends AdapterDelegate<List<AppUpdateData>> {

        private final AppCompatActivity activity;

        public Delegate(AppCompatActivity activity) {
            this.activity = activity;
        }

        @Override
        protected boolean isForViewType(@NonNull List<AppUpdateData> items, int position) {
            return items.get(position) instanceof UpdateableApp;
        }

        @NonNull
        @Override
        protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
            return new UpdateableAppListItemController(activity, activity.getLayoutInflater()
                    .inflate(R.layout.updateable_app_list_item, parent, false));
        }

        @Override
        protected void onBindViewHolder(@NonNull List<AppUpdateData> items, int position,
                                        @NonNull RecyclerView.ViewHolder holder, @NonNull List<Object> payloads) {
            UpdateableApp app = (UpdateableApp) items.get(position);
            ((UpdateableAppListItemController) holder).bindModel(app.app);
        }
    }

}

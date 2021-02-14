package com.team420.kekstore.views.apps;

import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;
import com.team420.kekstore.R;
import com.team420.kekstore.data.App;
import com.team420.kekstore.data.Schema;

class AppListAdapter extends RecyclerView.Adapter<StandardAppListItemController> {

    private Cursor cursor;
    private final AppCompatActivity activity;
    private final AppListItemDivider divider;

    AppListAdapter(AppCompatActivity activity) {
        this.activity = activity;
        divider = new AppListItemDivider(activity);
        setHasStableIds(true);
    }

    public void setAppCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StandardAppListItemController onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StandardAppListItemController(activity, activity.getLayoutInflater()
                .inflate(R.layout.app_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull StandardAppListItemController holder, int position) {
        cursor.moveToPosition(position);
        final App app = new App(cursor);
        holder.bindModel(app);
    }

    @Override
    public long getItemId(int position) {
        cursor.moveToPosition(position);
        return cursor.getLong(cursor.getColumnIndex(Schema.AppMetadataTable.Cols.ROW_ID));
    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        recyclerView.addItemDecoration(divider);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.removeItemDecoration(divider);
        super.onDetachedFromRecyclerView(recyclerView);
    }
}

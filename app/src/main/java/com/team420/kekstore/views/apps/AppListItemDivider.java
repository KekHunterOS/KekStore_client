package com.team420.kekstore.views.apps;

import android.content.Context;
import android.graphics.Rect;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.team420.kekstore.R;
import com.team420.kekstore.Utils;

/**
 * Draws a faint line between items, to be used with the {@link AppListItemDivider}.
 */
public class AppListItemDivider extends DividerItemDecoration {
    private final int itemSpacing;

    public AppListItemDivider(Context context) {
        super(context, DividerItemDecoration.VERTICAL);
        setDrawable(ContextCompat.getDrawable(context, R.drawable.app_list_item_divider));
        itemSpacing = Utils.dpToPx(8, context);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int position = parent.getChildAdapterPosition(view);
        if (position > 0) {
            outRect.bottom = itemSpacing;
        }
    }
}

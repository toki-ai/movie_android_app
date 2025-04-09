package com.example.presentation.util;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SpacingItemDecoration extends RecyclerView.ItemDecoration {
    private final int spacing;

    public SpacingItemDecoration(int spacing) {
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {

        if (parent.getChildAdapterPosition(view) != 0) {
            outRect.top = spacing;
        }

//        if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1) {
//            outRect.bottom = spacing;
//        }
    }
}

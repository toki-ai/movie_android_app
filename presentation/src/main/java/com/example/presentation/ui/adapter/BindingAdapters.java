package com.example.presentation.ui.adapter;

import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.example.presentation.R;
import com.squareup.picasso.Picasso;

public class BindingAdapters {

    @BindingAdapter("imageUrl")
    public static void loadImage(ImageView view, String url) {
        if (url != null && !url.isEmpty()) {
            Picasso.get()
                    .load(url)
                    .placeholder(R.drawable.img_slash_bg)
                    .error(R.drawable.img_slash_bg)
                    .into(view);
        } else {
            view.setImageResource(R.drawable.img_slash_bg);
        }
    }
}
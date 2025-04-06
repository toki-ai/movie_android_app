package com.example.presentation.ui.adapter;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;

import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;

import com.example.domain.entity.User;
import com.example.presentation.R;
import com.example.presentation.ui.viewmodel.UserViewModel;
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
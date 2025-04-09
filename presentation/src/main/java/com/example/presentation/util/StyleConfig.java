package com.example.presentation.util;

import android.content.Context;
import android.graphics.Color;
import android.widget.TextView;
import android.widget.Toast;

public class StyleConfig {
    public static void returnStyle(Context context, String content) {
        Toast toast = Toast.makeText(context, content, Toast.LENGTH_SHORT);
        TextView toastTextView = toast.getView().findViewById(android.R.id.message);
        toastTextView.setTextColor(Color.BLACK);
        toast.show();
    }
}

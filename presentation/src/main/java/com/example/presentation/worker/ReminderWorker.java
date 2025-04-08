package com.example.presentation.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.domain.entity.Reminder;
import com.example.presentation.R;
import com.example.presentation.di.MyApplication;
import com.example.presentation.ui.viewmodel.ReminderViewModel;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

public class ReminderWorker extends Worker {
    private static final String CHANNEL_ID = "movie_reminder_channel";
    @Inject
    ReminderViewModel reminderViewModel;

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        MyApplication.getAppComponent().inject(this);
    }

    @NonNull
    @Override
    public Result doWork() {

        int movieId = getInputData().getInt("movie_id", -1);
        Log.d("ReminderWorker", "doWork called with ID: " + movieId);
        if (movieId != -1) {
            Reminder reminder = reminderViewModel.getReminderByMovieId(movieId);

            if (reminder != null) {
                Log.d("ReminderWorker", "doWork called with ID: " + reminder.getPosterUrl());
                showNotification(reminder);
                reminderViewModel.removeReminder(reminder);
            }
        }
        return Result.success();
    }

    private void showNotification(Reminder reminder) {
        Log.d("HEHEH", "WJYYYYY");
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Movie Reminders", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        Bitmap posterBitmap = null;
        try {
            posterBitmap = Picasso.get()
                    .load(reminder.getPosterUrl())
                    .resize(100, 100)
                    .centerCrop()
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_film)
                .setLargeIcon(posterBitmap)
                .setContentTitle(reminder.getMovieTitle())
                .setContentText("Reminder at: " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(reminder.getReminderTime())))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        manager.notify(reminder.getMovieId(), builder.build());
    }
}

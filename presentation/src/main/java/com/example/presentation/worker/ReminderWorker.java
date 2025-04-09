package com.example.presentation.worker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
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

import io.reactivex.rxjava3.schedulers.Schedulers;

public class ReminderWorker extends Worker {
    private static final String CHANNEL_ID = "movie_reminder_channel";
    @Inject
    ReminderViewModel reminderViewModel;

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        MyApplication.getAppComponent().inject(this);
    }

    @SuppressLint("CheckResult")
    @NonNull
    @Override
    public Result doWork() {
        int movieId = getInputData().getInt("movie_id", -1);
        if (movieId != -1) {
            Reminder reminder = reminderViewModel.getReminderByMovieId(movieId);
            if (reminder != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "No notification permission", Toast.LENGTH_LONG).show();
                    return Result.failure();
                }
                showNotification(reminder);
                reminderViewModel.removeReminder(reminder)
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                () -> reminderViewModel.loadReminder(),
                                throwable -> Log.e("ReminderWorker", "Error removing reminder: " + throwable.getMessage())
                        );
            } else {
                Log.e("ReminderWorker", "Reminder not found for movieId: " + movieId);
            }
        }
        return Result.success();
    }

    private void showNotification(Reminder reminder) {
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

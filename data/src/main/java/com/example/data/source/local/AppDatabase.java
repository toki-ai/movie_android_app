package com.example.data.source.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.data.source.local.dao.FavoriteMovieDao;
import com.example.data.source.local.dao.ReminderDao;
import com.example.data.source.local.entity.FavoriteMovieEntity;
import com.example.data.source.local.entity.ReminderEntity;

@Database(entities = {FavoriteMovieEntity.class, ReminderEntity.class}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract FavoriteMovieDao favoriteDao();
    public abstract ReminderDao reminderDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "favorite_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

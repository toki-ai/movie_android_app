package com.example.data.source.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.data.source.local.entity.ReminderEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertReminder(ReminderEntity reminder);

    @Query("DELETE FROM reminder WHERE movieId = :movieId")
    Completable deleteReminderByMovieId(int movieId);

    @Query("SELECT * FROM reminder ORDER BY reminderTime ASC")
    Single<List<ReminderEntity>> getAllReminders();
}
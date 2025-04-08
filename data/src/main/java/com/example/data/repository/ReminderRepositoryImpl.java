package com.example.data.repository;

import android.util.Log;

import com.example.data.mapper.ReminderEntityToReminderMapper;
import com.example.data.source.local.dao.ReminderDao;
import com.example.data.source.local.entity.ReminderEntity;
import com.example.domain.entity.Reminder;
import com.example.domain.repository.ReminderRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class ReminderRepositoryImpl implements ReminderRepository {
    private final ReminderDao reminderDao;
    private final ReminderEntityToReminderMapper entityToReminderMapper;

    public ReminderRepositoryImpl(ReminderDao reminderDao) {
        this.reminderDao = reminderDao;
        this.entityToReminderMapper = new ReminderEntityToReminderMapper();
    }

    @Override
    public Completable addReminder(Reminder reminder) {
        ReminderEntity entity = new ReminderEntity(
                reminder.getMovieId(),
                reminder.getMovieTitle(),
                reminder.getPosterUrl(),
                reminder.getYear(),
                reminder.getRating(),
                reminder.getReminderTime());
        return reminderDao.insertReminder(entity);
    }

    @Override
    public Completable removeReminder(Reminder reminder) {
        return reminderDao.deleteReminderByMovieId(reminder.getMovieId());
    }

    @Override
    public Single<List<Reminder>> getAllReminders() {
        return reminderDao.getAllReminders()
                .map(entities -> {
                    List<Reminder> reminders = new ArrayList<>();
                    for (ReminderEntity entity : entities) {
                        reminders.add(entityToReminderMapper.map(entity));
                    }
                    return reminders;
                });
    }
}

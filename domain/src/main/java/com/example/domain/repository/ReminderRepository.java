package com.example.domain.repository;

import com.example.domain.entity.Reminder;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface ReminderRepository {
    Completable addReminder(Reminder reminder);
    Completable removeReminder(Reminder reminder);
    Single<List<Reminder>> getAllReminders();
}
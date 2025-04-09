package com.example.domain.usecase;

import android.util.Log;

import com.example.domain.entity.Reminder;
import com.example.domain.repository.ReminderRepository;

import io.reactivex.rxjava3.core.Completable;

public class RemoveReminderUseCase {
    private final ReminderRepository repository;

    public RemoveReminderUseCase(ReminderRepository repository) {
        this.repository = repository;
    }

    public Completable execute(Reminder reminder) {
        return repository.removeReminder(reminder);
    }
}
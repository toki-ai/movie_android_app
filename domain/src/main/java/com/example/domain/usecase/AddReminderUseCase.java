package com.example.domain.usecase;

import com.example.domain.entity.Reminder;
import com.example.domain.repository.ReminderRepository;

import io.reactivex.rxjava3.core.Completable;

public class AddReminderUseCase {
    private final ReminderRepository repository;

    public AddReminderUseCase(ReminderRepository repository) {
        this.repository = repository;
    }

    public Completable execute(Reminder reminder) {
        return repository.addReminder(reminder);
    }
}

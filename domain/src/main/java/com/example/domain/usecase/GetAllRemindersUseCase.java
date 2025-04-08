package com.example.domain.usecase;

import com.example.domain.entity.Reminder;
import com.example.domain.repository.ReminderRepository;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

public class GetAllRemindersUseCase {
    private final ReminderRepository repository;

    public GetAllRemindersUseCase(ReminderRepository repository) {
        this.repository = repository;
    }

    public Single<List<Reminder>> execute() {
        return repository.getAllReminders();
    }
}
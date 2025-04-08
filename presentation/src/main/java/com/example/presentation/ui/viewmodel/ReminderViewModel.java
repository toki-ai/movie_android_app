package com.example.presentation.ui.viewmodel;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.domain.entity.Reminder;
import com.example.domain.usecase.AddReminderUseCase;
import com.example.domain.usecase.GetAllRemindersUseCase;
import com.example.domain.usecase.RemoveReminderUseCase;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ReminderViewModel {
    private final AddReminderUseCase addReminderUseCase;
    private final RemoveReminderUseCase removeReminderUseCase;
    private final GetAllRemindersUseCase getAllRemindersUseCase;
    private final MutableLiveData<List<Reminder>> remindersLiveData = new MutableLiveData<>();

    @Inject
    public ReminderViewModel(
            AddReminderUseCase addReminderUseCase,
            RemoveReminderUseCase removeReminderUseCase,
            GetAllRemindersUseCase getAllRemindersUseCase) {
        this.addReminderUseCase = addReminderUseCase;
        this.removeReminderUseCase = removeReminderUseCase;
        this.getAllRemindersUseCase = getAllRemindersUseCase;
        loadReminder();
    }

    public Completable addReminder(Reminder reminder) {
        return addReminderUseCase.execute(reminder);
    }

    public Completable removeReminder(Reminder reminder) {
        Log.d("ReminderWorker", "hehehe");
        return removeReminderUseCase.execute(reminder);
    }

//    public Reminder getReminderByMovieId(int id){
//        return getAllRemindersUseCase.execute()
//                .blockingGet().stream()
//                .filter(r -> r.getMovieId() == id)
//                .findFirst().orElse(null);
//    }

    public Reminder getReminderByMovieId(int movieId) {
        return getAllReminders().getValue().stream().filter(
                r -> r.getMovieId() == movieId
        ).findFirst().orElse(null);
    }

    @SuppressLint("CheckResult")
    public void loadReminder() {
        getAllRemindersUseCase.execute()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        reminder -> remindersLiveData.setValue(reminder),
                        throwable -> {}
                );
    }

    public LiveData<List<Reminder>> getAllReminders() {
        return remindersLiveData;
    }
}
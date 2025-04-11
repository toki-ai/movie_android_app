package com.example.data.repository;

import androidx.lifecycle.LiveData;

import com.example.data.source.remote.firebase.FirebaseUserDataSource;
import com.example.domain.entity.User;
import com.example.domain.repository.UserRepository;

import io.reactivex.rxjava3.core.Single;

public class UserRepositoryImpl implements UserRepository {
    private final FirebaseUserDataSource dataSource;

    public UserRepositoryImpl(FirebaseUserDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public LiveData<User> getUser() {
        return dataSource.getUser();
    }

    @Override
    public Single<User> saveUser(User user) {
        return dataSource.saveUser(user);
    }
}


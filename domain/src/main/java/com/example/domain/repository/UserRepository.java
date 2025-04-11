package com.example.domain.repository;

import androidx.lifecycle.LiveData;

import com.example.domain.entity.User;

import io.reactivex.rxjava3.core.Single;

public interface UserRepository {
    LiveData<User> getUser();
    Single<User> saveUser(User user);
}
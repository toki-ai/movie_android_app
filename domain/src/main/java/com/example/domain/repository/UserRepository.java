package com.example.domain.repository;

import com.example.domain.entity.User;

import io.reactivex.rxjava3.core.Single;

public interface UserRepository {
    Single<User> getUser();
    Single<User> saveUser(User user);
}
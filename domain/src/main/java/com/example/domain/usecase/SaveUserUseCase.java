package com.example.domain.usecase;

import com.example.domain.entity.User;
import com.example.domain.repository.UserRepository;

import io.reactivex.rxjava3.core.Single;

public class SaveUserUseCase {
    private final UserRepository repository;

    public SaveUserUseCase(UserRepository repository) {
        this.repository = repository;
    }

    public Single<User> execute(User user) {
        return repository.saveUser(user);
    }
}
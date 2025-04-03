package com.example.domain.usecase;

import com.example.domain.entity.User;
import com.example.domain.repository.UserRepository;

import io.reactivex.rxjava3.core.Single;

public class GetUserUseCase {
    private final UserRepository repository;

    public GetUserUseCase(UserRepository repository) {
        this.repository = repository;
    }

    public Single<User> execute() {
        return repository.getUser();
    }
}
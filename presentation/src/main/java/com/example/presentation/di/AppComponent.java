package com.example.presentation.di;

import com.example.domain.repository.UserRepository;
import com.example.domain.usecase.GetUserUseCase;
import com.example.domain.usecase.SaveUserUseCase;
import com.example.presentation.MainActivity;
import com.example.presentation.MyApplication;
import com.example.presentation.ui.fragment.AboutFragment;
import com.example.presentation.ui.fragment.FavoriteFragment;
import com.example.presentation.ui.fragment.ListMoviesFragment;
import com.example.presentation.ui.fragment.SettingFragment;

import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {
    void inject(MyApplication application);
    void inject(MainActivity activity);
    void inject(ListMoviesFragment fragment);
    void inject(FavoriteFragment fragment);
    void inject(SettingFragment fragment);
    void inject(AboutFragment fragment);
    UserRepository userRepository();
    GetUserUseCase getUserUseCase();
    SaveUserUseCase saveUserUseCase();
}
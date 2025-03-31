package com.example.presentation;

import android.app.Application;
import com.example.presentation.di.AppComponent;
import com.example.presentation.di.AppModule;
import com.example.presentation.di.DaggerAppComponent;

public class MyApplication extends Application {
    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
        appComponent.inject(this);
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }
}
package com.example.presentation.ui.model;

import androidx.databinding.ObservableField;

public class UserProfile {
    public final ObservableField<String> name = new ObservableField<>();
    public final ObservableField<String> birthday = new ObservableField<>();
    public final ObservableField<String> email = new ObservableField<>();
    public final ObservableField<Boolean> gender = new ObservableField<>();
    public final ObservableField<String> image = new ObservableField<>();

    public UserProfile() {
        name.set("");
        birthday.set("");
        email.set("");
        gender.set(true);
        image.set("https://example.com/default_image.jpg");
    }
}
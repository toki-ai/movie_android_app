package com.example.presentation.ui.viewmodel;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.domain.entity.User;
import com.example.domain.usecase.GetUserUseCase;
import com.example.domain.usecase.SaveUserUseCase;
import com.example.presentation.ui.model.UserProfile;

import javax.inject.Inject;

import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

public class UserViewModel extends ViewModel {
    private final GetUserUseCase getUserUseCase;
    private final SaveUserUseCase saveUserUseCase;

    private final MutableLiveData<String> nameLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> birthdayLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> emailLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> genderLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> imageLiveData = new MutableLiveData<>();

    private final MutableLiveData<Boolean> isEditModeLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    private final UserProfile userProfile = new UserProfile();

    @Inject
    public UserViewModel(GetUserUseCase getUserUseCase, SaveUserUseCase saveUserUseCase) {
        this.getUserUseCase = getUserUseCase;
        this.saveUserUseCase = saveUserUseCase;
    }

    @SuppressLint("CheckResult")
    private void loadUser() {
        getUserUseCase.execute()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        user -> {
                            nameLiveData.setValue(user.getName());
                            birthdayLiveData.setValue(user.getBirthday());
                            emailLiveData.setValue(user.getEmail());
                            genderLiveData.setValue(user.isGender());
                            imageLiveData.setValue(user.getImage());

                            userProfile.name.set(user.getName());
                            userProfile.birthday.set(user.getBirthday());
                            userProfile.email.set(user.getEmail());
                            userProfile.gender.set(user.isGender());
                            userProfile.image.set(user.getImage());
                        },
                        throwable -> {
                            User defaultUser = new User(
                                    "Default User",
                                    "https://example.com/default_image.jpg",
                                    true,
                                    "user@example.com",
                                    "01/01/2000"
                            );
                            saveUserUseCase.execute(defaultUser)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                            u -> {
                                                nameLiveData.setValue(defaultUser.getName());
                                                birthdayLiveData.setValue(defaultUser.getBirthday());
                                                emailLiveData.setValue(defaultUser.getEmail());
                                                genderLiveData.setValue(defaultUser.isGender());
                                                imageLiveData.setValue(defaultUser.getImage());

                                                userProfile.name.set(defaultUser.getName());
                                                userProfile.birthday.set(defaultUser.getBirthday());
                                                userProfile.email.set(defaultUser.getEmail());
                                                userProfile.gender.set(defaultUser.isGender());
                                                userProfile.image.set(defaultUser.getImage());
                                            },
                                            error -> errorMessageLiveData.setValue("Failed to save user: " + error.getMessage())
                                    );
                        }
                );
    }

    public LiveData<String> getNameLiveData() {
        return nameLiveData;
    }

    public LiveData<String> getBirthdayLiveData() {
        return birthdayLiveData;
    }

    public LiveData<String> getEmailLiveData() {
        return emailLiveData;
    }

    public LiveData<Boolean> getGenderLiveData() {
        return genderLiveData;
    }

    public LiveData<String> getImageLiveData() {
        return imageLiveData;
    }

    public LiveData<Boolean> getIsEditModeLiveData() {
        return isEditModeLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void toggleEditMode(boolean enable) {
        isEditModeLiveData.setValue(enable);
        if (!enable) {
            loadUser();
        }
    }

    @SuppressLint("CheckResult")
    public void saveProfile() {
        String name = userProfile.name.get();
        String email = userProfile.email.get();
        String birthday = userProfile.birthday.get() != null ? userProfile.birthday.get() : "";
        Boolean gender = userProfile.gender.get() != null ? userProfile.gender.get() : true;
        String image = userProfile.image.get() != null ? userProfile.image.get() : "https://example.com/default_image.jpg";

        if (name == null || name.isEmpty() || email == null || email.isEmpty()) {
            errorMessageLiveData.setValue("Please fill username and email");
            return;
        }

        User updatedUser = new User(name, image, gender, email, birthday);
        saveUserUseCase.execute(updatedUser)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> {
                    Log.d("UserViewModel", "Starting profile update");
                    errorMessageLiveData.setValue(null);
                })
                .subscribe(
                        u -> {
                            Log.d("UserViewModel", "Profile update successful");
                            isEditModeLiveData.setValue(false);
                            nameLiveData.setValue(updatedUser.getName());
                            birthdayLiveData.setValue(updatedUser.getBirthday());
                            emailLiveData.setValue(updatedUser.getEmail());
                            genderLiveData.setValue(updatedUser.isGender());
                            imageLiveData.setValue(updatedUser.getImage());
                        },
                        error -> {
                            String errorMsg = "Failed to update profile";
                            if (error != null) {
                                errorMsg += ": " + error.getClass().getSimpleName();
                                if (error.getMessage() != null) {
                                    errorMsg += " - " + error.getMessage();
                                }
                                Log.e("UserViewModel", "Profile update error", error);
                            }
                            errorMessageLiveData.setValue(errorMsg);
                        }
                );
    }
}
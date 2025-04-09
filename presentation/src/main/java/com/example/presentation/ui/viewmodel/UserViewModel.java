package com.example.presentation.ui.viewmodel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.domain.entity.User;
import com.example.domain.usecase.GetUserUseCase;
import com.example.domain.usecase.SaveUserUseCase;

import java.io.ByteArrayOutputStream;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

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

    private Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    private boolean isNetworkAvailable() {
        if (context == null) return false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Inject
    public UserViewModel(GetUserUseCase getUserUseCase, SaveUserUseCase saveUserUseCase) {
        this.getUserUseCase = getUserUseCase;
        this.saveUserUseCase = saveUserUseCase;
    }

    public String bitmapToBase64(Bitmap bitmap) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    private Bitmap base64ToBitmap(String base64String) {
        byte[] bytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @SuppressLint("CheckResult")
    public void loadUser() {
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
                                            },
                                            error -> errorMessageLiveData.setValue("Failed to save user: " + error.getMessage())
                                    );
                        }
                );
    }

    public void toggleEditMode(boolean enable) {
        isEditModeLiveData.setValue(enable);
        if (!enable) {
            loadUser();
        }
    }

    @SuppressLint("CheckResult")
    public void saveProfile(Bitmap profileImageBitmap) {
        String name = nameLiveData.getValue();
        String email = emailLiveData.getValue();
        String birthday = birthdayLiveData.getValue() != null ? birthdayLiveData.getValue() : "";
        Boolean gender = genderLiveData.getValue() != null ? genderLiveData.getValue() : true;
        String image = imageLiveData.getValue() != null ? imageLiveData.getValue() : "https://example.com/default_image.jpg";

        if (name == null || name.isEmpty() || email == null || email.isEmpty()) {
            errorMessageLiveData.setValue("Please fill username and email");
            return;
        }

        if (!isNetworkAvailable()) {
            Log.e("UserViewModel", "No network available");
            errorMessageLiveData.setValue("No network available");
            return;
        }

        if (profileImageBitmap != null) {
            image = bitmapToBase64(profileImageBitmap);
        }

        User updatedUser = new User(name, image, gender, email, birthday);
        saveToDatabase(updatedUser);
    }

    @SuppressLint("CheckResult")
    private void saveToDatabase(User updatedUser) {
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
                                errorMsg += ": " + error.getClass().getSimpleName() + " - " + error.getMessage();
                                Log.e("UserViewModel", "Profile update error", error);
                            }
                            errorMessageLiveData.setValue(errorMsg);
                        }
                );
    }

    public Bitmap getProfileImageBitmap() {
        String imageString = imageLiveData.getValue();
        if (imageString != null && !imageString.startsWith("http")) {
            try {
                return base64ToBitmap(imageString);
            } catch (Exception e) {
                Log.e("UserViewModel", "Failed to decode base64 image", e);
            }
        }
        return null;
    }

    // Getters for LiveData
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

    // Setters to update MutableLiveData
    public void setBirthday(String birthday) {
        birthdayLiveData.setValue(birthday);
    }

    public void setImage(String image) {
        imageLiveData.setValue(image);
    }

    public void setName(String name) {
        nameLiveData.setValue(name);
    }

    public void setEmail(String email) {
        emailLiveData.setValue(email);
    }

    public void setGender(boolean gender) {
        genderLiveData.setValue(gender);
    }
}
package com.example.presentation.ui.viewmodel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.domain.entity.User;
import com.example.domain.usecase.GetUserUseCase;
import com.example.domain.usecase.SaveUserUseCase;
import com.example.presentation.ui.model.UserProfile;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;

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

    public void toggleEditMode(boolean enable) {
        isEditModeLiveData.setValue(enable);
        if (!enable) {
            loadUser();
        }
    }

    @SuppressLint("CheckResult")
    public void saveProfile(Bitmap profileImageBitmap) {
        String name = userProfile.name.get();
        String email = userProfile.email.get();
        String birthday = userProfile.birthday.get() != null ? userProfile.birthday.get() : "";
        Boolean gender = userProfile.gender.get() != null ? userProfile.gender.get() : true;
        String image = userProfile.image.get() != null ? userProfile.image.get() : "https://example.com/default_image.jpg";

        if (name == null || name.isEmpty() || email == null || email.isEmpty()) {
            errorMessageLiveData.setValue("Please fill username and email");
            return;
        }

        if (!isNetworkAvailable()) {
            Log.e("UserViewModel", "No network available");
            errorMessageLiveData.setValue("No network available");
            return;
        }

        User updatedUser = new User(name, image, gender, email, birthday);


            if (profileImageBitmap != null) {
                String folderPath = "users/default_user";
                String fileName = "avatar_" + System.currentTimeMillis() + ".jpg";
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();

                // Create a reference to the file location
                StorageReference imageRef = storageRef.child(folderPath).child(fileName);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                profileImageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos); // Reduced quality for better performance
                byte[] imageData = baos.toByteArray();

                Log.d("UserViewModel", "Uploading image to: " + folderPath + "/" + fileName);

                // Upload with progress monitoring
                imageRef.putBytes(imageData)
                        .addOnProgressListener(taskSnapshot -> {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            Log.d("UserViewModel", "Upload progress: " + progress + "%");
                        })
                        .addOnSuccessListener(taskSnapshot -> {
                            Log.d("UserViewModel", "Upload successful");
                            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                Log.d("UserViewModel", "Download URL: " + uri.toString());
                                updatedUser.setImage(uri.toString());
                                saveToDatabase(updatedUser);
                            });
                        })
                        .addOnFailureListener(e -> {
                            Log.e("UserViewModel", "Upload failed: " + e.getMessage(), e);
                            // Fall back to existing image or default
                            saveToDatabase(updatedUser);
                            errorMessageLiveData.setValue("Failed to upload image: " + e.getMessage());
                        });
            } else {
                saveToDatabase(updatedUser);
            }
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
}
package com.example.data.source.remote.firebase;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.domain.entity.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.reactivex.rxjava3.core.Single;

public class FirebaseUserDataSource {
    private static final String DEFAULT_USER_ID = "default_user";
    private final DatabaseReference userRef;
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private ValueEventListener listener;

    public FirebaseUserDataSource() {
        userRef = FirebaseDatabase.getInstance().getReference("users").child(DEFAULT_USER_ID);
    }

    public LiveData<User> getUser() {
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        userLiveData.setValue(user);
                    }
                } else {
                    userLiveData.setValue(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        };
        userRef.addValueEventListener(listener);
        return userLiveData;
    }

    public Single<User> saveUser(User user) {
        return Single.create(emitter -> {
            Log.d("FirebaseUserDataSource", "Attempting to save user: " + user.getName());
            userRef.setValue(user)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("FirebaseUserDataSource", "User saved successfully");
                        emitter.onSuccess(user);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirebaseUserDataSource", "Failed to save user", e);
                        emitter.onError(e);
                    });
        });
    }

    public void removeListener() {
        if (listener != null) {
            userRef.removeEventListener(listener);
        }
    }
}
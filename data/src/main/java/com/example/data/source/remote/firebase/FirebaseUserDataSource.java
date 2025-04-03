package com.example.data.source.remote.firebase;

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

    public FirebaseUserDataSource() {
        userRef = FirebaseDatabase.getInstance().getReference("users").child(DEFAULT_USER_ID);
    }

    public Single<User> getUser() {
        return Single.create(emitter -> userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        emitter.onSuccess(user);
                    } else {
                        emitter.onError(new Exception("User data is null"));
                    }
                } else {
                    emitter.onError(new Exception("User not found"));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                emitter.onError(error.toException());
            }
        }));
    }

    public Single<User> saveUser(User user) {
        return Single.create(emitter -> userRef.setValue(user)
                .addOnSuccessListener(aVoid -> emitter.onSuccess(user))
                .addOnFailureListener(emitter::onError));
    }
}
package com.example.presentation.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Boolean> isGridLiveData = new MutableLiveData<>(false);

    public void toggleGridMode() {
        Boolean current = isGridLiveData.getValue();
        isGridLiveData.setValue(current == null || !current);
    }

    public LiveData<Boolean> getIsGridLiveData() {
        return isGridLiveData;
    }
}

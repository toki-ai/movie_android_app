package com.example.presentation.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.presentation.R;
import com.example.presentation.databinding.FragmentReminderBinding;
import com.example.presentation.di.MyApplication;
import com.example.presentation.ui.adapter.ReminderAdapter;
import com.example.presentation.ui.viewmodel.ReminderViewModel;
import com.example.presentation.util.SpacingItemDecoration;

import javax.inject.Inject;

public class ReminderFragment extends Fragment {
    private FragmentReminderBinding binding;
    @Inject
    ReminderViewModel reminderViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReminderBinding.inflate(inflater, container, false);
        MyApplication.getAppComponent().inject(this);
        binding.setViewModel(reminderViewModel);
        binding.setLifecycleOwner(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ReminderAdapter adapter = new ReminderAdapter(reminderViewModel, view, NavHostFragment.findNavController(this));

        binding.reminderRecyclerView.setAdapter(adapter);

        binding.reminderRecyclerView.addItemDecoration(
                new SpacingItemDecoration((int) getResources().getDimension(R.dimen.reminder_item_spacing))
        );

        reminderViewModel.getAllReminders().observe(getViewLifecycleOwner(), reminders -> {
            adapter.setReminders(reminders);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
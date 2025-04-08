package com.example.presentation.ui.fragment;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.work.Data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.domain.entity.Movie;
import com.example.domain.entity.Reminder;
import com.example.presentation.R;
import com.example.presentation.databinding.FragmentMovieDetailBinding;
import com.example.presentation.di.MyApplication;
import com.example.presentation.ui.adapter.CastCrewAdapter;
import com.example.presentation.ui.viewmodel.MovieViewModel;
import com.example.presentation.ui.viewmodel.ReminderViewModel;
import com.example.presentation.ui.viewmodel.SharedViewModel;
import com.example.presentation.worker.ReminderWorker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MovieDetailFragment extends Fragment {
    private FragmentMovieDetailBinding binding;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private CastCrewAdapter castCrewAdapter;
    private Reminder pendingReminder;

    @Inject
    MovieViewModel viewModel;

    @Inject
    ReminderViewModel reminderViewModel;

    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    setAlarmAndShowNotification();
                } else {
                    Toast.makeText(requireContext(),
                            "Notification permission is required for reminders",
                            Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApplication.getAppComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMovieDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        castCrewAdapter = new CastCrewAdapter();
        binding.detailCrewList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.detailCrewList.setAdapter(castCrewAdapter);
        binding.setViewModel(viewModel);
        Bundle args = getArguments();
        if (args != null) {
            int movieId = args.getInt("arg_movie_id", -1);
            String movieTitle = args.getString("arg_movie_title", "");

            disposables.add(
                    viewModel.getMovieDetail(movieId)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    movie -> {
                                        binding.setMovie(movie);
                                        Log.d("TAGTAG", String.valueOf(movie.getCredits().size()));
                                        castCrewAdapter.submitList(movie.getCredits());

                                        // Hiển thị Reminder nếu có
                                        Reminder reminder = reminderViewModel.getReminderByMovieId(movieId);
                                        if (reminder != null) {
                                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                            binding.detailReminderInfo.setText("Reminder at: " + dateFormat.format(reminder.getReminderTime()));
                                        } else {
                                            binding.detailReminderInfo.setText("No reminder set");
                                        }
                                    },
                                    throwable -> {
                                        Log.e("Movie Detail", "Error: " + throwable.getMessage());
                                        Toast.makeText(requireContext(), "Error loading movies: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                            )
            );

            viewModel.getFavoriteChangeLiveData().observe(getViewLifecycleOwner(), changedMovie -> {
                Movie currentMovie = binding.getMovie();
                if (currentMovie != null) {
                    currentMovie.setFavorite(changedMovie.isFavorite());
                    binding.setMovie(currentMovie);
                }
            });

            binding.detailBtnReminder.setOnClickListener(v -> showDateTimePicker(movieId));
        }
    }

    private void setAlarmAndShowNotification() {
        if (pendingReminder == null) return;
        Reminder existingReminder = reminderViewModel.getReminderByMovieId(pendingReminder.getMovieId());
        if (existingReminder != null) {
            existingReminder.setReminderTime(pendingReminder.getReminderTime());
            disposables.add(
                    reminderViewModel.addReminder(existingReminder)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(() -> {
                                Log.d("Reminder", "Updated existing reminder with movieId: " + existingReminder.getMovieId());
                                updateWorkManager(existingReminder);
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                binding.detailReminderInfo.setText("Reminder at: " + dateFormat.format(existingReminder.getReminderTime()));
                                reminderViewModel.loadReminder();
                            }, throwable -> {
                                Log.e("Reminder", "Error updating reminder: " + throwable.getMessage());
                            })
            );
        } else {
            disposables.add(
                    reminderViewModel.addReminder(pendingReminder)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(() -> {
                                Log.d("Reminder", "Added new reminder with movieId: " + pendingReminder.getMovieId());
                                scheduleReminder(pendingReminder);
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                binding.detailReminderInfo.setText("Reminder at: " + dateFormat.format(pendingReminder.getReminderTime()));
                                reminderViewModel.loadReminder();
                            }, throwable -> {
                                Log.e("Reminder", "Error adding reminder: " + throwable.getMessage());
                            })
            );
        }
    }

    private void showDateTimePicker(int movieId) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            new TimePickerDialog(requireContext(), (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                Movie movie = binding.getMovie();
                pendingReminder = new Reminder(
                        movieId,
                        movie.getTitle(),
                        movie.getPosterPathUrl(),
                        movie.getReleaseDate().substring(0, 4),
                        0f,
                        calendar.getTimeInMillis()
                );

                requestNotificationPermissionIfNeeded();

            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void scheduleReminder(Reminder reminder) {
        long delay = reminder.getReminderTime() - System.currentTimeMillis();
        if (delay > 0) {
            Data inputData = new Data.Builder()
                    .putInt("movie_id", reminder.getMovieId())
                    .build();

            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                    .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .build();

            WorkManager.getInstance(requireContext())
                    .enqueueUniqueWork("reminder_" + reminder.getMovieId(), ExistingWorkPolicy.KEEP, workRequest);
            Log.d("Reminder", "Scheduled new WorkManager for movieId: " + reminder.getMovieId() + " with delay: " + delay);
        } else {
            Log.e("Reminder", "Delay is negative or zero: " + delay);
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            setAlarmAndShowNotification();
        }
    }

    private void updateWorkManager(Reminder reminder) {
        long delay = reminder.getReminderTime() - System.currentTimeMillis();
        if (delay > 0) {
            Data inputData = new Data.Builder()
                    .putInt("movie_id", reminder.getMovieId())
                    .build();

            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                    .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .build();

            WorkManager workManager = WorkManager.getInstance(requireContext());
            workManager.cancelUniqueWork("reminder_" + reminder.getMovieId());
            workManager.enqueueUniqueWork("reminder_" + reminder.getMovieId(), ExistingWorkPolicy.REPLACE, workRequest);
            Log.d("Reminder", "Updated WorkManager for movieId: " + reminder.getMovieId() + " with delay: " + delay);
        } else {
            Log.e("Reminder", "Delay is negative or zero: " + delay);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
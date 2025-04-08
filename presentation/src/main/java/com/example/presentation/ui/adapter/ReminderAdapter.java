package com.example.presentation.ui.adapter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.example.domain.entity.Reminder;
import com.example.presentation.R;
import com.example.presentation.databinding.ItemReminderBinding;
import com.example.presentation.ui.viewmodel.ReminderViewModel;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {
    private List<Reminder> reminders = new ArrayList<>();
    private final ReminderViewModel viewModel;
    private final NavController navController;
    private final View navigationView;

    public ReminderAdapter(ReminderViewModel viewModel, View navigationView, NavController navController) {
        this.viewModel = viewModel;
        this.navigationView = navigationView;
        this.navController = navController;
    }

    public void setReminders(List<Reminder> reminders) {
        this.reminders = reminders != null ? reminders : new ArrayList<>();
        Log.d("ReminderAdapter", "Set " + this.reminders.size() + " reminders");
        notifyDataSetChanged();
    }

    @Override
    public ReminderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemReminderBinding binding = ItemReminderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ReminderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ReminderViewHolder holder, int position) {
        holder.bind(reminders.get(position));
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    class ReminderViewHolder extends RecyclerView.ViewHolder {
        private final ItemReminderBinding binding;

        ReminderViewHolder(ItemReminderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @SuppressLint("CheckResult")
        void bind(Reminder reminder) {
            binding.setReminder(reminder);
            binding.setViewModel(viewModel);
            String movieInfo = reminder.getMovieTitle() + " - " + reminder.getYear() + " - " + reminder.getRating();
            binding.movieInfoTextView.setText(movieInfo);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String reminderTime = "Reminder at: " + dateFormat.format(reminder.getReminderTime());
            binding.reminderTimeTextView.setText(reminderTime);

            Picasso.get()
                    .load(reminder.getPosterUrl())
                    .resize(70, 90)
                    .centerCrop()
                    .placeholder(R.drawable.img_slash_bg)
                    .error(R.drawable.img_slash_bg)
                    .into(binding.posterImageView);

            binding.getRoot().setOnClickListener(v -> {
                if (navController != null) {
                    Bundle args = new Bundle();
                    args.putInt("arg_movie_id", reminder.getMovieId());


                    navController.navigate(R.id.action_reminderFragment_to_movieDetailFragment, args);
                }
            });

        }
    }
}
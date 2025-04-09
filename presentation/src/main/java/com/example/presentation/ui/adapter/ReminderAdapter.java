package com.example.presentation.ui.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
    private int maxItems = Integer.MAX_VALUE;

    public ReminderAdapter(ReminderViewModel viewModel, View navigationView, NavController navController) {
        this.viewModel = viewModel;
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

    public void setMaxItems(int maxItems) {
        this.maxItems = maxItems;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return Math.min(reminders.size(), maxItems);
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
            binding.movieInfoTextView.setText(reminder.getMovieTitle());
            binding.movieSubInfoTextView.setText("Year: " + reminder.getYear() + ", Rating: " + reminder.getRating());

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

            if (maxItems == 2) {
                binding.deleteButton.setVisibility(View.GONE);
                binding.movieSubInfoTextView.setVisibility(View.GONE);
            } else {
                binding.deleteButton.setVisibility(View.VISIBLE);
                binding.movieSubInfoTextView.setVisibility(View.VISIBLE);

                binding.deleteButton.setOnClickListener(v -> {
                    new AlertDialog.Builder(binding.getRoot().getContext(), R.style.CustomAlertDialogTheme)
                            .setTitle("Delete Reminder")
                            .setMessage("Are you sure you want to delete this reminder?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                viewModel.removeReminderNe(reminder);
                            })
                            .setNegativeButton("No", (dialog, which) -> {
                                dialog.dismiss();
                            })
                            .setCancelable(true)
                            .show();
                });
            }

            binding.getRoot().setOnClickListener(v -> {
                if (navController != null) {
                    Bundle args = new Bundle();
                    args.putInt("arg_movie_id", reminder.getMovieId());
                    args.putString("arg_movie_title", reminder.getMovieTitle());

                    navController.navigate(R.id.action_reminderFragment_to_movieDetailFragment, args);
                }
            });

        }
    }
}
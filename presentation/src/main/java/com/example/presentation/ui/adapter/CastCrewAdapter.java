package com.example.presentation.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.domain.entity.CastCrew;
import com.example.presentation.databinding.ItemCastCrewBinding;

import java.util.ArrayList;
import java.util.List;

public class CastCrewAdapter extends RecyclerView.Adapter<CastCrewAdapter.CastCrewViewHolder> {
    private List<CastCrew> castCrewList = new ArrayList<>();

    @NonNull
    @Override
    public CastCrewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCastCrewBinding binding = ItemCastCrewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CastCrewViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CastCrewViewHolder holder, int position) {
        CastCrew castCrew = castCrewList.get(position);
        holder.bind(castCrew);
    }

    @Override
    public int getItemCount() {
        return castCrewList.size();
    }

    public void submitList(List<CastCrew> list) {
        castCrewList.clear();
        castCrewList.addAll(list);
        notifyDataSetChanged();
    }

    static class CastCrewViewHolder extends RecyclerView.ViewHolder {
        private final ItemCastCrewBinding binding;

        CastCrewViewHolder(ItemCastCrewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CastCrew castCrew) {
            binding.setCastCrew(castCrew);
            binding.executePendingBindings();
        }
    }
}
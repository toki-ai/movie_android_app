package com.example.presentation.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;

import com.example.domain.entity.Movie;
import com.example.presentation.R;
import com.example.presentation.databinding.ItemMovieGridBinding;
import com.example.presentation.databinding.ItemMovieListBinding;
import com.example.presentation.ui.viewmodel.MovieViewModel;

public class MovieAdapter extends PagingDataAdapter<Movie, MovieAdapter.MovieViewHolder> {
    private static final int VIEW_TYPE_LIST = 0;
    private static final int VIEW_TYPE_GRID = 1;

    private boolean isGridMode;
    private final MovieViewModel viewModel;

    public MovieAdapter(boolean isGridMode, MovieViewModel viewModel) {
        super(new DiffUtil.ItemCallback<Movie>() {
            @Override
            public boolean areItemsTheSame(@NonNull Movie oldItem, @NonNull Movie newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull Movie oldItem, @NonNull Movie newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.isGridMode = isGridMode;
        this.viewModel = viewModel;
    }

    public void toggleViewMode() {
        isGridMode = !isGridMode;
        notifyDataSetChanged();
    }

    public boolean isGridMode() {
        return isGridMode;
    }

    @Override
    public int getItemViewType(int position) {
        return isGridMode ? VIEW_TYPE_GRID : VIEW_TYPE_LIST;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_GRID) {
            ItemMovieGridBinding binding = DataBindingUtil.inflate(
                    inflater, R.layout.item_movie_grid, parent, false
            );
            return new MovieViewHolder(binding);
        } else {
            ItemMovieListBinding binding = DataBindingUtil.inflate(
                    inflater, R.layout.item_movie_list, parent, false
            );
            return new MovieViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = getItem(position);
        if (movie != null) {
            holder.bind(movie);
        }
    }

    class MovieViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        private ItemMovieGridBinding gridBinding;
        private ItemMovieListBinding listBinding;

        MovieViewHolder(ItemMovieGridBinding binding) {
            super(binding.getRoot());
            this.gridBinding = binding;
        }

        MovieViewHolder(ItemMovieListBinding binding) {
            super(binding.getRoot());
            this.listBinding = binding;
        }

        void bind(Movie movie) {
            if (gridBinding != null) {
                gridBinding.setMovie(movie);
                gridBinding.setViewModel(viewModel);
                gridBinding.executePendingBindings();
            } else if (listBinding != null) {
                listBinding.setMovie(movie);
                listBinding.setViewModel(viewModel);
                listBinding.executePendingBindings();
            }
        }
    }
}
package com.example.data.source.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.data.source.local.entity.FavoriteMovieEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

@Dao
public interface FavoriteMovieDao {
    @Query("SELECT * FROM favorites")
    Single<List<FavoriteMovieEntity>> getFavoriteMovies();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insertFavorite(FavoriteMovieEntity favorite);

    @Query("DELETE FROM favorites WHERE id = :movieId")
    Single<Integer> deleteFavorite(int movieId);
    @Query("SELECT * FROM favorites WHERE id = :movieId")
    Single<FavoriteMovieEntity> isFavorite(int movieId);
}
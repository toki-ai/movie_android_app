package com.example.domain.repository;


import com.example.domain.entity.CastCrew;
import java.util.List;
import io.reactivex.rxjava3.core.Single;

public interface CastCrewRepository {
    Single<List<CastCrew>> getCastCrews(int movieId);
}
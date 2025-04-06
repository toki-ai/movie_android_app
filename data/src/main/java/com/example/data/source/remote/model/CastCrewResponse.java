package com.example.data.source.remote.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CastCrewResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("cast")
    private List<CastCrewDto> cast;

    @SerializedName("crew")
    private List<CastCrewDto> crew;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<CastCrewDto> getCrew() {
        return crew;
    }

    public void setCrew(List<CastCrewDto> crew) {
        this.crew = crew;
    }

    public List<CastCrewDto> getCast() {
        return cast;
    }

    public void setCast(List<CastCrewDto> cast) {
        this.cast = cast;
    }
}

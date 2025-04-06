package com.example.data.source.remote.model;

import com.google.gson.annotations.SerializedName;

public class CastCrewDto {

    private int id;

    private String name;

    @SerializedName("profile_path")
    private String profilePath;

    public CastCrewDto() {
    }

    public CastCrewDto(int id, String name, String profilePath) {
        this.id = id;
        this.name = name;
        this.profilePath = profilePath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePath() {
        return profilePath;
    }

    public void setProfilePath(String profilePath) {
        this.profilePath = profilePath;
    }
}
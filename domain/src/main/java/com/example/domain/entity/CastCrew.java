package com.example.domain.entity;

import com.example.domain.utils.Constants;

public class CastCrew {

    private int id;

    private String name;

    private String profilePath;

    public CastCrew() {}

    public CastCrew(int id, String name, String profilePath) {
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

    public String getProfilePathUrl() {
        return Constants.IMAGE_BASE_URL + profilePath;
    }

    public void setProfilePath(String profilePath) {
        this.profilePath = profilePath;
    }
}
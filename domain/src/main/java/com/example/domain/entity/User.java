package com.example.domain.entity;

public class User {
    private String name;
    private String birthday;
    private String email;
    private boolean gender; //true: Male; false: Female
    private String image;

    public User(){}

    public User(String name, String image, boolean gender, String email, String birthday) {
        this.name = name;
        this.image = image;
        this.gender = gender;
        this.email = email;
        this.birthday = birthday;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {this.birthday = birthday;}
}

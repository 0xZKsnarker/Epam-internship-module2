package com.epam.domain;

public class Trainer extends User {

    private long userId;
    private String specialization;

    public Trainer(String firstName, String lastName, String username, String password, boolean isActive, String specialization, long userId) {
        super(firstName, lastName, username, password, isActive);
        this.specialization = specialization;
        this.userId = userId;
    }



    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    @Override
    public String toString() {
        return "Trainer{" +
                "userId=" + userId +
                ", specialization='" + specialization + '\'' +
                '}';
    }
}

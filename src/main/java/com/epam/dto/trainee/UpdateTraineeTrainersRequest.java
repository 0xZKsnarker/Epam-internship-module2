package com.epam.dto.trainee;

import java.util.List;

public class UpdateTraineeTrainersRequest {
    private String traineeUsername;
    private List<String> trainers;

    public String getTraineeUsername() {
        return traineeUsername;
    }

    public void setTraineeUsername(String traineeUsername) {
        this.traineeUsername = traineeUsername;
    }

    public List<String> getTrainers() {
        return trainers;
    }

    public void setTrainers(List<String> trainers) {
        this.trainers = trainers;
    }
}

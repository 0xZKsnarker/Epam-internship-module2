package com.epam.dto.trainee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class UpdateTraineeTrainersRequest {
    @NotBlank
    private String traineeUsername;
    @NotEmpty
    private List<String> trainers;

    public UpdateTraineeTrainersRequest(String traineeUsername, List<String> trainers) {
        this.traineeUsername = traineeUsername;
        this.trainers = trainers;
    }
    public UpdateTraineeTrainersRequest(){}

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

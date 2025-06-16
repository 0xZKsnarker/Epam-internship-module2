package com.epam.dto.training;

import java.time.LocalDate;

public class AddTrainingRequest {

    private String traineeUsername;
    private String trainerUsername;
    private String trainingName;
    private int durationOfTraining;
    private LocalDate trainingDate;


    public String getTraineeUsername() {
        return traineeUsername;
    }

    public void setTraineeUsername(String traineeUsername) {
        this.traineeUsername = traineeUsername;
    }

    public String getTrainerUsername() {
        return trainerUsername;
    }

    public void setTrainerUsername(String trainerUsername) {
        this.trainerUsername = trainerUsername;
    }

    public String getTrainingName() {
        return trainingName;
    }

    public void setTrainingName(String trainingName) {
        this.trainingName = trainingName;
    }

    public int getDurationOfTraining() {
        return durationOfTraining;
    }

    public void setDurationOfTraining(int durationOfTraining) {
        this.durationOfTraining = durationOfTraining;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public void setTrainingDate(LocalDate trainingDate) {
        this.trainingDate = trainingDate;
    }
}

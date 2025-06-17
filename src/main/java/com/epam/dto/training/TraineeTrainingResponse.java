package com.epam.dto.training;

import java.time.LocalDate;
import java.util.Objects;

public class TraineeTrainingResponse {
    private String trainingName;
    private LocalDate trainingDate;
    private String trainingType;
    private Integer trainingDuration;
    private String trainerName;

    public TraineeTrainingResponse() {
    }

    public TraineeTrainingResponse(String trainingName, LocalDate trainingDate, String trainingType, Integer trainingDuration, String trainerName) {
        this.trainingName = trainingName;
        this.trainingDate = trainingDate;
        this.trainingType = trainingType;
        this.trainingDuration = trainingDuration;
        this.trainerName = trainerName;
    }

    public String getTrainingName() {
        return trainingName;
    }

    public void setTrainingName(String trainingName) {
        this.trainingName = trainingName;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public void setTrainingDate(LocalDate trainingDate) {
        this.trainingDate = trainingDate;
    }

    public String getTrainingType() {
        return trainingType;
    }

    public void setTrainingType(String trainingType) {
        this.trainingType = trainingType;
    }

    public Integer getTrainingDuration() {
        return trainingDuration;
    }

    public void setTrainingDuration(Integer trainingDuration) {
        this.trainingDuration = trainingDuration;
    }

    public String getTrainerName() {
        return trainerName;
    }

    public void setTrainerName(String trainerName) {
        this.trainerName = trainerName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TraineeTrainingResponse that = (TraineeTrainingResponse) o;
        return Objects.equals(trainingName, that.trainingName) && Objects.equals(trainingDate, that.trainingDate) && Objects.equals(trainingType, that.trainingType) && Objects.equals(trainingDuration, that.trainingDuration) && Objects.equals(trainerName, that.trainerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trainingName, trainingDate, trainingType, trainingDuration, trainerName);
    }

    @Override
    public String toString() {
        return "TraineeTrainingResponse{" +
                "trainingName='" + trainingName + '\'' +
                ", trainingDate=" + trainingDate +
                ", trainingType='" + trainingType + '\'' +
                ", trainingDuration=" + trainingDuration +
                ", trainerName='" + trainerName + '\'' +
                '}';
    }
}

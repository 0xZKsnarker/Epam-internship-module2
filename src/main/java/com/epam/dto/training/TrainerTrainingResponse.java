package com.epam.dto.training;

import java.time.LocalDate;
import java.util.Objects;

public class TrainerTrainingResponse {
    private String trainingName;
    private LocalDate trainingDate;
    private String trainingType;
    private Integer trainingDuration;
    private String traineeName; // The key difference from the other DTO

    public TrainerTrainingResponse() {
    }

    public TrainerTrainingResponse(String trainingName, LocalDate trainingDate, String trainingType, Integer trainingDuration, String traineeName) {
        this.trainingName = trainingName;
        this.trainingDate = trainingDate;
        this.trainingType = trainingType;
        this.trainingDuration = trainingDuration;
        this.traineeName = traineeName;
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

    public String getTraineeName() {
        return traineeName;
    }

    public void setTraineeName(String traineeName) {
        this.traineeName = traineeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrainerTrainingResponse that = (TrainerTrainingResponse) o;
        return Objects.equals(trainingName, that.trainingName) && Objects.equals(trainingDate, that.trainingDate) && Objects.equals(trainingType, that.trainingType) && Objects.equals(trainingDuration, that.trainingDuration) && Objects.equals(traineeName, that.traineeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trainingName, trainingDate, trainingType, trainingDuration, traineeName);
    }

    @Override
    public String toString() {
        return "TrainerTrainingResponse{" +
                "trainingName='" + trainingName + '\'' +
                ", trainingDate=" + trainingDate +
                ", trainingType='" + trainingType + '\'' +
                ", trainingDuration=" + trainingDuration +
                ", traineeName='" + traineeName + '\'' +
                '}';
    }
}

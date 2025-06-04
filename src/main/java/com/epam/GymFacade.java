package com.epam;

import com.epam.service.TraineeService;
import com.epam.service.TrainerService;
import com.epam.service.TrainingService;
import org.springframework.stereotype.Component;

@Component
public class GymFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public GymFacade(TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService) {
        this.traineeService  = traineeService;
        this.trainerService  = trainerService;
        this.trainingService = trainingService;
    }

    public TraineeService  trainees()  {
        return traineeService;
    }
    public TrainerService  trainers()  {
        return trainerService;
    }
    public TrainingService trainings() {
        return trainingService;
    }
}

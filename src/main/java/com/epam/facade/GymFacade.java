package com.epam.facade;

import com.epam.service.TraineeService;
import com.epam.service.TrainerService;
import com.epam.service.TrainingService;
import com.epam.service.TrainingTypeService;
import org.springframework.stereotype.Component;

@Component
public class GymFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;

    public GymFacade(TraineeService traineeService, TrainerService trainerService, TrainingService trainingService, TrainingTypeService trainingTypeService) {
        this.traineeService  = traineeService;
        this.trainerService  = trainerService;
        this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
    }


    public TraineeService trainees()  {
        return traineeService;
    }
    public TrainerService trainers()  {
        return trainerService;
    }
    public TrainingService trainings() {
        return trainingService;
    }
    public TrainingTypeService trainingTypes() {
        return trainingTypeService;
    }
}

package com.epam.controller;


import com.epam.domain.Trainee;
import com.epam.domain.Trainer;
import com.epam.domain.Training;
import com.epam.domain.TrainingType;
import com.epam.dto.training.AddTrainingRequest;
import com.epam.dto.training.TrainingTypeResponse;
import com.epam.exception.ResourceNotFoundException;
import com.epam.facade.GymFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trainings")
public class TrainingController {


    private final GymFacade gymFacade;

    public TrainingController(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }

    @PostMapping("create")
    public ResponseEntity<Void>createNewTraining (@RequestParam AddTrainingRequest addTrainingRequest){
        Trainee trainee = gymFacade.trainees().findByUsername(addTrainingRequest.getTraineeUsername()).orElseThrow(() -> new ResourceNotFoundException("\"Trainee not found for username: " + addTrainingRequest.getTraineeUsername()));

        Trainer trainer = gymFacade.trainers().findByUsername(addTrainingRequest.getTrainerUsername())
            .orElseThrow(() -> new ResourceNotFoundException("Trainer not found for username: " + addTrainingRequest.getTrainerUsername()));

        TrainingType trainingType = trainer.getSpecialization();

        Training newTraining = new Training(trainee, trainer, trainingType, addTrainingRequest.getTrainingName(), addTrainingRequest.getTrainingDate(), addTrainingRequest.getDurationOfTraining());
        gymFacade.trainings().create(newTraining);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

@GetMapping("/types")
    public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes() {
        List<TrainingTypeResponse> trainingTypeResponses = gymFacade.trainingTypes().findAll()
                .stream()
                .map(trainingType -> new TrainingTypeResponse(trainingType.getId(), trainingType.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(trainingTypeResponses);
    }
}

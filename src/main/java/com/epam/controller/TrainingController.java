package com.epam.controller;


import com.epam.domain.Trainee;
import com.epam.domain.Trainer;
import com.epam.domain.Training;
import com.epam.domain.TrainingType;
import com.epam.dto.trainee.TrainerInfo;
import com.epam.dto.trainee.UpdateTraineeProfileRequest;
import com.epam.dto.trainee.UpdateTraineeTrainersRequest;
import com.epam.dto.training.AddTrainingRequest;
import com.epam.dto.training.TraineeTrainingResponse;
import com.epam.dto.training.TrainerTrainingResponse;
import com.epam.dto.training.TrainingTypeResponse;
import com.epam.exception.ResourceNotFoundException;
import com.epam.facade.GymFacade;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trainings")
public class TrainingController {


    private final GymFacade gymFacade;

    public TrainingController(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }

    @Operation(summary = "Add a new training session")
    @PostMapping("/create")
    @Valid
    public ResponseEntity<Void>createNewTraining (@RequestBody AddTrainingRequest addTrainingRequest){
        Trainee trainee = gymFacade.trainees().findByUsername(addTrainingRequest.getTraineeUsername()).orElseThrow(() -> new ResourceNotFoundException("\"Trainee not found for username: " + addTrainingRequest.getTraineeUsername()));

        Trainer trainer = gymFacade.trainers().findByUsername(addTrainingRequest.getTrainerUsername())
            .orElseThrow(() -> new ResourceNotFoundException("Trainer not found for username: " + addTrainingRequest.getTrainerUsername()));

        TrainingType trainingType = trainer.getSpecialization();

        Training newTraining = new Training(trainee, trainer, trainingType, addTrainingRequest.getTrainingName(), addTrainingRequest.getTrainingDate(), addTrainingRequest.getDurationOfTraining());
        gymFacade.trainings().create(newTraining);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Get all available training types")
    @GetMapping("/types")
    public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes() {
        List<TrainingTypeResponse> trainingTypeResponses = gymFacade.trainingTypes().findAll()
                .stream()
                .map(trainingType -> new TrainingTypeResponse(trainingType.getId(), trainingType.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(trainingTypeResponses);
    }

    @Operation(summary = "Get a trainee's trainings list with filtering")
    @GetMapping("/trainee/{username}")
    public ResponseEntity<List<TraineeTrainingResponse>> getTraineeTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType
    ) {
        List<Training> trainings = gymFacade.trainings().getTraineeTrainingsByCriteria(username, fromDate, toDate, trainerName, trainingType);

        List<TraineeTrainingResponse> response = trainings.stream()
                .map(training -> new TraineeTrainingResponse(
                        training.getTrainingName(),
                        training.getTrainingDate(),
                        training.getTrainingType().getName(),
                        training.getTrainingDuration(),
                        training.getTrainer().getUser().getFirstName() + " " + training.getTrainer().getUser().getLastName()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get a trainer's trainings list with filtering")
    @GetMapping("/trainer/{username}")
    public ResponseEntity<List<TrainerTrainingResponse>> getTrainerTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String traineeName
    ) {
        List<Training> trainings = gymFacade.trainings().getTrainerTrainingsByCriteria(username, fromDate, toDate, traineeName);

        List<TrainerTrainingResponse> response = trainings.stream()
                .map(training -> new TrainerTrainingResponse(
                        training.getTrainingName(),
                        training.getTrainingDate(),
                        training.getTrainingType().getName(),
                        training.getTrainingDuration(),
                        training.getTrainee().getUser().getFirstName() + " " + training.getTrainee().getUser().getLastName()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get trainers not assigned to a specific trainee")
    @GetMapping("/not-assigned")
    public ResponseEntity<List<TrainerInfo>> getNotAssignedTrainers(@RequestParam String username) {
        List<Trainer> trainers = gymFacade.trainers().getUnassignedTrainers(username);

        List<TrainerInfo> response = trainers.stream()
                .map(trainer -> new TrainerInfo(
                        trainer.getUser().getUsername(),
                        trainer.getUser().getFirstName(),
                        trainer.getUser().getLastName(),
                        trainer.getSpecialization().getName()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

}

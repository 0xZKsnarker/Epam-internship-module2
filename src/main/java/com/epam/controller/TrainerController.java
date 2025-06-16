package com.epam.controller;

import com.epam.domain.Trainer;
import com.epam.domain.TrainingType;
import com.epam.domain.User;
import com.epam.dto.auth.UserCredentialsResponse;
import com.epam.dto.trainer.TraineeInfo;
import com.epam.dto.trainer.TrainerProfileResponse;
import com.epam.dto.trainer.TrainerRegistrationRequest;
import com.epam.dto.trainer.UpdateTrainerProfileRequest;
import com.epam.dto.user.UpdateActivationStatusRequest;
import com.epam.exception.ResourceNotFoundException;
import com.epam.facade.GymFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;


@RequestMapping("api/trainers")
@RestController
public class TrainerController {

    private GymFacade gymFacade;

    TrainerController(GymFacade theGymFacade){
        this.gymFacade = theGymFacade;
    }

    @PostMapping("/register")
    public ResponseEntity<UserCredentialsResponse> registerTrainer(@RequestBody TrainerRegistrationRequest trainerRegistrationRequest){
        Trainer trainer = new Trainer();
        User user = new User();
        user.setFirstName(trainerRegistrationRequest.getFirstName());
        user.setLastName(trainerRegistrationRequest.getLastName());
        trainer.setUser(user);
        TrainingType specialization = gymFacade.trainingTypes().findById(trainerRegistrationRequest.getSpecializationId())
                .orElseThrow(() -> new ResourceNotFoundException("Specialization not found"));
        trainer.setSpecialization(specialization);

        Trainer newTrainer = gymFacade.trainers().create(trainer);

        UserCredentialsResponse userCredentialsResponse = new UserCredentialsResponse(
                newTrainer.getUser().getUsername(),
                newTrainer.getUser().getPassword()
        );

        return new ResponseEntity<>(userCredentialsResponse, HttpStatus.CREATED);
    }


    @GetMapping("/profile")
    public ResponseEntity<TrainerProfileResponse>getTrainer(@RequestParam String username){
        Trainer trainer = gymFacade.trainers().findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Trainer with username: " + username + " not found"));

        TrainerProfileResponse trainerProfileResponse = new TrainerProfileResponse();

        trainerProfileResponse.setFirstName(trainer.getUser().getFirstName());
        trainerProfileResponse.setLastName(trainer.getUser().getLastName());
        trainerProfileResponse.setActive(trainer.getUser().isActive());
        trainerProfileResponse.setSpecialization(trainer.getSpecialization().getName());
        if (trainer.getTrainees() != null) {
            trainerProfileResponse.setTrainees(trainer.getTrainees().stream().map(trainee -> {
                TraineeInfo traineeInfo = new TraineeInfo();
                traineeInfo.setUsername(trainee.getUser().getUsername());
                traineeInfo.setFirstName(trainee.getUser().getFirstName());
                traineeInfo.setLastName(trainee.getUser().getLastName());
                return traineeInfo;
            }).collect(Collectors.toList()));
        }

        return ResponseEntity.ok(trainerProfileResponse);
    }


    @PutMapping("/profile")
    public ResponseEntity<TrainerProfileResponse> updateTrainerProfile(@RequestBody UpdateTrainerProfileRequest updateTrainerProfileRequest) {

        Trainer trainerToUpdate = gymFacade.trainers().findByUsername(updateTrainerProfileRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Trainee with username '" + updateTrainerProfileRequest.getUsername() + "' not found."));

        User userToUpdate = trainerToUpdate.getUser();
        userToUpdate.setFirstName(updateTrainerProfileRequest.getFirstName());
        userToUpdate.setLastName(updateTrainerProfileRequest.getLastName());
        userToUpdate.setActive(updateTrainerProfileRequest.isActive());
        trainerToUpdate.setUser(userToUpdate);

        TrainingType newTrainingType = gymFacade.trainingTypes().findById(updateTrainerProfileRequest.getSpecializationId())
                .orElseThrow(() -> new ResourceNotFoundException("Specialization not found for this ID"));
        trainerToUpdate.setSpecialization(newTrainingType);

        Trainer updatedTrainer = gymFacade.trainers().update(trainerToUpdate);

        TrainerProfileResponse trainerProfileResponse = new TrainerProfileResponse();
        trainerProfileResponse.setFirstName(updatedTrainer.getUser().getFirstName());
        trainerProfileResponse.setLastName(updatedTrainer.getUser().getLastName());
        trainerProfileResponse.setActive(updatedTrainer.getUser().isActive());
        trainerProfileResponse.setSpecialization(updatedTrainer.getSpecialization().getName());

        if (updatedTrainer.getTrainees() != null) {
            trainerProfileResponse.setTrainees(updatedTrainer.getTrainees().stream().map(trainee -> {
                TraineeInfo traineeInfo = new TraineeInfo();
                traineeInfo.setUsername(trainee.getUser().getUsername());
                traineeInfo.setFirstName(trainee.getUser().getFirstName());
                traineeInfo.setLastName(trainee.getUser().getLastName());
                return traineeInfo;
            }).collect(Collectors.toList()));
        }

        return ResponseEntity.ok(trainerProfileResponse);
    }


    @PutMapping("/activation")
    public ResponseEntity<Void> activateDeactivateTrainee(@RequestBody UpdateActivationStatusRequest updateActivationStatusRequest) {
        gymFacade.trainers().activateTrainer(updateActivationStatusRequest.getUsername(), updateActivationStatusRequest.isActive());
        return ResponseEntity.ok().build();
    }


}

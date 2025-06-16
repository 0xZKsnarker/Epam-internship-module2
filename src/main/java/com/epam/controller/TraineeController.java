package com.epam.controller;

import com.epam.domain.Trainee;
import com.epam.domain.User;
import com.epam.dto.auth.UserCredentialsResponse;
import com.epam.dto.trainee.TraineeProfileResponse;
import com.epam.dto.trainee.TraineeRegistrationRequest;
import com.epam.dto.trainee.TrainerInfo;
import com.epam.dto.trainee.UpdateTraineeProfileRequest;
import com.epam.dto.user.UpdateActivationStatusRequest;
import com.epam.exception.ResourceNotFoundException;
import com.epam.facade.GymFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RequestMapping("api/trainees")
@RestController
public class TraineeController {

    private GymFacade gymFacade;

    TraineeController(GymFacade theGymFacade){
        this.gymFacade = theGymFacade;
    }

    @PostMapping("/register")
    public ResponseEntity<UserCredentialsResponse>registerTrainee(@RequestBody TraineeRegistrationRequest traineeRegistrationRequest){
        Trainee trainee = new Trainee();
        User user = new User();
        user.setFirstName(traineeRegistrationRequest.getFirstName());
        user.setLastName(traineeRegistrationRequest.getLastName());
        trainee.setUser(user);
        trainee.setAddress(traineeRegistrationRequest.getAddress());
        trainee.setDateOfBirth(traineeRegistrationRequest.getDateOfBirth());

        Trainee newTrainee = gymFacade.trainees().create(trainee);

        UserCredentialsResponse userCredentialsResponse = new UserCredentialsResponse(
                newTrainee.getUser().getUsername(),
                newTrainee.getUser().getPassword()
        );

        return new ResponseEntity<>(userCredentialsResponse, HttpStatus.CREATED);
    }


    @GetMapping("/profile")
    public ResponseEntity<TraineeProfileResponse>getTrainee(@RequestParam String username){
        Trainee trainee = gymFacade.trainees().findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Trainee with username: " + username + " not found"));

        TraineeProfileResponse traineeProfileResponse = new TraineeProfileResponse();

        traineeProfileResponse.setFirstName(trainee.getUser().getFirstName());
        traineeProfileResponse.setLastName(trainee.getUser().getLastName());
        traineeProfileResponse.setAddress(trainee.getAddress());
        traineeProfileResponse.setDateOfBirth(trainee.getDateOfBirth());
        traineeProfileResponse.setActive(trainee.getUser().isActive());

        if (traineeProfileResponse.getTrainers() != null){
            traineeProfileResponse.setTrainers(trainee.getTrainers().stream().map(trainer -> {

                TrainerInfo trainerInfo = new TrainerInfo();
                trainerInfo.setUsername(trainer.getUser().getUsername());
                trainerInfo.setFirstName(trainer.getUser().getFirstName());
                trainerInfo.setLastName(trainer.getUser().getLastName());
                trainerInfo.setSpecialization(trainer.getSpecialization().getName());
                return trainerInfo;

            }).collect(Collectors.toList()));
        }
        return ResponseEntity.ok(traineeProfileResponse);
    }


    @PutMapping("/profile")
    public ResponseEntity<TraineeProfileResponse> updateTraineeProfile(@RequestBody UpdateTraineeProfileRequest updateTraineeProfileRequest) {

        Trainee traineeToUpdate = gymFacade.trainees().findByUsername(updateTraineeProfileRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Trainee with username '" + updateTraineeProfileRequest.getUsername() + "' not found."));

        User userToUpdate = traineeToUpdate.getUser();
        userToUpdate.setFirstName(updateTraineeProfileRequest.getFirstName());
        userToUpdate.setLastName(updateTraineeProfileRequest.getLastName());
        userToUpdate.setActive(updateTraineeProfileRequest.isActive());

        traineeToUpdate.setAddress(updateTraineeProfileRequest.getAddress());
        traineeToUpdate.setDateOfBirth(updateTraineeProfileRequest.getDateOfBirth());
        traineeToUpdate.setUser(userToUpdate);
        Trainee updatedTrainee = gymFacade.trainees().update(traineeToUpdate);

        TraineeProfileResponse response = new TraineeProfileResponse();
        response.setFirstName(updatedTrainee.getUser().getFirstName());
        response.setLastName(updatedTrainee.getUser().getLastName());
        response.setDateOfBirth(updatedTrainee.getDateOfBirth());
        response.setAddress(updatedTrainee.getAddress());
        response.setActive(updatedTrainee.getUser().isActive());

        if (updatedTrainee.getTrainers() != null) {
            response.setTrainers(updatedTrainee.getTrainers().stream().map(trainer -> {
                TrainerInfo trainerInfo = new TrainerInfo();
                trainerInfo.setUsername(trainer.getUser().getUsername());
                trainerInfo.setFirstName(trainer.getUser().getFirstName());
                trainerInfo.setLastName(trainer.getUser().getLastName());
                trainerInfo.setSpecialization(trainer.getSpecialization().getName());
                return trainerInfo;
            }).collect(Collectors.toList()));
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/activation")
    public ResponseEntity<Void> activateDeactivateTrainee(@RequestBody UpdateActivationStatusRequest updateActivationStatusRequest) {
        gymFacade.trainees().activateTrainee(updateActivationStatusRequest.getUsername(), updateActivationStatusRequest.isActive());
        return ResponseEntity.ok().build();
    }
}

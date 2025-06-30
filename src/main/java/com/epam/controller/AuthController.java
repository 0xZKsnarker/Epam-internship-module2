package com.epam.controller;

import com.epam.dto.auth.ChangePasswordRequest;
import com.epam.facade.GymFacade;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {


    private final GymFacade gymFacade;
    private MeterRegistry meterRegistry;

    public AuthController(GymFacade gymFacade, MeterRegistry meterRegistry) {
        this.gymFacade = gymFacade;
        this.meterRegistry = meterRegistry;
    }

    @Operation(summary = "Login for Trainee or Trainer")
    @GetMapping("/login/{username}")
    public ResponseEntity<Void> login (@PathVariable String username,@Valid @RequestParam String password){
        boolean trainerlogin = gymFacade.trainers().checkCredentials(username, password);
        boolean traineelogin = gymFacade.trainees().checkCredentials(username, password);

        if(traineelogin || trainerlogin){
            meterRegistry.counter("auth.logins", "status", "success", "username", username).increment();
            return ResponseEntity.ok().build();
        }else{
            meterRegistry.counter("auth.logins", "status", "failure", "username", username).increment();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


    @Operation(summary = "Change password for Trainee or Trainer")
    @Valid
    @PutMapping("/{username}/password")
    public ResponseEntity<Void>changePassword(@PathVariable String username, @Valid @RequestBody ChangePasswordRequest changePasswordRequest){
        boolean traineeLoginIsValid = gymFacade.trainees().checkCredentials(username, changePasswordRequest.getOldPass());
        boolean trainerLoginIsValid = gymFacade.trainers().checkCredentials(username, changePasswordRequest.getOldPass());

        if (traineeLoginIsValid){
            gymFacade.trainees().changePassword(username, changePasswordRequest.getNewPass());
            meterRegistry.counter("auth.password.changes", "username", username).increment(); // <-- ADD THIS
            return ResponseEntity.ok().build();
        }else if (trainerLoginIsValid){
            gymFacade.trainers().changePassword(username, changePasswordRequest.getNewPass());
            meterRegistry.counter("auth.password.changes", "username", username).increment(); // <-- ADD THIS
            return ResponseEntity.ok().build();
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}

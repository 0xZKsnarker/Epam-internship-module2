package com.epam.controller;

import com.epam.dto.auth.ChangePasswordRequest;
import com.epam.facade.GymFacade;
import com.epam.utils.CredentialsService;
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
    private final CredentialsService credentialsService;
    private MeterRegistry meterRegistry;

    public AuthController(GymFacade gymFacade, CredentialsService credentialsService, MeterRegistry meterRegistry) {
        this.gymFacade = gymFacade;
        this.credentialsService = credentialsService;
        this.meterRegistry = meterRegistry;
    }

    @Operation(summary = "Login for Trainee or Trainer")
    @GetMapping("/login/{username}")
    public ResponseEntity<Void> login (@PathVariable String username,@Valid @RequestParam String password){
        boolean trainerlogin = gymFacade.trainers().findByUsername(username)
                .map(trainer -> credentialsService.checkCredentials(trainer.getUser(), password))
                .orElse(false);

        boolean traineelogin = gymFacade.trainees().findByUsername(username)
                .map(trainee -> credentialsService.checkCredentials(trainee.getUser(), password))
                .orElse(false);

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
        var traineeOpt = gymFacade.trainees().findByUsername(username);
        if (traineeOpt.isPresent() && credentialsService.checkCredentials(traineeOpt.get().getUser(), changePasswordRequest.getOldPass())) {
            gymFacade.trainees().changePassword(username, changePasswordRequest.getNewPass());
            meterRegistry.counter("auth.password.changes", "username", username).increment();
            return ResponseEntity.ok().build();
        }

        var trainerOpt = gymFacade.trainers().findByUsername(username);
        if (trainerOpt.isPresent() && credentialsService.checkCredentials(trainerOpt.get().getUser(), changePasswordRequest.getOldPass())) {
            gymFacade.trainers().changePassword(username, changePasswordRequest.getNewPass());
            meterRegistry.counter("auth.password.changes", "username", username).increment();
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
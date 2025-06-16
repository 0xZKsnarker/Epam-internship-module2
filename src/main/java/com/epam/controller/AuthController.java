package com.epam.controller;

import com.epam.dto.auth.ChangePasswordRequest;
import com.epam.facade.GymFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {


    private final GymFacade gymFacade;

    public AuthController(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }

    @GetMapping("/login")
    public ResponseEntity<Void> login (@RequestParam String username, @RequestParam String password){
        boolean trainerlogin = gymFacade.trainers().checkCredentials(username, password);
        boolean traineelogin = gymFacade.trainees().checkCredentials(username, password);

        if(traineelogin || trainerlogin){
            return ResponseEntity.ok().build();
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PutMapping("/change-pass")
    public ResponseEntity<Void>changePassword(@RequestParam ChangePasswordRequest changePasswordRequest){
        boolean traineeLoginIsValid = gymFacade.trainees().checkCredentials(changePasswordRequest.getUsername(), changePasswordRequest.getOldPass());
        boolean trainerLoginIsValid = gymFacade.trainers().checkCredentials(changePasswordRequest.getUsername(), changePasswordRequest.getOldPass());

        if (traineeLoginIsValid){
            gymFacade.trainees().changePassword(changePasswordRequest.getUsername(), changePasswordRequest.getNewPass());
            return ResponseEntity.ok().build();
        }else if (trainerLoginIsValid){
            gymFacade.trainers().changePassword(changePasswordRequest.getUsername(), changePasswordRequest.getNewPass());
            return ResponseEntity.ok().build();
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}

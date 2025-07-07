package com.epam.controller;

import com.epam.dto.auth.ChangePasswordRequest;
import com.epam.dto.auth.LoginRequest;
import com.epam.security.Jwtutil;
import com.epam.security.LoginAttemptCounterService;
import com.epam.service.TraineeService;
import com.epam.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final Jwtutil jwtUtil;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final LoginAttemptCounterService loginAttemptService;

    public AuthController(AuthenticationManager authenticationManager, UserDetailsService userDetailsService, Jwtutil jwtUtil, TraineeService traineeService, TrainerService trainerService, LoginAttemptCounterService loginAttemptService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.loginAttemptService = loginAttemptService;
    }

    @Operation(summary = "Login for Trainee or Trainer to get a JWT")
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody @Valid LoginRequest loginRequest) {
        try {authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Incorrect username or password");
        }

        //If auth succeeds reset the login attempt counter
        loginAttemptService.loginSuccessful(loginRequest.getUsername());

        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(Map.of("jwt", jwt));
    }

    @Operation(summary = "Change password for an authenticated user")
    @PutMapping("/{username}/password")
    public ResponseEntity<Void> changePassword(@PathVariable String username, @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, changePasswordRequest.getOldPass())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).build();
        }

        if (traineeService.findByUsername(username).isPresent()) {
            traineeService.changePassword(username, changePasswordRequest.getNewPass());
        } else if (trainerService.findByUsername(username).isPresent()) {
            trainerService.changePassword(username, changePasswordRequest.getNewPass());
        } else {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }
}
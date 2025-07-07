package com.epam.controller;

import com.epam.dto.auth.ChangePasswordRequest;
import com.epam.dto.auth.LoginRequest;
import com.epam.security.Jwtutil;
import com.epam.security.LoginAttemptCounterService;
import com.epam.service.TraineeService;
import com.epam.service.TrainerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private UserDetailsService userDetailsService;
    @MockBean
    private Jwtutil jwtUtil;
    @MockBean
    private TraineeService traineeService;
    @MockBean
    private TrainerService trainerService;
    @MockBean
    private LoginAttemptCounterService loginAttemptService;

    @Test
    void login_withValidCredentials_shouldReturnJwt() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("user");
        loginRequest.setPassword("password");

        UserDetails mockUserDetails = new User("user", "encodedPassword", new ArrayList<>());

        when(userDetailsService.loadUserByUsername("user")).thenReturn(mockUserDetails);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("fake.jwt.token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").value("fake.jwt.token"));

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("user", "password")
        );
        verify(loginAttemptService).loginSuccessful("user");
    }

    @Test
    void login_withInvalidCredentials_shouldReturnUnauthorized() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("user");
        loginRequest.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid Credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());

        verify(loginAttemptService, never()).loginSuccessful(anyString());
    }

    @Test
    void changePassword_withValidOldPassword_shouldReturnOk() throws Exception {
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("user", "old_pass", "new_pass");

        when(traineeService.findByUsername("user")).thenReturn(Optional.of(new com.epam.domain.Trainee()));

        mockMvc.perform(put("/api/auth/{username}/password", "user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isOk());

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("user", "old_pass")
        );
        verify(traineeService).changePassword("user", "new_pass");
    }

    @Test
    void changePassword_withInvalidOldPassword_shouldReturnUnauthorized() throws Exception {
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("user", "wrong_old_pass", "new_pass");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid Credentials"));

        mockMvc.perform(put("/api/auth/{username}/password", "user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isUnauthorized());

        verify(traineeService, never()).changePassword(anyString(), anyString());
        verify(trainerService, never()).changePassword(anyString(), anyString());
    }
}
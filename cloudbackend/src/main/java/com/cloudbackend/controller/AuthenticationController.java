package com.cloudbackend.controller;

import com.cloudbackend.dto.AuthResponse;
import com.cloudbackend.dto.LoginRequest;
import com.cloudbackend.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        logger.info("New login {}", loginRequest.getUsername());
        return ResponseEntity.ok(authenticationService.login(loginRequest.getUsername(), loginRequest.getPassword()));
    }

}

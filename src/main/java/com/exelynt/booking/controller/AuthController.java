package com.exelynt.booking.controller;

import com.exelynt.booking.dto.request.LoginRequest;
import com.exelynt.booking.dto.request.RegisterRequest;
import com.exelynt.booking.dto.response.LoginResponse;
import com.exelynt.booking.dto.response.UserResponse;
import com.exelynt.booking.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Public self-registration. Always creates a USER account (see AuthService). */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /** Required endpoint: POST /auth/login — returns a JWT to use as a Bearer token. */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}

package com.fundoonotes.controller;

import com.fundoonotes.dto.request.LoginRequestDto;
import com.fundoonotes.dto.request.UserRegisterRequestDto;
import com.fundoonotes.dto.response.LoginResponseDto;
import com.fundoonotes.dto.response.UserResponseDto;
import com.fundoonotes.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user registration and authentication endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Register a new user.
     *
     * @param dto the registration request body
     * @return created user data with 201 status
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(
            @Valid @RequestBody UserRegisterRequestDto dto) {
        log.info("POST /api/users/register — email: {}", dto.getEmail());
        UserResponseDto response = userService.register(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Authenticate user and return JWT token.
     *
     * @param dto the login request body
     * @return login response with token and 200 status
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto dto) {
        log.info("POST /api/users/login — email: {}", dto.getEmail());
        LoginResponseDto response = userService.login(dto);
        return ResponseEntity.ok(response);
    }
}

package com.fundoonotes.controller;

import com.fundoonotes.dto.request.ForgotPasswordRequestDto;
import com.fundoonotes.dto.request.LoginRequestDto;
import com.fundoonotes.dto.request.ResetPasswordRequestDto;
import com.fundoonotes.dto.request.UserRegisterRequestDto;
import com.fundoonotes.dto.response.LoginResponseDto;
import com.fundoonotes.dto.response.MessageResponseDto;
import com.fundoonotes.dto.response.UserResponseDto;
import com.fundoonotes.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user registration, authentication, and security endpoints.
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

    /**
     * Initiate forgot-password flow.
     * Generates an OTP, stores it in Redis, and publishes a reset event.
     *
     * @param dto the forgot password request with email
     * @return success message with 200 status
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponseDto> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDto dto) {
        log.info("POST /api/users/forgot-password — email: {}", dto.getEmail());
        MessageResponseDto response = userService.forgotPassword(dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Reset password using OTP.
     * Validates OTP from Redis, updates password, and cleans up.
     *
     * @param dto the reset password request with email, OTP, and new password
     * @return success message with 200 status
     */
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponseDto> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto dto) {
        log.info("POST /api/users/reset-password — email: {}", dto.getEmail());
        MessageResponseDto response = userService.resetPassword(dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Logout user by blacklisting their JWT token.
     * The token is invalidated in Redis for its remaining TTL.
     *
     * @param token the JWT token from Authorization header
     * @return success message with 200 status
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponseDto> logout(
            @RequestHeader("Authorization") String token) {
        log.info("POST /api/users/logout");
        MessageResponseDto response = userService.logout(token);
        return ResponseEntity.ok(response);
    }
}

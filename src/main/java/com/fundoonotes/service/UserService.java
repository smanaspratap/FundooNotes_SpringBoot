package com.fundoonotes.service;

import com.fundoonotes.dto.request.ForgotPasswordRequestDto;
import com.fundoonotes.dto.request.LoginRequestDto;
import com.fundoonotes.dto.request.ResetPasswordRequestDto;
import com.fundoonotes.dto.request.UserRegisterRequestDto;
import com.fundoonotes.dto.response.LoginResponseDto;
import com.fundoonotes.dto.response.MessageResponseDto;
import com.fundoonotes.dto.response.UserResponseDto;

/**
 * Service interface for user-related operations.
 */
public interface UserService {

    /**
     * Register a new user.
     *
     * @param dto the registration request data
     * @return the created user's response data
     */
    UserResponseDto register(UserRegisterRequestDto dto);

    /**
     * Authenticate a user and generate a JWT token.
     *
     * @param dto the login request data
     * @return login response containing JWT token
     */
    LoginResponseDto login(LoginRequestDto dto);

    /**
     * Initiate forgot-password flow: generates OTP, stores in Redis,
     * publishes PasswordResetEvent for async notification.
     *
     * @param dto the forgot password request containing the user's email
     * @return success message
     */
    MessageResponseDto forgotPassword(ForgotPasswordRequestDto dto);

    /**
     * Reset user password using OTP validation.
     * Validates the OTP from Redis, encodes new password, saves, and cleans up.
     *
     * @param dto the reset password request with email, OTP, and new password
     * @return success message
     */
    MessageResponseDto resetPassword(ResetPasswordRequestDto dto);

    /**
     * Logout user by blacklisting their JWT token in Redis.
     * The token is blacklisted for its remaining TTL.
     *
     * @param token the JWT token to invalidate
     * @return success message
     */
    MessageResponseDto logout(String token);
}

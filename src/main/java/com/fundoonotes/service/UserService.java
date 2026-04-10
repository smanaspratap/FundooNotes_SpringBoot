package com.fundoonotes.service;

import com.fundoonotes.dto.request.LoginRequestDto;
import com.fundoonotes.dto.request.UserRegisterRequestDto;
import com.fundoonotes.dto.response.LoginResponseDto;
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
}

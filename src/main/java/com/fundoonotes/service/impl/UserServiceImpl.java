package com.fundoonotes.service.impl;

import com.fundoonotes.dto.request.LoginRequestDto;
import com.fundoonotes.dto.request.UserRegisterRequestDto;
import com.fundoonotes.dto.response.LoginResponseDto;
import com.fundoonotes.dto.response.UserResponseDto;
import com.fundoonotes.entity.User;
import com.fundoonotes.exception.InvalidCredentialsException;
import com.fundoonotes.exception.UserAlreadyExistsException;
import com.fundoonotes.exception.UserNotFoundException;
import com.fundoonotes.mapper.EntityDtoMapper;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.security.JwtUtil;
import com.fundoonotes.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementation of UserService.
 * Handles user registration and authentication with password encoding and JWT generation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public UserResponseDto register(UserRegisterRequestDto dto) {
        log.info("Attempting to register user with email: {}", dto.getEmail());

        // Check for duplicate email
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            log.warn("Registration failed — email already exists: {}", dto.getEmail());
            throw new UserAlreadyExistsException(
                    "User with email " + dto.getEmail() + " already exists");
        }

        // Build and save user entity
        User user = User.builder()
                .firstName(dto.getFirstName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with id: {}", savedUser.getId());

        return EntityDtoMapper.toUserResponseDto(savedUser);
    }

    @Override
    public LoginResponseDto login(LoginRequestDto dto) {
        log.info("Login attempt for email: {}", dto.getEmail());

        // Find user by email
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed — user not found: {}", dto.getEmail());
                    return new UserNotFoundException(
                            "User with email " + dto.getEmail() + " not found");
                });

        // Validate password
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("Login failed — invalid credentials for email: {}", dto.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        log.info("Login successful for userId: {}", user.getId());

        return LoginResponseDto.builder()
                .token(token)
                .email(user.getEmail())
                .message("Login successful")
                .build();
    }
}

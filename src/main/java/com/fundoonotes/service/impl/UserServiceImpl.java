package com.fundoonotes.service.impl;

import com.fundoonotes.cache.TokenCacheService;
import com.fundoonotes.dto.event.PasswordResetEvent;
import com.fundoonotes.dto.request.ForgotPasswordRequestDto;
import com.fundoonotes.dto.request.LoginRequestDto;
import com.fundoonotes.dto.request.ResetPasswordRequestDto;
import com.fundoonotes.dto.request.UserRegisterRequestDto;
import com.fundoonotes.dto.response.LoginResponseDto;
import com.fundoonotes.dto.response.MessageResponseDto;
import com.fundoonotes.dto.response.UserResponseDto;
import com.fundoonotes.entity.User;
import com.fundoonotes.exception.InvalidCredentialsException;
import com.fundoonotes.exception.InvalidOtpException;
import com.fundoonotes.exception.UserAlreadyExistsException;
import com.fundoonotes.exception.UserNotFoundException;
import com.fundoonotes.dto.event.UserRegistrationEvent;
import com.fundoonotes.mapper.EntityDtoMapper;
import com.fundoonotes.messaging.producer.EventPublisher;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.security.JwtUtil;
import com.fundoonotes.security.TokenValidationService;
import com.fundoonotes.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Implementation of UserService.
 * Handles user registration, authentication, password management, and logout.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EventPublisher eventPublisher;
    private final TokenCacheService tokenCacheService;
    private final TokenValidationService tokenValidationService;

    private static final long RESET_OTP_TTL_MINUTES = 10;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

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

        // Publish registration event for async welcome notification
        eventPublisher.publishUserRegistration(UserRegistrationEvent.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .timestamp(savedUser.getRegisteredAt())
                .build());

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

    @Override
    public MessageResponseDto forgotPassword(ForgotPasswordRequestDto dto) {
        log.info("Forgot password request for email: {}", dto.getEmail());

        // Verify user exists
        userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> {
                    log.warn("Forgot password failed — user not found: {}", dto.getEmail());
                    return new UserNotFoundException(
                            "User with email " + dto.getEmail() + " not found");
                });

        // Generate 6-digit OTP
        String otp = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));

        // Store OTP in Redis with TTL
        tokenCacheService.storeResetOtp(dto.getEmail(), otp, RESET_OTP_TTL_MINUTES);
        log.info("Reset OTP generated and stored for email: {}", dto.getEmail());

        // Publish password reset event (email only — NO OTP in event payload for security)
        eventPublisher.publishPasswordReset(PasswordResetEvent.builder()
                .email(dto.getEmail())
                .timestamp(LocalDateTime.now())
                .build());

        return MessageResponseDto.builder()
                .message("Password reset OTP has been sent. Please check your email.")
                .build();
    }

    @Override
    public MessageResponseDto resetPassword(ResetPasswordRequestDto dto) {
        log.info("Reset password attempt for email: {}", dto.getEmail());

        // Find user
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> {
                    log.warn("Reset password failed — user not found: {}", dto.getEmail());
                    return new UserNotFoundException(
                            "User with email " + dto.getEmail() + " not found");
                });

        // Validate OTP from Redis
        String storedOtp = tokenCacheService.getResetOtp(dto.getEmail());
        if (storedOtp == null) {
            log.warn("Reset password failed — OTP expired or not found for email: {}", dto.getEmail());
            throw new InvalidOtpException("OTP has expired. Please request a new one.");
        }
        if (!storedOtp.equals(dto.getOtp())) {
            log.warn("Reset password failed — invalid OTP for email: {}", dto.getEmail());
            throw new InvalidOtpException("Invalid OTP. Please try again.");
        }

        // Encode and save new password
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        log.info("Password reset successfully for userId: {}", user.getId());

        // Clean up OTP from Redis
        tokenCacheService.deleteResetOtp(dto.getEmail());

        return MessageResponseDto.builder()
                .message("Password has been reset successfully.")
                .build();
    }

    @Override
    public MessageResponseDto logout(String token) {
        // Validate the token first
        Long userId = tokenValidationService.validateAndExtractUserId(token);
        log.info("Logout request for userId: {}", userId);

        // Calculate remaining TTL and blacklist in Redis
        long remainingTtlMs = tokenValidationService.getRemainingTtlMs(token);
        if (remainingTtlMs > 0) {
            tokenCacheService.blacklistToken(token, remainingTtlMs);
            log.info("Token blacklisted for userId: {} (TTL={}ms)", userId, remainingTtlMs);
        }

        return MessageResponseDto.builder()
                .message("Logout successful.")
                .build();
    }
}

package com.fundoonotes.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Event published when a password reset is initiated.
 * SECURITY: Never include OTP or reset token in this event payload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetEvent implements Serializable {

    private String email;
    private LocalDateTime timestamp;
}

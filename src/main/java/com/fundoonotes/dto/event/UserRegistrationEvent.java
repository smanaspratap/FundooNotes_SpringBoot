package com.fundoonotes.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Event published when a new user registers successfully.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationEvent implements Serializable {

    private Long userId;
    private String email;
    private String firstName;
    private LocalDateTime timestamp;
}

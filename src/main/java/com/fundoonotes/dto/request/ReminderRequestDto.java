package com.fundoonotes.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for creating a new reminder.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReminderRequestDto {

    @NotNull(message = "Note ID is required")
    private Long noteId;

    @NotNull(message = "Reminder time is required")
    @Future(message = "Reminder time must be in the future")
    private LocalDateTime reminderTime;
}

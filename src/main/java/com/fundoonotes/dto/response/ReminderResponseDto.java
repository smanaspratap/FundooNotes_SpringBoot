package com.fundoonotes.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for reminder data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReminderResponseDto {

    private Long id;
    private Long noteId;
    private String noteTitle;
    private LocalDateTime reminderTime;
    private boolean notified;
    private LocalDateTime createdAt;
}

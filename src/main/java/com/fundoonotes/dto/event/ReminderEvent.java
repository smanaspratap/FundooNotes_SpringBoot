package com.fundoonotes.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Event published when a new reminder is created.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReminderEvent implements Serializable {

    private Long reminderId;
    private Long noteId;
    private Long userId;
    private LocalDateTime reminderTime;
}

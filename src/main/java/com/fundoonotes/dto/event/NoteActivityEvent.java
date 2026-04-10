package com.fundoonotes.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Event published when a note state changes (pin, archive, trash, etc.).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteActivityEvent implements Serializable {

    private Long noteId;
    private Long userId;
    private String action;
    private LocalDateTime timestamp;
}

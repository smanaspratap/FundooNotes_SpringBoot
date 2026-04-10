package com.fundoonotes.service;

import com.fundoonotes.dto.request.ReminderRequestDto;
import com.fundoonotes.dto.response.ReminderResponseDto;

import java.util.List;

/**
 * Service interface for reminder operations.
 */
public interface ReminderService {

    /**
     * Create a new reminder for a note.
     *
     * @param dto   the reminder request data
     * @param token the JWT token from Authorization header
     * @return the created reminder's response data
     */
    ReminderResponseDto createReminder(ReminderRequestDto dto, String token);

    /**
     * Get all reminders for the authenticated user.
     *
     * @param token the JWT token from Authorization header
     * @return list of all reminders
     */
    List<ReminderResponseDto> getReminders(String token);
}

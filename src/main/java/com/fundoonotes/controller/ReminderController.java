package com.fundoonotes.controller;

import com.fundoonotes.dto.request.ReminderRequestDto;
import com.fundoonotes.dto.response.ReminderResponseDto;
import com.fundoonotes.service.ReminderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for reminder management endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    /**
     * Create a new reminder for a note.
     *
     * @param dto   the reminder request body
     * @param token the JWT token from Authorization header
     * @return created reminder data with 201 status
     */
    @PostMapping
    public ResponseEntity<ReminderResponseDto> createReminder(
            @Valid @RequestBody ReminderRequestDto dto,
            @RequestHeader("Authorization") String token) {
        log.info("POST /api/reminders — creating reminder for noteId={}", dto.getNoteId());
        ReminderResponseDto response = reminderService.createReminder(dto, token);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get all reminders for the authenticated user.
     *
     * @param token the JWT token from Authorization header
     * @return list of reminders with 200 status
     */
    @GetMapping
    public ResponseEntity<List<ReminderResponseDto>> getReminders(
            @RequestHeader("Authorization") String token) {
        log.info("GET /api/reminders — retrieving all reminders");
        List<ReminderResponseDto> reminders = reminderService.getReminders(token);
        return ResponseEntity.ok(reminders);
    }
}

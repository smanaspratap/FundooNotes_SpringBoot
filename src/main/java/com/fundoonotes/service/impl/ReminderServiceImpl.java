package com.fundoonotes.service.impl;

import com.fundoonotes.dto.event.ReminderEvent;
import com.fundoonotes.dto.request.ReminderRequestDto;
import com.fundoonotes.dto.response.ReminderResponseDto;
import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.Reminder;
import com.fundoonotes.exception.NoteNotFoundException;
import com.fundoonotes.exception.UnauthorizedAccessException;
import com.fundoonotes.mapper.EntityDtoMapper;
import com.fundoonotes.messaging.producer.EventPublisher;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.ReminderRepository;
import com.fundoonotes.security.TokenValidationService;
import com.fundoonotes.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of ReminderService.
 * Handles reminder creation with note ownership validation and event publishing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderServiceImpl implements ReminderService {

    private final ReminderRepository reminderRepository;
    private final NoteRepository noteRepository;
    private final TokenValidationService tokenValidationService;
    private final EventPublisher eventPublisher;

    @Override
    public ReminderResponseDto createReminder(ReminderRequestDto dto, String token) {
        Long userId = tokenValidationService.validateAndExtractUserId(token);

        // Find the note and verify ownership
        Note note = noteRepository.findById(dto.getNoteId())
                .orElseThrow(() -> {
                    log.warn("Note not found: noteId={}", dto.getNoteId());
                    return new NoteNotFoundException("Note not found with id: " + dto.getNoteId());
                });

        if (!note.getUser().getId().equals(userId)) {
            log.warn("Unauthorized reminder creation: userId={} for noteId={}", userId, dto.getNoteId());
            throw new UnauthorizedAccessException("You are not authorized to set a reminder on this note");
        }

        // Create and save reminder
        Reminder reminder = Reminder.builder()
                .reminderTime(dto.getReminderTime())
                .note(note)
                .build();

        Reminder savedReminder = reminderRepository.save(reminder);
        log.info("Reminder created: id={} for noteId={} at={}", savedReminder.getId(), dto.getNoteId(), dto.getReminderTime());

        // Publish reminder event for async processing
        eventPublisher.publishReminderCreated(ReminderEvent.builder()
                .reminderId(savedReminder.getId())
                .noteId(note.getId())
                .userId(userId)
                .reminderTime(dto.getReminderTime())
                .build());

        return EntityDtoMapper.toReminderResponseDto(savedReminder);
    }

    @Override
    public List<ReminderResponseDto> getReminders(String token) {
        Long userId = tokenValidationService.validateAndExtractUserId(token);

        List<Reminder> reminders = reminderRepository.findByNoteUserId(userId);
        log.info("Retrieved {} reminders for userId={}", reminders.size(), userId);

        return reminders.stream()
                .map(EntityDtoMapper::toReminderResponseDto)
                .collect(Collectors.toList());
    }
}

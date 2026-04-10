package com.fundoonotes.service.impl;

import com.fundoonotes.dto.request.NoteRequestDto;
import com.fundoonotes.dto.response.NoteResponseDto;
import com.fundoonotes.dto.event.NoteActivityEvent;
import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import com.fundoonotes.exception.NoteNotFoundException;
import com.fundoonotes.exception.UnauthorizedAccessException;
import com.fundoonotes.exception.UserNotFoundException;
import com.fundoonotes.mapper.EntityDtoMapper;
import com.fundoonotes.messaging.producer.EventPublisher;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.security.TokenValidationService;
import com.fundoonotes.service.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of NoteService.
 * Handles note CRUD and state transitions with ownership validation.
 *
 * Caching strategy:
 * - Public methods keep the String token signature (no Part 1 API drift)
 * - Internal methods with Long userId are where @Cacheable/@CacheEvict apply
 * - Mutations evict the user's cache; reads populate it
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final TokenValidationService tokenValidationService;
    private final EventPublisher eventPublisher;

    // =========================================================================
    // Public API methods — Part 1 signatures preserved
    // =========================================================================

    @Override
    public NoteResponseDto createNote(NoteRequestDto dto, String token) {
        Long userId = extractAndValidateUserId(token);
        User user = findUserById(userId);
        return createNoteForUser(dto, user, userId);
    }

    @Override
    public List<NoteResponseDto> getAllNotes(String token) {
        Long userId = extractAndValidateUserId(token);
        return getAllNotesByUserId(userId);
    }

    @Override
    public NoteResponseDto pinNote(Long noteId, String token) {
        Note note = getAuthorizedNote(noteId, token);
        note.setPinned(true);
        Note saved = noteRepository.save(note);
        Long userId = saved.getUser().getId();
        log.info("Note pinned: noteId={}", noteId);
        publishNoteActivity(noteId, userId, "PIN");
        evictUserNotesCache(userId);
        return EntityDtoMapper.toNoteResponseDto(saved);
    }

    @Override
    public NoteResponseDto unpinNote(Long noteId, String token) {
        Note note = getAuthorizedNote(noteId, token);
        note.setPinned(false);
        Note saved = noteRepository.save(note);
        Long userId = saved.getUser().getId();
        log.info("Note unpinned: noteId={}", noteId);
        evictUserNotesCache(userId);
        return EntityDtoMapper.toNoteResponseDto(saved);
    }

    @Override
    public NoteResponseDto archiveNote(Long noteId, String token) {
        Note note = getAuthorizedNote(noteId, token);
        note.setArchived(true);
        Note saved = noteRepository.save(note);
        Long userId = saved.getUser().getId();
        log.info("Note archived: noteId={}", noteId);
        publishNoteActivity(noteId, userId, "ARCHIVE");
        evictUserNotesCache(userId);
        return EntityDtoMapper.toNoteResponseDto(saved);
    }

    @Override
    public NoteResponseDto unarchiveNote(Long noteId, String token) {
        Note note = getAuthorizedNote(noteId, token);
        note.setArchived(false);
        Note saved = noteRepository.save(note);
        Long userId = saved.getUser().getId();
        log.info("Note unarchived: noteId={}", noteId);
        evictUserNotesCache(userId);
        return EntityDtoMapper.toNoteResponseDto(saved);
    }

    @Override
    public NoteResponseDto trashNote(Long noteId, String token) {
        Note note = getAuthorizedNote(noteId, token);
        note.setTrashed(true);
        Note saved = noteRepository.save(note);
        Long userId = saved.getUser().getId();
        log.info("Note trashed: noteId={}", noteId);
        publishNoteActivity(noteId, userId, "TRASH");
        evictUserNotesCache(userId);
        return EntityDtoMapper.toNoteResponseDto(saved);
    }

    @Override
    public NoteResponseDto restoreNote(Long noteId, String token) {
        Note note = getAuthorizedNote(noteId, token);
        note.setTrashed(false);
        Note saved = noteRepository.save(note);
        Long userId = saved.getUser().getId();
        log.info("Note restored from trash: noteId={}", noteId);
        evictUserNotesCache(userId);
        return EntityDtoMapper.toNoteResponseDto(saved);
    }

    // =========================================================================
    // Internal cached methods — clean cache keys by userId
    // =========================================================================

    /**
     * Get all notes for a user, backed by Redis cache.
     * Cache key: userNotes::{userId}
     */
    @Cacheable(value = "userNotes", key = "#userId")
    public List<NoteResponseDto> getAllNotesByUserId(Long userId) {
        List<Note> notes = noteRepository.findByUserId(userId);
        log.info("Cache MISS — fetched {} notes from DB for userId={}", notes.size(), userId);

        return notes.stream()
                .map(EntityDtoMapper::toNoteResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Create a note and evict the user's cache.
     */
    @CacheEvict(value = "userNotes", key = "#userId")
    public NoteResponseDto createNoteForUser(NoteRequestDto dto, User user, Long userId) {
        Note note = EntityDtoMapper.toNoteEntity(dto, user);
        Note savedNote = noteRepository.save(note);
        log.info("Note created with id={} for userId={} (cache evicted)", savedNote.getId(), userId);
        return EntityDtoMapper.toNoteResponseDto(savedNote);
    }

    /**
     * Evict cache for a specific user (called after mutations).
     */
    @CacheEvict(value = "userNotes", key = "#userId")
    public void evictUserNotesCache(Long userId) {
        log.debug("Cache evicted for userId={}", userId);
    }

    // =========================================================================
    // Private helper methods
    // =========================================================================

    /**
     * Validate token and extract userId via TokenValidationService.
     * Delegates all auth logic to the security boundary.
     */
    private Long extractAndValidateUserId(String token) {
        return tokenValidationService.validateAndExtractUserId(token);
    }

    /**
     * Find a user by ID or throw UserNotFoundException.
     */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found for userId: {}", userId);
                    return new UserNotFoundException("User not found with id: " + userId);
                });
    }

    /**
     * Retrieve a note and verify the requesting user owns it.
     * Throws NoteNotFoundException if note doesn't exist.
     * Throws UnauthorizedAccessException if user doesn't own the note.
     */
    private Note getAuthorizedNote(Long noteId, String token) {
        Long userId = extractAndValidateUserId(token);

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> {
                    log.warn("Note not found: noteId={}", noteId);
                    return new NoteNotFoundException("Note not found with id: " + noteId);
                });

        if (!note.getUser().getId().equals(userId)) {
            log.warn("Unauthorized access attempt: userId={} tried to access noteId={}", userId, noteId);
            throw new UnauthorizedAccessException(
                    "You are not authorized to access this note");
        }

        return note;
    }

    /**
     * Publish a note activity audit event to RabbitMQ.
     */
    private void publishNoteActivity(Long noteId, Long userId, String action) {
        eventPublisher.publishNoteActivity(NoteActivityEvent.builder()
                .noteId(noteId)
                .userId(userId)
                .action(action)
                .timestamp(LocalDateTime.now())
                .build());
    }
}

package com.fundoonotes.service.impl;

import com.fundoonotes.dto.request.NoteRequestDto;
import com.fundoonotes.dto.response.NoteResponseDto;
import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import com.fundoonotes.exception.NoteNotFoundException;
import com.fundoonotes.exception.UnauthorizedAccessException;
import com.fundoonotes.exception.UserNotFoundException;
import com.fundoonotes.mapper.EntityDtoMapper;
import com.fundoonotes.repository.NoteRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.security.JwtUtil;
import com.fundoonotes.service.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of NoteService.
 * Handles note CRUD and state transitions with ownership validation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    public NoteResponseDto createNote(NoteRequestDto dto, String token) {
        Long userId = extractAndValidateUserId(token);
        User user = findUserById(userId);

        Note note = EntityDtoMapper.toNoteEntity(dto, user);
        Note savedNote = noteRepository.save(note);

        log.info("Note created with id: {} for userId: {}", savedNote.getId(), userId);
        return EntityDtoMapper.toNoteResponseDto(savedNote);
    }

    @Override
    public List<NoteResponseDto> getAllNotes(String token) {
        Long userId = extractAndValidateUserId(token);

        List<Note> notes = noteRepository.findByUserId(userId);
        log.info("Retrieved {} notes for userId: {}", notes.size(), userId);

        return notes.stream()
                .map(EntityDtoMapper::toNoteResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public NoteResponseDto pinNote(Long noteId, String token) {
        Note note = getAuthorizedNote(noteId, token);
        note.setPinned(true);
        Note saved = noteRepository.save(note);
        log.info("Note pinned: noteId={}", noteId);
        return EntityDtoMapper.toNoteResponseDto(saved);
    }

    @Override
    public NoteResponseDto unpinNote(Long noteId, String token) {
        Note note = getAuthorizedNote(noteId, token);
        note.setPinned(false);
        Note saved = noteRepository.save(note);
        log.info("Note unpinned: noteId={}", noteId);
        return EntityDtoMapper.toNoteResponseDto(saved);
    }

    @Override
    public NoteResponseDto archiveNote(Long noteId, String token) {
        Note note = getAuthorizedNote(noteId, token);
        note.setArchived(true);
        Note saved = noteRepository.save(note);
        log.info("Note archived: noteId={}", noteId);
        return EntityDtoMapper.toNoteResponseDto(saved);
    }

    @Override
    public NoteResponseDto unarchiveNote(Long noteId, String token) {
        Note note = getAuthorizedNote(noteId, token);
        note.setArchived(false);
        Note saved = noteRepository.save(note);
        log.info("Note unarchived: noteId={}", noteId);
        return EntityDtoMapper.toNoteResponseDto(saved);
    }

    @Override
    public NoteResponseDto trashNote(Long noteId, String token) {
        Note note = getAuthorizedNote(noteId, token);
        note.setTrashed(true);
        Note saved = noteRepository.save(note);
        log.info("Note trashed: noteId={}", noteId);
        return EntityDtoMapper.toNoteResponseDto(saved);
    }

    @Override
    public NoteResponseDto restoreNote(Long noteId, String token) {
        Note note = getAuthorizedNote(noteId, token);
        note.setTrashed(false);
        Note saved = noteRepository.save(note);
        log.info("Note restored from trash: noteId={}", noteId);
        return EntityDtoMapper.toNoteResponseDto(saved);
    }

    // =========================================================================
    // Private helper methods
    // =========================================================================

    /**
     * Extract userId from token and validate the token.
     * Throws UnauthorizedAccessException if token is invalid.
     */
    private Long extractAndValidateUserId(String token) {
        if (token == null || token.isBlank()) {
            log.warn("Missing or empty authorization token");
            throw new UnauthorizedAccessException("Authorization token is required");
        }

        if (!jwtUtil.validateToken(token)) {
            log.warn("Invalid or expired authorization token");
            throw new UnauthorizedAccessException("Invalid or expired token");
        }

        return jwtUtil.extractUserId(token);
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
}

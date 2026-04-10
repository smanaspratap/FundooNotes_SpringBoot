package com.fundoonotes.controller;

import com.fundoonotes.dto.request.NoteRequestDto;
import com.fundoonotes.dto.response.NoteResponseDto;
import com.fundoonotes.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for note management endpoints.
 * Token is passed via the Authorization header.
 */
@Slf4j
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    /**
     * Create a new note for the authenticated user.
     */
    @PostMapping
    public ResponseEntity<NoteResponseDto> createNote(
            @Valid @RequestBody NoteRequestDto dto,
            @RequestHeader("Authorization") String token) {
        log.info("POST /api/notes — creating note");
        NoteResponseDto response = noteService.createNote(dto, token);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get all notes for the authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<NoteResponseDto>> getAllNotes(
            @RequestHeader("Authorization") String token) {
        log.info("GET /api/notes — retrieving all notes");
        List<NoteResponseDto> notes = noteService.getAllNotes(token);
        return ResponseEntity.ok(notes);
    }

    /**
     * Pin a note (set pinned = true).
     */
    @PatchMapping("/{noteId}/pin")
    public ResponseEntity<NoteResponseDto> pinNote(
            @PathVariable Long noteId,
            @RequestHeader("Authorization") String token) {
        log.info("PATCH /api/notes/{}/pin", noteId);
        NoteResponseDto response = noteService.pinNote(noteId, token);
        return ResponseEntity.ok(response);
    }

    /**
     * Unpin a note (set pinned = false).
     */
    @PatchMapping("/{noteId}/unpin")
    public ResponseEntity<NoteResponseDto> unpinNote(
            @PathVariable Long noteId,
            @RequestHeader("Authorization") String token) {
        log.info("PATCH /api/notes/{}/unpin", noteId);
        NoteResponseDto response = noteService.unpinNote(noteId, token);
        return ResponseEntity.ok(response);
    }

    /**
     * Archive a note (set archived = true).
     */
    @PatchMapping("/{noteId}/archive")
    public ResponseEntity<NoteResponseDto> archiveNote(
            @PathVariable Long noteId,
            @RequestHeader("Authorization") String token) {
        log.info("PATCH /api/notes/{}/archive", noteId);
        NoteResponseDto response = noteService.archiveNote(noteId, token);
        return ResponseEntity.ok(response);
    }

    /**
     * Unarchive a note (set archived = false).
     */
    @PatchMapping("/{noteId}/unarchive")
    public ResponseEntity<NoteResponseDto> unarchiveNote(
            @PathVariable Long noteId,
            @RequestHeader("Authorization") String token) {
        log.info("PATCH /api/notes/{}/unarchive", noteId);
        NoteResponseDto response = noteService.unarchiveNote(noteId, token);
        return ResponseEntity.ok(response);
    }

    /**
     * Trash a note (set trashed = true).
     */
    @PatchMapping("/{noteId}/trash")
    public ResponseEntity<NoteResponseDto> trashNote(
            @PathVariable Long noteId,
            @RequestHeader("Authorization") String token) {
        log.info("PATCH /api/notes/{}/trash", noteId);
        NoteResponseDto response = noteService.trashNote(noteId, token);
        return ResponseEntity.ok(response);
    }

    /**
     * Restore a note from trash (set trashed = false).
     */
    @PatchMapping("/{noteId}/restore")
    public ResponseEntity<NoteResponseDto> restoreNote(
            @PathVariable Long noteId,
            @RequestHeader("Authorization") String token) {
        log.info("PATCH /api/notes/{}/restore", noteId);
        NoteResponseDto response = noteService.restoreNote(noteId, token);
        return ResponseEntity.ok(response);
    }
}

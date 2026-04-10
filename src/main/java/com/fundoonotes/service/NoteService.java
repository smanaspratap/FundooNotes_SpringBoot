package com.fundoonotes.service;

import com.fundoonotes.dto.request.NoteRequestDto;
import com.fundoonotes.dto.response.NoteResponseDto;

import java.util.List;

/**
 * Service interface for note-related operations.
 */
public interface NoteService {

    /**
     * Create a new note for the authenticated user.
     *
     * @param dto   the note request data
     * @param token the JWT token from Authorization header
     * @return the created note's response data
     */
    NoteResponseDto createNote(NoteRequestDto dto, String token);

    /**
     * Get all notes for the authenticated user.
     *
     * @param token the JWT token from Authorization header
     * @return list of all notes
     */
    List<NoteResponseDto> getAllNotes(String token);

    /**
     * Set a note as pinned.
     *
     * @param noteId the note ID
     * @param token  the JWT token
     * @return updated note response
     */
    NoteResponseDto pinNote(Long noteId, String token);

    /**
     * Remove pin from a note.
     *
     * @param noteId the note ID
     * @param token  the JWT token
     * @return updated note response
     */
    NoteResponseDto unpinNote(Long noteId, String token);

    /**
     * Set a note as archived.
     *
     * @param noteId the note ID
     * @param token  the JWT token
     * @return updated note response
     */
    NoteResponseDto archiveNote(Long noteId, String token);

    /**
     * Remove archive from a note.
     *
     * @param noteId the note ID
     * @param token  the JWT token
     * @return updated note response
     */
    NoteResponseDto unarchiveNote(Long noteId, String token);

    /**
     * Move a note to trash.
     *
     * @param noteId the note ID
     * @param token  the JWT token
     * @return updated note response
     */
    NoteResponseDto trashNote(Long noteId, String token);

    /**
     * Restore a note from trash.
     *
     * @param noteId the note ID
     * @param token  the JWT token
     * @return updated note response
     */
    NoteResponseDto restoreNote(Long noteId, String token);
}

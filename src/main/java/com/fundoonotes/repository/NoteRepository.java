package com.fundoonotes.repository;

import com.fundoonotes.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Note entity persistence operations.
 */
@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    /**
     * Find all notes belonging to a specific user.
     *
     * @param userId the user's ID
     * @return list of notes
     */
    List<Note> findByUserId(Long userId);

    /**
     * Find active notes (not archived and not trashed) for a user.
     *
     * @param userId the user's ID
     * @return list of active notes
     */
    List<Note> findByUserIdAndArchivedFalseAndTrashedFalse(Long userId);

    /**
     * Search notes by title (case-insensitive) for a specific user.
     *
     * @param userId the user's ID
     * @param title  the title substring to search
     * @return list of matching notes
     */
    List<Note> findByUserIdAndTitleContainingIgnoreCase(Long userId, String title);
}

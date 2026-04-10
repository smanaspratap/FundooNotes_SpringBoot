package com.fundoonotes.repository;

import com.fundoonotes.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Reminder entity persistence operations.
 */
@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    /**
     * Find all reminders for notes belonging to a specific user.
     *
     * @param userId the user's ID
     * @return list of reminders
     */
    List<Reminder> findByNoteUserId(Long userId);

    /**
     * Find all unnotified reminders whose time has passed.
     * Used for processing pending reminder notifications.
     *
     * @param time the cutoff time
     * @return list of due reminders
     */
    List<Reminder> findByNotifiedFalseAndReminderTimeBefore(LocalDateTime time);
}

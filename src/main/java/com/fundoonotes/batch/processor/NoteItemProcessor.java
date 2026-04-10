package com.fundoonotes.batch.processor;

import com.fundoonotes.batch.dto.NoteCsvRow;
import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

/**
 * Spring Batch item processor that converts CSV rows into Note entities.
 * Validates the data and associates notes with the importing user.
 */
@Slf4j
public class NoteItemProcessor implements ItemProcessor<NoteCsvRow, Note> {

    private final User user;

    public NoteItemProcessor(User user) {
        this.user = user;
    }

    @Override
    public Note process(NoteCsvRow item) throws Exception {
        // Validate required fields
        if (item.getTitle() == null || item.getTitle().isBlank()) {
            log.warn("Skipping CSV row with empty title");
            return null; // Returning null skips the item
        }

        Note note = Note.builder()
                .title(item.getTitle().trim())
                .description(item.getDescription() != null ? item.getDescription().trim() : "")
                .user(user)
                .build();

        log.debug("Processed CSV row → Note(title={})", note.getTitle());
        return note;
    }
}

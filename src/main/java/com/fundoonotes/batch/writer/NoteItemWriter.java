package com.fundoonotes.batch.writer;

import com.fundoonotes.entity.Note;
import com.fundoonotes.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

/**
 * Spring Batch item writer that persists processed Note entities to the database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NoteItemWriter implements ItemWriter<Note> {

    private final NoteRepository noteRepository;

    @Override
    public void write(Chunk<? extends Note> chunk) throws Exception {
        log.info("Batch writing {} notes to database", chunk.size());
        noteRepository.saveAll(chunk.getItems());
    }
}

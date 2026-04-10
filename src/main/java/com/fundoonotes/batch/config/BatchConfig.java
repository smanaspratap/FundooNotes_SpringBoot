package com.fundoonotes.batch.config;

import com.fundoonotes.batch.dto.NoteCsvRow;
import com.fundoonotes.batch.processor.NoteItemProcessor;
import com.fundoonotes.batch.writer.NoteItemWriter;
import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.separator.DefaultRecordSeparatorPolicy;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch configuration for CSV note import.
 *
 * Job and Step beans are defined here. The reader is created per-request
 * with the uploaded CSV file as its resource. The processor receives
 * the authenticated user to set note ownership.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final NoteItemWriter noteItemWriter;

    /**
     * Create a FlatFileItemReader for NoteCsvRow from an uploaded CSV resource.
     */
    public FlatFileItemReader<NoteCsvRow> csvReader(Resource resource) {
        FlatFileItemReader<NoteCsvRow> reader = new FlatFileItemReader<>();
        reader.setResource(resource);
        reader.setLinesToSkip(1); // Skip header row
        reader.setRecordSeparatorPolicy(new DefaultRecordSeparatorPolicy());

        DefaultLineMapper<NoteCsvRow> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("title", "description");
        lineMapper.setLineTokenizer(tokenizer);

        BeanWrapperFieldSetMapper<NoteCsvRow> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(NoteCsvRow.class);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        reader.setLineMapper(lineMapper);
        return reader;
    }

    /**
     * Create a Step for the note import job.
     */
    public Step importNoteStep(Resource csvResource, User user) {
        return new StepBuilder("importNoteStep", jobRepository)
                .<NoteCsvRow, Note>chunk(10, transactionManager)
                .reader(csvReader(csvResource))
                .processor(new NoteItemProcessor(user))
                .writer(noteItemWriter)
                .build();
    }

    /**
     * Create a Job for the note import.
     */
    public Job importNoteJob(Resource csvResource, User user) {
        return new JobBuilder("importNoteJob", jobRepository)
                .start(importNoteStep(csvResource, user))
                .build();
    }
}

package com.fundoonotes.controller;

import com.fundoonotes.batch.config.BatchConfig;
import com.fundoonotes.dto.response.MessageResponseDto;
import com.fundoonotes.entity.User;
import com.fundoonotes.exception.BatchProcessingException;
import com.fundoonotes.exception.UserNotFoundException;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.security.TokenValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for batch operations.
 * Authenticated admin/utility endpoint for CSV note import.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/batch")
@RequiredArgsConstructor
public class BatchController {

    private final BatchConfig batchConfig;
    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final TokenValidationService tokenValidationService;
    private final UserRepository userRepository;

    /**
     * Import notes from a CSV file for the authenticated user.
     *
     * CSV format:
     * title,description
     * "Shopping List","Buy milk, eggs, bread"
     *
     * @param file  the CSV file to import
     * @param token the JWT token from Authorization header
     * @return execution status with job ID
     */
    @PostMapping("/import-notes")
    public ResponseEntity<Map<String, Object>> importNotes(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String token) {
        log.info("POST /api/admin/batch/import-notes — CSV import requested");

        Long userId = tokenValidationService.validateAndExtractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        try {
            InputStreamResource resource = new InputStreamResource(file.getInputStream());

            Job job = batchConfig.importNoteJob(resource, user);
            JobParameters params = new JobParametersBuilder()
                    .addLong("startTime", System.currentTimeMillis())
                    .addLong("userId", userId)
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(job, params);
            log.info("Batch job started: executionId={}, status={}", execution.getId(), execution.getStatus());

            Map<String, Object> response = new HashMap<>();
            response.put("executionId", execution.getId());
            response.put("status", execution.getStatus().toString());
            response.put("message", "Batch import job launched successfully");

            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);

        } catch (Exception ex) {
            log.error("Batch import failed: {}", ex.getMessage(), ex);
            throw new BatchProcessingException("Failed to process CSV import: " + ex.getMessage());
        }
    }

    /**
     * Check the status of a batch import job.
     *
     * @param executionId the job execution ID
     * @return job status details
     */
    @GetMapping("/status/{executionId}")
    public ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable Long executionId) {
        log.info("GET /api/admin/batch/status/{}", executionId);

        JobExecution execution = jobExplorer.getJobExecution(executionId);
        if (execution == null) {
            Map<String, Object> notFound = new HashMap<>();
            notFound.put("message", "Job execution not found with id: " + executionId);
            return new ResponseEntity<>(notFound, HttpStatus.NOT_FOUND);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("executionId", execution.getId());
        response.put("status", execution.getStatus().toString());
        response.put("startTime", execution.getStartTime());
        response.put("endTime", execution.getEndTime());
        response.put("exitStatus", execution.getExitStatus().getExitCode());

        return ResponseEntity.ok(response);
    }
}

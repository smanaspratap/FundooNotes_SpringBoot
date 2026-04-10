package com.fundoonotes.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for note creation and update requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteRequestDto {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
}

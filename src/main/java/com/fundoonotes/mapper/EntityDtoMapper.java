package com.fundoonotes.mapper;

import com.fundoonotes.dto.request.NoteRequestDto;
import com.fundoonotes.dto.response.NoteResponseDto;
import com.fundoonotes.dto.response.UserResponseDto;
import com.fundoonotes.entity.Note;
import com.fundoonotes.entity.User;

/**
 * Utility class for mapping between entities and DTOs.
 * Centralizes all conversion logic to keep service layer clean
 * and make future extensions (labels, reminders, etc.) easy to integrate.
 */
public final class EntityDtoMapper {

    private EntityDtoMapper() {
        // Prevent instantiation — static utility class
    }

    /**
     * Convert a User entity to UserResponseDto.
     *
     * @param user the user entity
     * @return the user response DTO
     */
    public static UserResponseDto toUserResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .email(user.getEmail())
                .registeredAt(user.getRegisteredAt())
                .build();
    }

    /**
     * Convert a Note entity to NoteResponseDto.
     *
     * @param note the note entity
     * @return the note response DTO
     */
    public static NoteResponseDto toNoteResponseDto(Note note) {
        return NoteResponseDto.builder()
                .id(note.getId())
                .title(note.getTitle())
                .description(note.getDescription())
                .pinned(note.isPinned())
                .archived(note.isArchived())
                .trashed(note.isTrashed())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }

    /**
     * Convert a NoteRequestDto to a Note entity.
     * Sets the user association but leaves state flags at defaults.
     *
     * @param dto  the note request DTO
     * @param user the owning user
     * @return the note entity
     */
    public static Note toNoteEntity(NoteRequestDto dto, User user) {
        return Note.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .user(user)
                .build();
    }
}

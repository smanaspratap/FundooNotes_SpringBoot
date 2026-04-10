package com.fundoonotes.batch.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a single row in the note import CSV file.
 * Mapped by column header names via OpenCSV.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteCsvRow {

    @CsvBindByName(column = "title", required = true)
    private String title;

    @CsvBindByName(column = "description")
    private String description;
}

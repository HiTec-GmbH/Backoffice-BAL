package de.szut.pms.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateProjectDto(

        @NotBlank(message = "Die Bezeichnung darf nicht leer sein.")
        String name,

        @NotNull(message = "Die Kunden-ID muss angegeben werden.")
        Long customerId,

        @NotBlank(message = "Der Ansprechpartner muss angegeben werden.")
        String customerContact,

        @NotNull(message = "Die Projektleiter-ID muss angegeben werden.")
        Long projectLeaderId,

        @NotBlank(message = "Das Projektziel muss angegeben werden.")
        String comment,

        @NotNull(message = "Das Startdatum muss angegeben werden.")
        LocalDate startDate,

        @NotNull(message = "Das Enddatum muss angegeben werden.")
        LocalDate endDate

) {
}
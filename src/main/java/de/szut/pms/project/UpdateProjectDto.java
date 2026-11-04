
package de.szut.pms.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateProjectDto(

        @Schema(example = "Webshop Relaunch")
        @NotBlank(message = "Die Bezeichnung darf nicht leer sein.")
        String name,

        @Schema(example = "42")
        @NotNull(message = "Die Kunden-ID muss angegeben werden.")
        Long customerId,

        @Schema(example = "Erika Musterfrau")
        @NotBlank(message = "Der Ansprechpartner muss angegeben werden.")
        String customerContact,

        @Schema(example = "1")
        @NotNull(message = "Die Projektleiter-ID muss angegeben werden.")
        Long projectLeaderId,

        @Schema(example = "Neuer Webshop bis Jahresende")
        @NotBlank(message = "Das Projektziel muss angegeben werden.")
        String comment,

        @Schema(example = "2027-01-01")
        @NotNull(message = "Das Startdatum muss angegeben werden.")
        LocalDate startDate,

        @Schema(example = "2027-03-31")
        @NotNull(message = "Das Enddatum muss angegeben werden.")
        LocalDate endDate

) {
}

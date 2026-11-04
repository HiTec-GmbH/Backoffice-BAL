package de.szut.pms.projectemployee;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProjectEmployeeDto(

        @Schema(example = "Java")
        @NotBlank(message = "Die Qualifikation darf nicht leer sein")
        String qualification,

        @Schema(example = "5")
        @NotNull(message = "Die Mitarbeiter-Id darf nicht leer sein")
        Long employeeId) {
}

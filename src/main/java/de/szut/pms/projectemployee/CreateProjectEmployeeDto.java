package de.szut.pms.projectemployee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProjectEmployeeDto(

        @NotBlank (message = "Die Qualifikation darf nicht leer sein")
        String qualification,

        @NotNull (message = "Die Mitarbeiter-Id darf nicht leer sein")
        Long employeeId)
        {
}

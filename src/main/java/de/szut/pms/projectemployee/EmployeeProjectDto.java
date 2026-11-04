package de.szut.pms.projectemployee;

import java.time.LocalDate;

public record EmployeeProjectDto(
        Long projectId,
        String projectName,
        LocalDate startDate,
        LocalDate endDate,
        String qualification) {
}

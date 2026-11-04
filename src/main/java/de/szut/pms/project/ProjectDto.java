package de.szut.pms.project;

import de.szut.pms.projectemployee.ProjectEmployeeDto;

import java.time.LocalDate;
import java.util.List;

public record ProjectDto(

        Long id,
        String name,
        Long customerId,
        String customerContact,
        Long projectLeaderId,
        String comment,
        LocalDate startDate,
        LocalDate endDate,
        List<ProjectEmployeeDto> projectEmployees

) {
}
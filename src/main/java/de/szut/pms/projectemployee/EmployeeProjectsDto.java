package de.szut.pms.projectemployee;

import java.util.List;

public record EmployeeProjectsDto(
        Long employeeId,
        List<EmployeeProjectDto> projects) {
}

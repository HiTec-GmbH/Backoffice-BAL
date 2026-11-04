package de.szut.pms.projectemployee;

import java.util.List;

public record ProjectEmployeesDto(
        Long projectId,
        String projectName,
        List<ProjectEmployeeDto> employees) {
}

package de.szut.pms.employee;

import java.util.List;

public record EmployeeDto(
    Long id,
    List<SkillDto> skillSet) {
}

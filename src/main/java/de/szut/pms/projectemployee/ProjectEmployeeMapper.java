package de.szut.pms.projectemployee;

import org.springframework.stereotype.Component;

@Component
public class ProjectEmployeeMapper {

    public ProjectEmployeeDto toDto(ProjectEmployeeEntity entity) {
        return new ProjectEmployeeDto(entity.getEmployeeId(), entity.getQualification());
    }

    public ProjectEmployeeEntity toEntity(CreateProjectEmployeeDto dto) {
        return new ProjectEmployeeEntity(dto.qualification(), dto.employeeId());
    }

    public EmployeeProjectDto toEmployeeProjectDto(ProjectEmployeeEntity entity) {
        return new EmployeeProjectDto(
                entity.getProject().getId(),
                entity.getProject().getName(),
                entity.getProject().getStartDate(),
                entity.getProject().getEndDate(),
                entity.getQualification());
    }
}

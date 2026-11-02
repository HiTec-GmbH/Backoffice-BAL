package de.szut.pms.projectemployee;

import org.springframework.stereotype.Component;

@Component
public class ProjectEmployeeMapper {

    public ProjectEmployeeDto toDto(ProjectEmployeeEntity entity) {
        return new ProjectEmployeeDto(entity.getId(), entity.getQualification(), entity.getEmployeeId());
    }

    public ProjectEmployeeEntity toEntity(CreateProjectEmployeeDto dto) {
        return new ProjectEmployeeEntity(dto.qualification(), dto.employeeId());
    }
}

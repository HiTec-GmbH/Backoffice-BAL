
package de.szut.pms.project;

import java.util.List;

import de.szut.pms.projectemployee.ProjectEmployeeDto;

import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public ProjectDto toDto(
            ProjectEntity entity,
            List<ProjectEmployeeDto> projectEmployees) {

        return new ProjectDto(
                entity.getId(),
                entity.getName(),
                entity.getCustomerId(),
                entity.getCustomerContact(),
                entity.getProjectLeaderId(),
                entity.getComment(),
                entity.getStartDate(),
                entity.getEndDate(),
                projectEmployees
        );
    }

    public ProjectEntity toEntity(CreateProjectDto dto) {

        ProjectEntity entity = new ProjectEntity();

        entity.setName(dto.name());
        entity.setCustomerId(dto.customerId());
        entity.setCustomerContact(dto.customerContact());
        entity.setProjectLeaderId(dto.projectLeaderId());
        entity.setComment(dto.comment());
        entity.setStartDate(dto.startDate());
        entity.setEndDate(dto.endDate());

        return entity;
    }

    public void updateEntity(
            ProjectEntity entity,
            UpdateProjectDto dto) {

        entity.setName(dto.name());
        entity.setCustomerId(dto.customerId());
        entity.setCustomerContact(dto.customerContact());
        entity.setProjectLeaderId(dto.projectLeaderId());
        entity.setComment(dto.comment());
        entity.setStartDate(dto.startDate());
        entity.setEndDate(dto.endDate());
    }
}

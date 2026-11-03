package de.szut.pms.projectemployee;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.szut.pms.employee.EmployeeAlreadyAssignedException;
import de.szut.pms.employee.EmployeeClient;
import de.szut.pms.employee.EmployeeDto;
import de.szut.pms.employee.EmployeeOverlapException;
import de.szut.pms.employee.QualificationMissingException;
import de.szut.pms.project.ProjectEntity;
import de.szut.pms.project.ProjectNotFoundException;
import de.szut.pms.project.ProjectRepository;

@Service
public class ProjectEmployeeService {

    private final ProjectEmployeeRepository repository;
    private final ProjectEmployeeMapper mapper;
    private final EmployeeClient employeeClient;
    private final ProjectRepository projectRepository;

    public ProjectEmployeeService(ProjectEmployeeRepository repository, ProjectEmployeeMapper mapper, EmployeeClient employeeClient, ProjectRepository projectRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.employeeClient = employeeClient;
        this.projectRepository = projectRepository;
    }

    @Transactional
    public ProjectEmployeeDto create(Long projectId, CreateProjectEmployeeDto dto) {
        ProjectEntity project = loadProject(projectId);
        ensureNotAssigned(dto.employeeId(), projectId);
        ensureNoOverlap(dto.employeeId(), project);
        ensureHasQualification(dto.employeeId(), dto.qualification());

        ProjectEmployeeEntity entity = mapper.toEntity(dto);
        entity.setProject(project);
        return mapper.toDto(repository.save(entity));
    }
    
    private ProjectEntity loadProject(Long projectId) {
    return projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(
                    "Es existiert kein Projekt mit der ID " + projectId + "."));
    }

    private void ensureNotAssigned(Long employeeId, Long projectId) {
        if (repository.isAssigned(employeeId, projectId)) {
            throw new EmployeeAlreadyAssignedException(employeeId, projectId);
        }
    }

    private void ensureNoOverlap(Long employeeId, ProjectEntity project) {
        List<ProjectEmployeeEntity> overlaps = repository.findOverlapping(
                employeeId, project.getId(), project.getStartDate(), project.getEndDate());
        if (!overlaps.isEmpty()) {
            throw new EmployeeOverlapException(employeeId, overlaps.get(0).getProject());
        }
    }

    private void ensureHasQualification(Long employeeId, String qualification) {
        EmployeeDto employee = employeeClient.findById(employeeId);
        boolean hasQualification = employee.skillSet().stream()
                .anyMatch(s -> s.skill().equalsIgnoreCase(qualification));
        if (!hasQualification) {
            throw new QualificationMissingException(employeeId, qualification);
        }
    }

    @Transactional(readOnly = true)
    public List<ProjectEmployeeDto> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public void deleteById(Long id) {
        ProjectEmployeeEntity entity = repository.findById(id)
                .orElseThrow(() -> new ProjectEmployeeNotFoundException(id));
        repository.delete(entity);
    }
}

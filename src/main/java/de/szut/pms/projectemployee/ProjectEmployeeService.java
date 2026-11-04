package de.szut.pms.projectemployee;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.szut.pms.employee.EmployeeAlreadyAssignedException;
import de.szut.pms.employee.EmployeeClient;
import de.szut.pms.employee.EmployeeDto;
import de.szut.pms.employee.EmployeeOverlapException;
import de.szut.pms.employee.QualificationMissingException;
import de.szut.pms.employee.SkillDto;
import de.szut.pms.project.ProjectEntity;
import de.szut.pms.project.ProjectNotFoundException;
import de.szut.pms.project.ProjectRepository;

@Service
public class ProjectEmployeeService {

    private final ProjectEmployeeRepository repository;
    private final ProjectEmployeeMapper mapper;
    private final EmployeeClient employeeClient;
    private final ProjectRepository projectRepository;

    public ProjectEmployeeService(ProjectEmployeeRepository repository, ProjectEmployeeMapper mapper,
            EmployeeClient employeeClient, ProjectRepository projectRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.employeeClient = employeeClient;
        this.projectRepository = projectRepository;
    }

    @Transactional
    public ProjectEmployeesDto create(Long projectId, CreateProjectEmployeeDto dto) {
        ProjectEntity project = loadProject(projectId);
        // Erst die Prüfungen in der eigenen Datenbank, dann der Aufruf beim Employee-Service,
        // damit der fremde Service nur gefragt wird, wenn die Zuweisung sonst möglich wäre
        ensureNotAssigned(dto.employeeId(), projectId);
        ensureNoOverlap(dto.employeeId(), project);

        String qualification = ensureHasQualification(dto.employeeId(), dto.qualification());

        ProjectEmployeeEntity entity = mapper.toEntity(dto);
        entity.setProject(project);
        entity.setQualification(qualification);
        repository.save(entity);

        return toProjectEmployeesDto(project);
    }

    private ProjectEmployeesDto toProjectEmployeesDto(ProjectEntity project) {
        List<ProjectEmployeeDto> employees = repository.findAllByProjectId(project.getId()).stream()
                .map(mapper::toDto)
                .toList();
        return new ProjectEmployeesDto(project.getId(), project.getName(), employees);
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

    // Gespeichert wird die Schreibweise aus dem Employee-Service, damit „java“ und
    // „Java“ einheitlich erscheinen.
    private String ensureHasQualification(Long employeeId, String qualification) {
        EmployeeDto employee = employeeClient.findById(employeeId);
        return employee.skillSet().stream()
                .map(SkillDto::skill)
                .filter(skill -> skill.equalsIgnoreCase(qualification))
                .findFirst()
                .orElseThrow(() -> new QualificationMissingException(employeeId, qualification));
    }

    @Transactional(readOnly = true)
    public ProjectEmployeesDto findByProject(Long projectId) {
        return toProjectEmployeesDto(loadProject(projectId));
    }

    @Transactional
    public ProjectEmployeesDto remove(Long projectId, Long employeeId) {
        ProjectEntity project = loadProject(projectId);
        ProjectEmployeeEntity assignment = repository.findByProjectIdAndEmployeeId(projectId, employeeId)
                .orElseThrow(() -> new ProjectEmployeeNotFoundException(employeeId, projectId));
        repository.delete(assignment);
        // Hibernate schreibt das Löschen vor der nächsten Abfrage in die Datenbank,
        // deshalb enthält die zurückgegebene Liste den Mitarbeiter nicht mehr
        return toProjectEmployeesDto(project);
    }

    @Transactional(readOnly = true)
    public EmployeeProjectsDto findProjectsOfEmployee(Long employeeId) {
        // Nur Existenzprüfung (A_3): ohne sie bekäme ein unbekannter Mitarbeiter eine leere Liste,
        // und man könnte ihn nicht von einem Mitarbeiter ohne Projekte (A_2) unterscheiden
        employeeClient.findById(employeeId);
        List<EmployeeProjectDto> projects = repository.findAllByEmployeeId(employeeId).stream()
                .map(mapper::toEmployeeProjectDto)
                .toList();
        return new EmployeeProjectsDto(employeeId, projects);
    }
}

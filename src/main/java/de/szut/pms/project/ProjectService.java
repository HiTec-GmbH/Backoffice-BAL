
package de.szut.pms.project;

import java.util.List;

import de.szut.pms.customer.CustomerClient;
import de.szut.pms.customer.CustomerNotFoundException;
import de.szut.pms.employee.EmployeeClient;
import de.szut.pms.employee.EmployeeOverlapException;
import de.szut.pms.projectemployee.ProjectEmployeeDto;
import de.szut.pms.projectemployee.ProjectEmployeeEntity;
import de.szut.pms.projectemployee.ProjectEmployeeMapper;
import de.szut.pms.projectemployee.ProjectEmployeeRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    private final ProjectRepository repository;
    private final ProjectMapper mapper;
    private final EmployeeClient employeeClient;
    private final CustomerClient customerClient;
    private final ProjectEmployeeRepository projectEmployeeRepository;
    private final ProjectEmployeeMapper projectEmployeeMapper;

    public ProjectService(
            ProjectRepository repository,
            ProjectMapper mapper,
            EmployeeClient employeeClient,
            CustomerClient customerClient,
            ProjectEmployeeRepository projectEmployeeRepository,
            ProjectEmployeeMapper projectEmployeeMapper) {

        this.repository = repository;
        this.mapper = mapper;
        this.employeeClient = employeeClient;
        this.customerClient = customerClient;
        this.projectEmployeeRepository = projectEmployeeRepository;
        this.projectEmployeeMapper = projectEmployeeMapper;
    }

    @Transactional
    public ProjectDto create(CreateProjectDto dto) {

        if (dto.endDate().isBefore(dto.startDate())) {
            throw new InvalidProjectException(
                    "endDate",
                    "Das Enddatum darf nicht vor dem Startdatum liegen."
            );
        }

        // Nur Existenzprüfung: wirft EmployeeNotFoundException (→ 404), wenn es den Projektleiter nicht gibt
        employeeClient.findById(dto.projectLeaderId());

        if (!customerClient.existsById(dto.customerId())) {
            throw new CustomerNotFoundException(dto.customerId());
        }

        ProjectEntity entity = mapper.toEntity(dto);

        ProjectEntity saved = repository.save(entity);

        return toProjectDto(saved);
    }

    @Transactional(readOnly = true)
    public ProjectDto findById(Long projectId) {

        ProjectEntity project = repository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(
                        "Es existiert kein Projekt mit der ID "
                                + projectId + "."
                ));

        return toProjectDto(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> findAll() {

        return repository.findAll()
                .stream()
                .map(this::toProjectDto)
                .toList();
    }

    @Transactional
    public ProjectDto update(Long projectId, UpdateProjectDto dto) {

        ProjectEntity project = repository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(
                        "Es existiert kein Projekt mit der ID "
                                + projectId + "."
                ));

        if (dto.endDate().isBefore(dto.startDate())) {
            throw new InvalidProjectException(
                    "endDate",
                    "Das Enddatum darf nicht vor dem Startdatum liegen."
            );
        }

        // Nur Existenzprüfung: wirft EmployeeNotFoundException (→ 404), wenn es den Projektleiter nicht gibt
        employeeClient.findById(dto.projectLeaderId());

        if (!customerClient.existsById(dto.customerId())) {
            throw new CustomerNotFoundException(dto.customerId());
        }

        checkEmployeeOverlaps(project, dto);

        // Erst nach allen erfolgreichen Prüfungen Daten ändern
        mapper.updateEntity(project, dto);

        ProjectEntity updated = repository.save(project);

        return toProjectDto(updated);
    }

    // Prüft mit dem neuen Zeitraum, ob ein bereits zugeordneter Mitarbeiter dadurch mit einem
    // anderen Projekt kollidiert (BAL-006 A_6). Das eigene Projekt wird dabei ausgeschlossen.
    private void checkEmployeeOverlaps(
            ProjectEntity project,
            UpdateProjectDto dto) {

        List<ProjectEmployeeEntity> assignments =
                projectEmployeeRepository.findAllByProjectId(project.getId());

        for (ProjectEmployeeEntity assignment : assignments) {

            List<ProjectEmployeeEntity> overlaps =
                    projectEmployeeRepository.findOverlapping(
                            assignment.getEmployeeId(),
                            project.getId(),
                            dto.startDate(),
                            dto.endDate()
                    );

            if (!overlaps.isEmpty()) {

                ProjectEntity conflictingProject =
                        overlaps.get(0).getProject();

                throw new EmployeeOverlapException(
                        assignment.getEmployeeId(),
                        conflictingProject
                );
            }
        }
    }

    @Transactional
    public void delete(Long projectId) {

        ProjectEntity project = repository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(
                        "Es existiert kein Projekt mit der ID "
                                + projectId + "."
                ));

        List<ProjectEmployeeEntity> assignments =
                projectEmployeeRepository.findAllByProjectId(projectId);

        projectEmployeeRepository.deleteAll(assignments);

        // flush, damit die Zuordnungen vor dem Projekt gelöscht werden. Sonst kann Hibernate
        // das Projekt zuerst löschen, und der Fremdschlüssel in project_employee schlägt fehl.
        projectEmployeeRepository.flush();

        repository.delete(project);
    }

    private ProjectDto toProjectDto(ProjectEntity project) {

        List<ProjectEmployeeDto> projectEmployees =
                projectEmployeeRepository.findAllByProjectId(project.getId())
                        .stream()
                        .map(projectEmployeeMapper::toDto)
                        .toList();

        return mapper.toDto(project, projectEmployees);
    }
}

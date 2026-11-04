
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

        // Zeitraum überprüfen
        if (dto.endDate().isBefore(dto.startDate())) {
            throw new InvalidProjectException(
                    "endDate",
                    "Das Enddatum darf nicht vor dem Startdatum liegen."
            );
        }

        // Projektleiter überprüfen
        employeeClient.findById(dto.projectLeaderId());

        // Kunden überprüfen
        if (!customerClient.existsById(dto.customerId())) {
            throw new CustomerNotFoundException(dto.customerId());
        }

        // DTO in Entity umwandeln
        ProjectEntity entity = mapper.toEntity(dto);

        // Projekt speichern
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

    // Projektdaten aktualisieren
    @Transactional
    public ProjectDto update(Long projectId, UpdateProjectDto dto) {

        // Bestehendes Projekt laden
        ProjectEntity project = repository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(
                        "Es existiert kein Projekt mit der ID "
                                + projectId + "."
                ));

        // Zeitraum überprüfen
        if (dto.endDate().isBefore(dto.startDate())) {
            throw new InvalidProjectException(
                    "endDate",
                    "Das Enddatum darf nicht vor dem Startdatum liegen."
            );
        }

        // Neue Projektleiter-ID überprüfen
        employeeClient.findById(dto.projectLeaderId());

        // Neue Kunden-ID überprüfen
        if (!customerClient.existsById(dto.customerId())) {
            throw new CustomerNotFoundException(dto.customerId());
        }

        // Prüfen, ob der neue Zeitraum zu Doppelbelegungen führt
        checkEmployeeOverlaps(project, dto);

        // Erst nach allen erfolgreichen Prüfungen Daten ändern
        mapper.updateEntity(project, dto);

        // Geändertes Projekt speichern
        ProjectEntity updated = repository.save(project);

        // Vollständiges Projekt inklusive Mitarbeiter zurückgeben
        return toProjectDto(updated);
    }

    // Prüft alle Mitarbeiter des Projekts auf zeitliche Überschneidungen
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

    // Gemeinsame Hilfsmethode für ein vollständiges ProjectDto
    private ProjectDto toProjectDto(ProjectEntity project) {

        List<ProjectEmployeeDto> projectEmployees =
                projectEmployeeRepository.findAllByProjectId(project.getId())
                        .stream()
                        .map(projectEmployeeMapper::toDto)
                        .toList();

        return mapper.toDto(project, projectEmployees);
    }
}

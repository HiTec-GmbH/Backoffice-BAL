package de.szut.pms.project;

import java.util.List;

import de.szut.pms.customer.CustomerClient;
import de.szut.pms.customer.CustomerNotFoundException;
import de.szut.pms.employee.EmployeeClient;
import de.szut.pms.projectemployee.ProjectEmployeeDto;
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

        // Projekt mit Projektmitarbeitern als DTO zurückgeben
        return toProjectDto(saved);
    }

    @Transactional(readOnly = true)
    public ProjectDto findById(Long projectId) {

        // Projekt anhand der ID suchen
        ProjectEntity project = repository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(
                        "Es existiert kein Projekt mit der ID "
                                + projectId + "."
                ));

        return toProjectDto(project);
    }

    // Alle Projekte abrufen
    @Transactional(readOnly = true)
    public List<ProjectDto> findAll() {

        return repository.findAll()
                .stream()
                .map(this::toProjectDto)
                .toList();
    }

    // Gemeinsame Hilfsmethode für die Umwandlung
    // eines Projekts inklusive seiner Mitarbeiter
    private ProjectDto toProjectDto(ProjectEntity project) {

        // Mitarbeiterzuordnungen aus der Datenbank laden
        List<ProjectEmployeeDto> projectEmployees =
                projectEmployeeRepository.findAllByProjectId(project.getId())
                        .stream()
                        .map(projectEmployeeMapper::toDto)
                        .toList();

        // Projektdaten und Mitarbeiterliste zusammenführen
        return mapper.toDto(project, projectEmployees);
    }
}
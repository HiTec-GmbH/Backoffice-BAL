package de.szut.pms.project;

import de.szut.pms.customer.CustomerClient;
import de.szut.pms.customer.CustomerNotFoundException;
import de.szut.pms.employee.EmployeeClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    private final ProjectRepository repository;
    private final ProjectMapper mapper;
    private final EmployeeClient employeeClient;
    private final CustomerClient customerClient;

    public ProjectService(
            ProjectRepository repository,
            ProjectMapper mapper,
            EmployeeClient employeeClient,
            CustomerClient customerClient) {

        this.repository = repository;
        this.mapper = mapper;
        this.employeeClient = employeeClient;
        this.customerClient = customerClient;
    }

    @Transactional
    public ProjectDto create(CreateProjectDto dto) {

        // 1. Zeitraum überprüfen
        if (dto.endDate().isBefore(dto.startDate())) {
            throw new InvalidProjectException(
                    "endDate",
                    "Das Enddatum darf nicht vor dem Startdatum liegen."
            );
        }

        employeeClient.findById(dto.projectLeaderId());

        // 3. Kunden überprüfen
        if (!customerClient.existsById(dto.customerId())) {
            throw new CustomerNotFoundException(dto.customerId());
        }

        // 4. DTO in Entity umwandeln
        ProjectEntity entity = mapper.toEntity(dto);

        // 5. Projekt speichern
        ProjectEntity saved = repository.save(entity);

        // 6. Gespeichertes Projekt als DTO zurückgeben
        return mapper.toDto(saved);
    }
}
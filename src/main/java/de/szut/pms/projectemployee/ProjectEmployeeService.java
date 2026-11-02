package de.szut.pms.projectemployee;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.szut.pms.employee.EmployeeClient;
import de.szut.pms.employee.EmployeeDto;
import de.szut.pms.employee.QualificationMissingException;

@Service
public class ProjectEmployeeService {

    private final ProjectEmployeeRepository repository;
    private final ProjectEmployeeMapper mapper;
    private final EmployeeClient employeeClient;

    public ProjectEmployeeService(ProjectEmployeeRepository repository, ProjectEmployeeMapper mapper, EmployeeClient employeeClient) {
        this.repository = repository;
        this.mapper = mapper;
        this.employeeClient = employeeClient;
    }

    @Transactional
    public ProjectEmployeeDto create(CreateProjectEmployeeDto dto) {
        EmployeeDto employee = employeeClient.findById(dto.employeeId());

        boolean hasQualification = employee.skillSet().stream()
                .anyMatch(s -> s.skill().equalsIgnoreCase(dto.qualification()));
        if (!hasQualification) {
            throw new QualificationMissingException(dto.employeeId(), dto.qualification());
        }

        ProjectEmployeeEntity saved = repository.save(mapper.toEntity(dto));
        return mapper.toDto(saved);
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

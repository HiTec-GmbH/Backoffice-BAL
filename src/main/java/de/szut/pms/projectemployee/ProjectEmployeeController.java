package de.szut.pms.projectemployee;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/projects/{projectId}/employees")
public class ProjectEmployeeController {

    private final ProjectEmployeeService service;

    public ProjectEmployeeController(ProjectEmployeeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProjectEmployeesDto> create(@PathVariable Long projectId,
            @RequestBody @Valid CreateProjectEmployeeDto dto) {
        ProjectEmployeesDto result = service.create(projectId, dto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .build()
                .toUri();

        return ResponseEntity.created(location).body(result);
    }

    @GetMapping
    public ResponseEntity<ProjectEmployeesDto> findByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(service.findByProject(projectId));
    }

    @DeleteMapping("/{employeeId}")
    public ResponseEntity<ProjectEmployeesDto> remove(@PathVariable Long projectId, @PathVariable Long employeeId) {
        return ResponseEntity.ok(service.remove(projectId, employeeId));
    }
}

package de.szut.pms.projectemployee;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employees/{employeeId}/projects")
public class EmployeeProjectController {

    private final ProjectEmployeeService service;

    public EmployeeProjectController(ProjectEmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<EmployeeProjectsDto> findProjectsOfEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(service.findProjectsOfEmployee(employeeId));
    }
}

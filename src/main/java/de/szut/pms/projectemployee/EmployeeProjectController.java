package de.szut.pms.projectemployee;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Mitarbeiter", description = "Projekte eines Mitarbeiters abfragen")
@RestController
@RequestMapping("/employees/{employeeId}/projects")
public class EmployeeProjectController {

    private final ProjectEmployeeService service;

    public EmployeeProjectController(ProjectEmployeeService service) {
        this.service = service;
    }

    @Operation(summary = "Alle Projekte eines Mitarbeiters abrufen")
    @ApiResponse(responseCode = "200", description = "Projekte mit Zeitraum und Qualifikation, ggf. leere Liste")
    @ApiResponse(responseCode = "401", description = "Kein oder ungültiger Token")
    @ApiResponse(responseCode = "404", description = "Mitarbeiter existiert im Employee-Service nicht")
    @ApiResponse(responseCode = "503", description = "Employee-Service nicht erreichbar")
    @GetMapping
    public ResponseEntity<EmployeeProjectsDto> findProjectsOfEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(service.findProjectsOfEmployee(employeeId));
    }
}

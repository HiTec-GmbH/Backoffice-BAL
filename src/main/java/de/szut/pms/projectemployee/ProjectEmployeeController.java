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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Projektmitarbeiter", description = "Mitarbeiter einem Projekt zuweisen, abrufen und entfernen")
@RestController
@RequestMapping("/projects/{projectId}/employees")
public class ProjectEmployeeController {

    private final ProjectEmployeeService service;

    public ProjectEmployeeController(ProjectEmployeeService service) {
        this.service = service;
    }

    @Operation(summary = "Mitarbeiter einem Projekt zuweisen", description = "Prüft Projekt, Mitarbeiter, Qualifikation und Überschneidungen und gibt alle Projektmitarbeiter zurück.")
    @ApiResponse(responseCode = "201", description = "Mitarbeiter zugewiesen")
    @ApiResponse(responseCode = "400", description = "Eingabe ungültig oder Mitarbeiter hat die Qualifikation nicht")
    @ApiResponse(responseCode = "401", description = "Kein oder ungültiger Token")
    @ApiResponse(responseCode = "404", description = "Projekt oder Mitarbeiter existiert nicht")
    @ApiResponse(responseCode = "409", description = "Bereits zugeordnet oder zeitliche Überschneidung mit einem anderen Projekt")
    @ApiResponse(responseCode = "503", description = "Employee-Service nicht erreichbar")
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

    @Operation(summary = "Alle Mitarbeiter eines Projekts abrufen")
    @ApiResponse(responseCode = "200", description = "Projekt mit seinen Mitarbeitern, ggf. leere Liste")
    @ApiResponse(responseCode = "401", description = "Kein oder ungültiger Token")
    @ApiResponse(responseCode = "404", description = "Projekt existiert nicht")
    @GetMapping
    public ResponseEntity<ProjectEmployeesDto> findByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(service.findByProject(projectId));
    }

    @Operation(summary = "Mitarbeiter aus einem Projekt entfernen")
    @ApiResponse(responseCode = "200", description = "Verbleibende Mitarbeiter des Projekts")
    @ApiResponse(responseCode = "401", description = "Kein oder ungültiger Token")
    @ApiResponse(responseCode = "404", description = "Projekt existiert nicht oder Mitarbeiter ist nicht zugeordnet")
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<ProjectEmployeesDto> remove(@PathVariable Long projectId, @PathVariable Long employeeId) {
        return ResponseEntity.ok(service.remove(projectId, employeeId));
    }
}

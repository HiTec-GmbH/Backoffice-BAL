
package de.szut.pms.project;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Projekte", description = "Projekte anlegen, abrufen, ändern und löschen")
@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @Operation(summary = "Projekt anlegen", description = "Prüft Pflichtfelder, Zeitraum, Projektleiter und Kunde und legt das Projekt an.")
    @ApiResponse(responseCode = "201", description = "Projekt angelegt")
    @ApiResponse(responseCode = "400", description = "Pflichtangabe fehlt oder Enddatum liegt vor dem Startdatum")
    @ApiResponse(responseCode = "401", description = "Kein oder ungültiger Token")
    @ApiResponse(responseCode = "404", description = "Projektleiter oder Kunde existiert nicht")
    @ApiResponse(responseCode = "503", description = "Employee-Service nicht erreichbar")
    @PostMapping
    public ResponseEntity<ProjectDto> create(
            @RequestBody @Valid CreateProjectDto dto) {

        ProjectDto created = service.create(dto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Projekt mit allen Daten abrufen")
    @ApiResponse(responseCode = "200", description = "Projekt inklusive Projektmitarbeitern")
    @ApiResponse(responseCode = "401", description = "Kein oder ungültiger Token")
    @ApiResponse(responseCode = "404", description = "Projekt existiert nicht")
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> findById(
            @PathVariable Long id) {

        ProjectDto project = service.findById(id);

        return ResponseEntity.ok(project);
    }

    @Operation(summary = "Alle Projekte abrufen")
    @ApiResponse(responseCode = "200", description = "Alle Projekte inklusive Projektmitarbeitern, ggf. leere Liste")
    @ApiResponse(responseCode = "401", description = "Kein oder ungültiger Token")
    @GetMapping
    public ResponseEntity<List<ProjectDto>> findAll() {

        List<ProjectDto> projects = service.findAll();

        return ResponseEntity.ok(projects);
    }

    @Operation(summary = "Projektdaten ändern", description = "Prüft dieselben Regeln wie beim Anlegen und zusätzlich, ob der neue Zeitraum Mitarbeiter doppelt verplant.")
    @ApiResponse(responseCode = "200", description = "Projekt geändert")
    @ApiResponse(responseCode = "400", description = "Pflichtangabe fehlt oder Enddatum liegt vor dem Startdatum")
    @ApiResponse(responseCode = "401", description = "Kein oder ungültiger Token")
    @ApiResponse(responseCode = "404", description = "Projekt, Projektleiter oder Kunde existiert nicht")
    @ApiResponse(responseCode = "409", description = "Neuer Zeitraum führt zu einer Doppelbelegung eines Projektmitarbeiters")
    @ApiResponse(responseCode = "503", description = "Employee-Service nicht erreichbar")
    @PutMapping("/{id}")
    public ResponseEntity<ProjectDto> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateProjectDto dto) {

        ProjectDto updated = service.update(id, dto);

        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Projekt löschen", description = "Löscht das Projekt samt seinen Mitarbeiterzuordnungen, die Mitarbeiter sind danach wieder frei.")
    @ApiResponse(responseCode = "204", description = "Projekt gelöscht")
    @ApiResponse(responseCode = "401", description = "Kein oder ungültiger Token")
    @ApiResponse(responseCode = "404", description = "Projekt existiert nicht")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}

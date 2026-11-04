
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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    // Projekt anlegen
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

    // Einzelnes Projekt abrufen
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> findById(
            @PathVariable Long id) {

        ProjectDto project = service.findById(id);

        return ResponseEntity.ok(project);
    }

    // Alle Projekte abrufen
    @GetMapping
    public ResponseEntity<List<ProjectDto>> findAll() {

        List<ProjectDto> projects = service.findAll();

        return ResponseEntity.ok(projects);
    }

    // Projektdaten aktualisieren
    @PutMapping("/{id}")
    public ResponseEntity<ProjectDto> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateProjectDto dto) {

        ProjectDto updated = service.update(id, dto);

        return ResponseEntity.ok(updated);
    }

    // Projekt löschen
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}

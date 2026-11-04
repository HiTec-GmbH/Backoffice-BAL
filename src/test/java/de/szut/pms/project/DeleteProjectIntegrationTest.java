
package de.szut.pms.project;

import java.time.LocalDate;
import java.util.List;

import de.szut.pms.employee.EmployeeClient;
import de.szut.pms.employee.EmployeeDto;
import de.szut.pms.employee.SkillDto;
import de.szut.pms.projectemployee.ProjectEmployeeEntity;
import de.szut.pms.testcontainers.AbstractIntegrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DeleteProjectIntegrationTest extends AbstractIntegrationTest {

    @MockitoBean
    private EmployeeClient employeeClient;

    @BeforeEach
    void prepare() {
        // Mitarbeiter 5 existiert und besitzt die Qualifikation Java
        when(employeeClient.findById(5L))
                .thenReturn(new EmployeeDto(
                        5L,
                        List.of(new SkillDto(1L, "Java"))
                ));

        // Mitarbeiter 8 existiert und besitzt die Qualifikation Testing
        when(employeeClient.findById(8L))
                .thenReturn(new EmployeeDto(
                        8L,
                        List.of(new SkillDto(2L, "Testing"))
                ));
    }

    // T3: Projekt erfolgreich löschen
    @Test
    @WithMockUser
    void deleteProjectSuccessfully() throws Exception {

        // 1. Projekt anlegen
        ProjectEntity project = saveProject(
                "Backoffice-Projekt",
                "2027-01-01",
                "2027-03-31"
        );

        // 2. Zwei Mitarbeiter zuweisen
        saveAssignment(project, 5L, "Java");
        saveAssignment(project, 8L, "Testing");

        Long projectId = project.getId();

        // 3. Vor dem Löschen: BAL-010 zeigt das Projekt bei Mitarbeiter 5
        mockMvc.perform(get("/employees/{employeeId}/projects", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(5))
                .andExpect(jsonPath("$.projects.length()").value(1))
                .andExpect(jsonPath("$.projects[0].projectId").value(projectId))
                .andExpect(jsonPath("$.projects[0].projectName")
                        .value("Backoffice-Projekt"))
                .andExpect(jsonPath("$.projects[0].qualification")
                        .value("Java"));

        // 4. Projekt löschen
        mockMvc.perform(delete("/projects/{id}", projectId)
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // 5. Prüfen, ob das Projekt aus der Datenbank verschwunden ist
        assertThat(projectRepository.findById(projectId)).isEmpty();

        // 6. Prüfen, ob alle Mitarbeiterzuordnungen gelöscht wurden
        assertThat(projectEmployeeRepository
                .findAllByProjectId(projectId))
                .isEmpty();

        // 7. Projekt darf nicht mehr in der Projektliste erscheinen
        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // 8. Projekt darf auch über seine ID nicht mehr abrufbar sein
        mockMvc.perform(get("/projects/{id}", projectId))
                .andExpect(status().isNotFound());

        // 9. BAL-010 darf das gelöschte Projekt nicht mehr anzeigen
        mockMvc.perform(get("/employees/{employeeId}/projects", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(5))
                .andExpect(jsonPath("$.projects").isEmpty());

        mockMvc.perform(get("/employees/{employeeId}/projects", 8L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(8))
                .andExpect(jsonPath("$.projects").isEmpty());

        // 10. Auch die Repository-Abfrage darf keine alten Zuordnungen finden
        assertThat(projectEmployeeRepository.findAllByEmployeeId(5L))
                .isEmpty();

        assertThat(projectEmployeeRepository.findAllByEmployeeId(8L))
                .isEmpty();
    }

    // T4: Mitarbeiter nach dem Löschen wieder einem anderen
    // Projekt im gleichen Zeitraum zuweisen
    @Test
    @WithMockUser
    void deletedProjectsEmployeesCanBeAssignedAgain() throws Exception {

        // 1. Zwei Projekte mit identischem Zeitraum anlegen
        ProjectEntity projectA = saveProject(
                "Projekt A",
                "2027-01-01",
                "2027-03-31"
        );

        ProjectEntity projectB = saveProject(
                "Projekt B",
                "2027-01-01",
                "2027-03-31"
        );

        // 2. Mitarbeiter 5 ist zunächst Projekt A zugewiesen
        saveAssignment(projectA, 5L, "Java");

        // 3. Projekt A löschen
        mockMvc.perform(delete("/projects/{id}", projectA.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // 4. Prüfen, dass BAL-010 keine alte Projektzuordnung mehr liefert
        mockMvc.perform(get("/employees/{employeeId}/projects", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projects").isEmpty());

        // 5. Mitarbeiter 5 nun Projekt B zuweisen
        // Der Zeitraum ist identisch. Die Zuweisung muss trotzdem funktionieren.
        mockMvc.perform(post("/projects/{id}/employees", projectB.getId())
                        .content("""
                                {
                                    "qualification": "Java",
                                    "employeeId": 5
                                }
                                """)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectId").value(projectB.getId()))
                .andExpect(jsonPath("$.projectName").value("Projekt B"))
                .andExpect(jsonPath("$.employees.length()").value(1))
                .andExpect(jsonPath("$.employees[0].employeeId").value(5))
                .andExpect(jsonPath("$.employees[0].qualification")
                        .value("Java"));

        // 6. Prüfen, ob die neue Zuordnung wirklich gespeichert wurde
        assertThat(projectEmployeeRepository.findAllByEmployeeId(5L))
                .hasSize(1);

        assertThat(projectEmployeeRepository
                .findByProjectIdAndEmployeeId(projectB.getId(), 5L))
                .isPresent();

        // 7. BAL-010 muss jetzt Projekt B anzeigen
        mockMvc.perform(get("/employees/{employeeId}/projects", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(5))
                .andExpect(jsonPath("$.projects.length()").value(1))
                .andExpect(jsonPath("$.projects[0].projectId")
                        .value(projectB.getId()))
                .andExpect(jsonPath("$.projects[0].projectName")
                        .value("Projekt B"))
                .andExpect(jsonPath("$.projects[0].startDate")
                        .value("2027-01-01"))
                .andExpect(jsonPath("$.projects[0].endDate")
                        .value("2027-03-31"))
                .andExpect(jsonPath("$.projects[0].qualification")
                        .value("Java"));

        // 8. Das alte Projekt darf weiterhin nicht existieren
        assertThat(projectRepository.findById(projectA.getId()))
                .isEmpty();
    }

    // T4: Unbekanntes Projekt löschen
    @Test
    @WithMockUser
    void deleteUnknownProjectReturns404() throws Exception {

        // 1. Ein existierendes Projekt vorbereiten
        ProjectEntity project = saveProject(
                "Bestehendes Projekt",
                "2027-01-01",
                "2027-03-31"
        );

        saveAssignment(project, 5L, "Java");

        // 2. Nicht existierendes Projekt löschen
        mockMvc.perform(delete("/projects/{id}", 999999L)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        // 3. Existierendes Projekt muss erhalten bleiben
        assertThat(projectRepository.findById(project.getId()))
                .isPresent();

        // 4. Auch seine Mitarbeiterzuordnung muss erhalten bleiben
        assertThat(projectEmployeeRepository
                .findAllByProjectId(project.getId()))
                .hasSize(1);
    }

    // Hilfsmethode zum Erstellen eines Projekts
    private ProjectEntity saveProject(
            String name,
            String startDate,
            String endDate) {

        ProjectEntity project = new ProjectEntity();

        project.setName(name);
        project.setCustomerId(100L);
        project.setCustomerContact("Max Mustermann");
        project.setProjectLeaderId(1L);
        project.setComment("Testprojekt");

        project.setStartDate(LocalDate.parse(startDate));
        project.setEndDate(LocalDate.parse(endDate));

        return projectRepository.save(project);
    }

    // Hilfsmethode zum Erstellen einer Mitarbeiterzuordnung
    private void saveAssignment(
            ProjectEntity project,
            Long employeeId,
            String qualification) {

        ProjectEmployeeEntity assignment =
                new ProjectEmployeeEntity(qualification, employeeId);

        assignment.setProject(project);

        projectEmployeeRepository.save(assignment);
    }
}

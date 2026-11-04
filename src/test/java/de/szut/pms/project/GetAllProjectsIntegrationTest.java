
package de.szut.pms.project;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import org.springframework.security.test.context.support.WithMockUser;

import de.szut.pms.projectemployee.ProjectEmployeeEntity;
import de.szut.pms.testcontainers.AbstractIntegrationTest;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GetAllProjectsIntegrationTest extends AbstractIntegrationTest {

    // T3: Happy Path – mehrere Projekte inklusive Mitarbeiter abrufen
    @Test
    @WithMockUser
    void getAllProjectsReturnsAllProjectsWithEmployees() throws Exception {

        // 1. Zwei Projekte in der Datenbank anlegen
        ProjectEntity projectA = saveProject(
                "Backoffice-Erweiterung",
                100L,
                "Max Mustermann",
                1L,
                "Erweiterung der Projektverwaltung",
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2027, 3, 31)
        );

        ProjectEntity projectB = saveProject(
                "Webseitenentwicklung",
                200L,
                "Erika Musterfrau",
                2L,
                "Neue Unternehmenswebseite",
                LocalDate.of(2027, 4, 1),
                LocalDate.of(2027, 7, 31)
        );

        // 2. Projektmitarbeiter zuweisen
        saveAssignment(projectA, 5L, "Java");
        saveAssignment(projectA, 8L, "Testing");

        saveAssignment(projectB, 12L, "SQL");

        // 3. Alle Projekte über die REST-Schnittstelle abrufen
        mockMvc.perform(get("/projects"))

                .andExpect(status().isOk())

                // 4. Anzahl und Namen der Projekte überprüfen
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].name",
                        containsInAnyOrder(
                                "Backoffice-Erweiterung",
                                "Webseitenentwicklung"
                        )))

                // 5. Erstes Projekt überprüfen
                .andExpect(jsonPath(
                        "$[?(@.name == 'Backoffice-Erweiterung')].id",
                        contains(projectA.getId().intValue())))
                .andExpect(jsonPath(
                        "$[?(@.name == 'Backoffice-Erweiterung')].customerId",
                        contains(100)))
                .andExpect(jsonPath(
                        "$[?(@.name == 'Backoffice-Erweiterung')].customerContact",
                        contains("Max Mustermann")))
                .andExpect(jsonPath(
                        "$[?(@.name == 'Backoffice-Erweiterung')].projectLeaderId",
                        contains(1)))
                .andExpect(jsonPath(
                        "$[?(@.name == 'Backoffice-Erweiterung')].comment",
                        contains("Erweiterung der Projektverwaltung")))
                .andExpect(jsonPath(
                        "$[?(@.name == 'Backoffice-Erweiterung')].startDate",
                        contains("2026-11-01")))
                .andExpect(jsonPath(
                        "$[?(@.name == 'Backoffice-Erweiterung')].endDate",
                        contains("2027-03-31")))

                // Mitarbeiter des ersten Projekts überprüfen
                .andExpect(jsonPath(
                        "$[?(@.name == 'Backoffice-Erweiterung')].projectEmployees[*].employeeId",
                        containsInAnyOrder(5, 8)))
                .andExpect(jsonPath(
                        "$[?(@.name == 'Backoffice-Erweiterung')].projectEmployees[*].qualification",
                        containsInAnyOrder("Java", "Testing")))

                // 6. Zweites Projekt überprüfen
                .andExpect(jsonPath(
                        "$[?(@.name == 'Webseitenentwicklung')].id",
                        contains(projectB.getId().intValue())))
                .andExpect(jsonPath(
                        "$[?(@.name == 'Webseitenentwicklung')].customerId",
                        contains(200)))
                .andExpect(jsonPath(
                        "$[?(@.name == 'Webseitenentwicklung')].customerContact",
                        contains("Erika Musterfrau")))
                .andExpect(jsonPath(
                        "$[?(@.name == 'Webseitenentwicklung')].projectLeaderId",
                        contains(2)))
                .andExpect(jsonPath(
                        "$[?(@.name == 'Webseitenentwicklung')].comment",
                        contains("Neue Unternehmenswebseite")))
                .andExpect(jsonPath(
                        "$[?(@.name == 'Webseitenentwicklung')].startDate",
                        contains("2027-04-01")))
                .andExpect(jsonPath(
                        "$[?(@.name == 'Webseitenentwicklung')].endDate",
                        contains("2027-07-31")))

                // Mitarbeiter des zweiten Projekts überprüfen
                .andExpect(jsonPath(
                        "$[?(@.name == 'Webseitenentwicklung')].projectEmployees[*].employeeId",
                        contains(12)))
                .andExpect(jsonPath(
                        "$[?(@.name == 'Webseitenentwicklung')].projectEmployees[*].qualification",
                        contains("SQL")));
    }

    // T4: Keine Projekte vorhanden
    @Test
    @WithMockUser
    void getAllProjectsReturnsEmptyListWhenNoProjectsExist() throws Exception {

        // Sicherstellen, dass keine Projekte vorhanden sind
        org.assertj.core.api.Assertions
                .assertThat(projectRepository.findAll())
                .isEmpty();

        // Alle Projekte abrufen
        mockMvc.perform(get("/projects"))

                // Trotzdem HTTP 200
                .andExpect(status().isOk())

                // Die Antwort ist eine leere Liste
                .andExpect(content().json("[]"));
    }

    // Hilfsmethode zum Erstellen eines Projekts
    private ProjectEntity saveProject(
            String name,
            Long customerId,
            String customerContact,
            Long projectLeaderId,
            String comment,
            LocalDate startDate,
            LocalDate endDate) {

        ProjectEntity project = new ProjectEntity();

        project.setName(name);
        project.setCustomerId(customerId);
        project.setCustomerContact(customerContact);
        project.setProjectLeaderId(projectLeaderId);
        project.setComment(comment);
        project.setStartDate(startDate);
        project.setEndDate(endDate);

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

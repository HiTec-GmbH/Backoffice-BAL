
package de.szut.pms.project;

import java.time.LocalDate;
import java.util.List;

import de.szut.pms.customer.CustomerClient;
import de.szut.pms.employee.EmployeeClient;
import de.szut.pms.employee.EmployeeDto;
import de.szut.pms.employee.EmployeeNotFoundException;
import de.szut.pms.projectemployee.ProjectEmployeeEntity;
import de.szut.pms.testcontainers.AbstractIntegrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UpdateProjectIntegrationTest extends AbstractIntegrationTest {

    @MockitoBean
    private EmployeeClient employeeClient;

    @MockitoBean
    private CustomerClient customerClient;

    @BeforeEach
    void prepare() {
        // Existierende Projektleiter simulieren
        when(employeeClient.findById(1L))
                .thenReturn(new EmployeeDto(1L, List.of()));

        when(employeeClient.findById(2L))
                .thenReturn(new EmployeeDto(2L, List.of()));

        // Existierende Kunden simulieren
        when(customerClient.existsById(100L)).thenReturn(true);
        when(customerClient.existsById(200L)).thenReturn(true);
    }

    // T4: Erfolgreiches Aktualisieren eines Projekts
    @Test
    @WithMockUser
    void updateProjectSuccessfully() throws Exception {

        // 1. Bestehendes Projekt vorbereiten
        ProjectEntity project = saveProject();

        // 2. Mitarbeiterzuordnung vorbereiten
        saveAssignment(project, 5L, "Java");

        // 3. Neue Projektdaten senden
        String content = projectJson(
                "Aktualisiertes Projekt",
                200L,
                2L,
                "Neuer Ansprechpartner",
                "Aktualisiertes Projektziel",
                "2026-11-01",
                "2027-04-30"
        );

        // 4. Projekt aktualisieren
        mockMvc.perform(put("/projects/{id}", project.getId())
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(project.getId()))
                .andExpect(jsonPath("$.name")
                        .value("Aktualisiertes Projekt"))
                .andExpect(jsonPath("$.customerId").value(200))
                .andExpect(jsonPath("$.customerContact")
                        .value("Neuer Ansprechpartner"))
                .andExpect(jsonPath("$.projectLeaderId").value(2))
                .andExpect(jsonPath("$.comment")
                        .value("Aktualisiertes Projektziel"))
                .andExpect(jsonPath("$.startDate").value("2026-11-01"))
                .andExpect(jsonPath("$.endDate").value("2027-04-30"))
                .andExpect(jsonPath("$.projectEmployees[0].employeeId").value(5))
                .andExpect(jsonPath("$.projectEmployees[0].qualification")
                        .value("Java"));

        // 5. Prüfen, ob die Änderung dauerhaft gespeichert wurde
        mockMvc.perform(get("/projects/{id}", project.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Aktualisiertes Projekt"))
                .andExpect(jsonPath("$.customerId").value(200))
                .andExpect(jsonPath("$.customerContact")
                        .value("Neuer Ansprechpartner"))
                .andExpect(jsonPath("$.projectLeaderId").value(2))
                .andExpect(jsonPath("$.endDate").value("2027-04-30"));
    }

    // T5: Projekt existiert nicht
    @Test
    @WithMockUser
    void updateUnknownProjectReturns404() throws Exception {

        Long unknownProjectId = 999999L;

        String content = projectJson(
                "Aktualisiertes Projekt",
                100L,
                1L,
                "Max Mustermann",
                "Neues Projektziel",
                "2026-11-01",
                "2027-03-31"
        );

        mockMvc.perform(put("/projects/{id}", unknownProjectId)
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail")
                        .value("Es existiert kein Projekt mit der ID "
                                + unknownProjectId + "."));
    }

    // T5: Pflichtangabe fehlt
    @Test
    @WithMockUser
    void updateProjectWithoutRequiredNameReturns400() throws Exception {

        ProjectEntity project = saveProject();

        String content = """
                {
                    "customerId": 100,
                    "customerContact": "Neuer Ansprechpartner",
                    "projectLeaderId": 1,
                    "comment": "Neues Projektziel",
                    "startDate": "2026-11-01",
                    "endDate": "2027-03-31"
                }
                """;

        mockMvc.perform(put("/projects/{id}", project.getId())
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());

        // Die alten Daten müssen erhalten bleiben
        assertProjectUnchanged(project);
    }

    // T5: Enddatum liegt vor Startdatum
    @Test
    @WithMockUser
    void updateProjectWithInvalidDateRangeReturns400() throws Exception {

        ProjectEntity project = saveProject();

        String content = projectJson(
                "Aktualisiertes Projekt",
                100L,
                1L,
                "Neuer Ansprechpartner",
                "Neues Projektziel",
                "2027-03-31",
                "2026-11-01"
        );

        mockMvc.perform(put("/projects/{id}", project.getId())
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.endDate")
                        .value("Das Enddatum darf nicht vor dem Startdatum liegen."));

        assertProjectUnchanged(project);
    }

    // T5: Neue Projektleiter-ID existiert nicht
    @Test
    @WithMockUser
    void updateProjectWithUnknownProjectLeaderReturns404() throws Exception {

        ProjectEntity project = saveProject();

        when(employeeClient.findById(999L))
                .thenThrow(new EmployeeNotFoundException(999L));

        String content = projectJson(
                "Aktualisiertes Projekt",
                100L,
                999L,
                "Neuer Ansprechpartner",
                "Neues Projektziel",
                "2026-11-01",
                "2027-03-31"
        );

        mockMvc.perform(put("/projects/{id}", project.getId())
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail")
                        .value("Es existiert kein Mitarbeiter mit der ID 999."));

        assertProjectUnchanged(project);
    }

    // T5: Neue Kunden-ID existiert nicht
    @Test
    @WithMockUser
    void updateProjectWithUnknownCustomerReturns404() throws Exception {

        ProjectEntity project = saveProject();

        when(customerClient.existsById(999L)).thenReturn(false);

        String content = projectJson(
                "Aktualisiertes Projekt",
                999L,
                1L,
                "Neuer Ansprechpartner",
                "Neues Projektziel",
                "2026-11-01",
                "2027-03-31"
        );

        mockMvc.perform(put("/projects/{id}", project.getId())
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail")
                        .value("Es existiert kein Kunde mit der ID 999."));

        assertProjectUnchanged(project);
    }

    // T5: Neuer Zeitraum verursacht eine Doppelbelegung
    @Test
    @WithMockUser
    void updateProjectWithEmployeeOverlapReturns409() throws Exception {

        // 1. Erstes Projekt: bisher bis zum 31.03.2027
        ProjectEntity project = saveProject(
                "Backoffice-Projekt",
                "Max Mustermann",
                1L,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2027, 3, 31)
        );

        // 2. Zweites Projekt beginnt am Folgetag
        ProjectEntity otherProject = saveProject(
                "Folgeprojekt",
                "Erika Musterfrau",
                2L,
                LocalDate.of(2027, 4, 1),
                LocalDate.of(2027, 7, 31)
        );

        // 3. Derselbe Mitarbeiter arbeitet in beiden Projekten
        saveAssignment(project, 5L, "Java");
        saveAssignment(otherProject, 5L, "Java");

        // 4. Durch die Verlängerung entsteht eine Überschneidung
        String content = projectJson(
                "Backoffice-Projekt",
                100L,
                1L,
                "Max Mustermann",
                "Verlängertes Projekt",
                "2026-11-01",
                "2027-04-15"
        );

        mockMvc.perform(put("/projects/{id}", project.getId())
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail",
                        containsString("Der Mitarbeiter 5")))
                .andExpect(jsonPath("$.detail",
                        containsString("Folgeprojekt")));

        // 5. Das ursprüngliche Projekt muss unverändert bleiben
        ProjectEntity unchanged = projectRepository.findById(project.getId())
                .orElseThrow();

        assertThat(unchanged.getEndDate())
                .isEqualTo(LocalDate.of(2027, 3, 31));

        assertThat(unchanged.getName())
                .isEqualTo("Backoffice-Projekt");
    }

    // Hilfsmethode zum Erstellen eines Standardprojekts
    private ProjectEntity saveProject() {

        return saveProject(
                "Altes Projekt",
                "Alter Ansprechpartner",
                1L,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2027, 3, 31)
        );
    }

    // Hilfsmethode zum Erstellen eines Projekts mit individuellen Daten
    private ProjectEntity saveProject(
            String name,
            String customerContact,
            Long projectLeaderId,
            LocalDate startDate,
            LocalDate endDate) {

        ProjectEntity project = new ProjectEntity();

        project.setName(name);
        project.setCustomerId(100L);
        project.setCustomerContact(customerContact);
        project.setProjectLeaderId(projectLeaderId);
        project.setComment("Altes Projektziel");
        project.setStartDate(startDate);
        project.setEndDate(endDate);

        return projectRepository.save(project);
    }

    // Hilfsmethode für JSON-Requests
    private String projectJson(
            String name,
            Long customerId,
            Long projectLeaderId,
            String customerContact,
            String comment,
            String startDate,
            String endDate) {

        return """
                {
                    "name": "%s",
                    "customerId": %d,
                    "customerContact": "%s",
                    "projectLeaderId": %d,
                    "comment": "%s",
                    "startDate": "%s",
                    "endDate": "%s"
                }
                """.formatted(
                name,
                customerId,
                customerContact,
                projectLeaderId,
                comment,
                startDate,
                endDate
        );
    }

    // Prüft, ob ein Projekt nach einem fehlgeschlagenen Update unverändert ist
    private void assertProjectUnchanged(ProjectEntity original) {

        ProjectEntity unchanged = projectRepository.findById(original.getId())
                .orElseThrow();

        assertThat(unchanged.getName()).isEqualTo("Altes Projekt");
        assertThat(unchanged.getCustomerId()).isEqualTo(100L);
        assertThat(unchanged.getCustomerContact())
                .isEqualTo("Alter Ansprechpartner");
        assertThat(unchanged.getProjectLeaderId()).isEqualTo(1L);
        assertThat(unchanged.getComment()).isEqualTo("Altes Projektziel");
        assertThat(unchanged.getStartDate())
                .isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(unchanged.getEndDate())
                .isEqualTo(LocalDate.of(2027, 3, 31));
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

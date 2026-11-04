package de.szut.pms.project;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import org.springframework.security.test.context.support.WithMockUser;

import de.szut.pms.projectemployee.ProjectEmployeeEntity;
import de.szut.pms.testcontainers.AbstractIntegrationTest;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GetProjectByIdIntegrationTest extends AbstractIntegrationTest {

    @Test
    @WithMockUser
    void getExistingProjectReturnsAllData() throws Exception {

        // 1. Projekt vorbereiten
        ProjectEntity project = saveProject();

        // 2. Projektmitarbeiter zuweisen
        saveAssignment(project, 5L, "Java");
        saveAssignment(project, 8L, "Testing");

        // 3. Projekt über die REST-Schnittstelle abrufen
        mockMvc.perform(get("/projects/{id}", project.getId()))

                // 4. HTTP-Status überprüfen
                .andExpect(status().isOk())

                // 5. Projektdaten überprüfen
                .andExpect(jsonPath("$.id").value(project.getId()))
                .andExpect(jsonPath("$.name").value("Backoffice-Erweiterung"))
                .andExpect(jsonPath("$.customerId").value(100))
                .andExpect(jsonPath("$.customerContact")
                        .value("Max Mustermann"))
                .andExpect(jsonPath("$.projectLeaderId").value(1))
                .andExpect(jsonPath("$.comment")
                        .value("Erweiterung der Projektverwaltung"))
                .andExpect(jsonPath("$.startDate").value("2026-11-01"))
                .andExpect(jsonPath("$.endDate").value("2027-03-31"))

                // 6. Projektmitarbeiter überprüfen
                .andExpect(jsonPath("$.projectEmployees.length()").value(2))
                .andExpect(jsonPath("$.projectEmployees[*].employeeId",
                        containsInAnyOrder(5, 8)))
                .andExpect(jsonPath("$.projectEmployees[*].qualification",
                        containsInAnyOrder("Java", "Testing")));
    }

    @Test
    @WithMockUser
    void getUnknownProjectReturns404() throws Exception {

        Long unknownProjectId = 999999L;

        mockMvc.perform(get("/projects/{id}", unknownProjectId))

                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.title")
                        .value("Projekt nicht gefunden"))

                .andExpect(jsonPath("$.detail")
                        .value("Es existiert kein Projekt mit der ID "
                                + unknownProjectId + "."));
    }

    private ProjectEntity saveProject() {

        ProjectEntity project = new ProjectEntity();

        project.setName("Backoffice-Erweiterung");
        project.setCustomerId(100L);
        project.setCustomerContact("Max Mustermann");
        project.setProjectLeaderId(1L);
        project.setComment("Erweiterung der Projektverwaltung");
        project.setStartDate(LocalDate.of(2026, 11, 1));
        project.setEndDate(LocalDate.of(2027, 3, 31));

        return projectRepository.save(project);
    }

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
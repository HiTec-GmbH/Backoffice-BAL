package de.szut.pms.projectemployee;

import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import de.szut.pms.project.ProjectEntity;
import de.szut.pms.project.ProjectRepository;
import de.szut.pms.testcontainers.AbstractIntegrationTest;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GetProjectEmployeesIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectEmployeeRepository projectEmployeeRepository;

    @BeforeEach
    void prepare() {
        cleanUp();
    }

    // Projektmitarbeiter zuerst löschen, sonst verhindert der Fremdschlüssel das Löschen der Projekte
    @AfterEach
    void cleanUp() {
        projectEmployeeRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    @WithMockUser
    void getEmployeesOfProject() throws Exception { // A_1
        ProjectEntity project = saveProject("A", "2027-01-01", "2027-03-31");
        saveAssignment(project, 5L, "Java");
        saveAssignment(project, 8L, "Test");

        mockMvc.perform(get("/projects/{id}/employees", project.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(project.getId()))
                .andExpect(jsonPath("$.projectName").value("A"))
                .andExpect(jsonPath("$.employees.length()").value(2))
                .andExpect(jsonPath("$.employees[*].employeeId", containsInAnyOrder(5, 8)))
                .andExpect(jsonPath("$.employees[*].qualification", containsInAnyOrder("Java", "Test")));
    }

    @Test
    @WithMockUser
    void getEmployeesOfProjectWithoutEmployees() throws Exception { // A_2
        ProjectEntity project = saveProject("A", "2027-01-01", "2027-03-31");

        mockMvc.perform(get("/projects/{id}/employees", project.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(project.getId()))
                .andExpect(jsonPath("$.employees").isEmpty());
    }

    @Test
    @WithMockUser
    void getEmployeesOfUnknownProjectReturns404() throws Exception { // A_3
        mockMvc.perform(get("/projects/{id}/employees", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getEmployeesWithoutAuthenticationReturns401() throws Exception {
        mockMvc.perform(get("/projects/{id}/employees", 1L))
                .andExpect(status().isUnauthorized());
    }

    private ProjectEntity saveProject(String name, String start, String end) {
        ProjectEntity project = new ProjectEntity();
        project.setName(name);
        project.setCustomerId(1L);
        project.setCustomerContact("Erika Musterfrau");
        project.setProjectLeaderId(1L);
        project.setComment("Testprojekt");
        project.setStartDate(LocalDate.parse(start));
        project.setEndDate(LocalDate.parse(end));
        return projectRepository.save(project);
    }

    private void saveAssignment(ProjectEntity project, Long employeeId, String qualification) {
        ProjectEmployeeEntity assignment = new ProjectEmployeeEntity(qualification, employeeId);
        assignment.setProject(project);
        projectEmployeeRepository.save(assignment);
    }
}

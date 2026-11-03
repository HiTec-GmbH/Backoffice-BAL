package de.szut.pms.projectemployee;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import de.szut.pms.employee.EmployeeClient;
import de.szut.pms.employee.EmployeeDto;
import de.szut.pms.employee.SkillDto;
import de.szut.pms.project.ProjectEntity;
import de.szut.pms.project.ProjectRepository;
import de.szut.pms.testcontainers.AbstractIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RemoveProjectEmployeeIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectEmployeeRepository projectEmployeeRepository;

    @MockitoBean
    private EmployeeClient employeeClient;

    private ProjectEntity projectA; // 01.01.–31.03.2027

    @BeforeEach
    void prepare() {
        cleanUp();
        projectA = saveProject("A", "2027-01-01", "2027-03-31");
    }

    // Projektmitarbeiter zuerst löschen, sonst verhindert der Fremdschlüssel das Löschen der Projekte
    @AfterEach
    void cleanUp() {
        projectEmployeeRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    @WithMockUser
    void removeEmployeeSuccessfully() throws Exception { // A_1
        saveAssignment(projectA, 5L, "Java");
        saveAssignment(projectA, 8L, "Test");

        mockMvc.perform(delete("/projects/{id}/employees/{employeeId}", projectA.getId(), 5L).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(projectA.getId()))
                .andExpect(jsonPath("$.projectName").value("A"))
                .andExpect(jsonPath("$.employees.length()").value(1))
                .andExpect(jsonPath("$.employees[0].employeeId").value(8));

        assertThat(projectEmployeeRepository.findAll()).hasSize(1);
    }

    @Test
    @WithMockUser
    void removedEmployeeCanBeAssignedToOtherProjectInSamePeriod() throws Exception { // A_2
        saveAssignment(projectA, 5L, "Java");
        ProjectEntity projectB = saveProject("B", "2027-01-01", "2027-03-31");
        when(employeeClient.findById(5L))
                .thenReturn(new EmployeeDto(5L, List.of(new SkillDto(1L, "Java"))));

        mockMvc.perform(delete("/projects/{id}/employees/{employeeId}", projectA.getId(), 5L).with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/projects/{id}/employees", projectB.getId())
                        .content("""
                                { "employeeId": 5, "qualification": "Java" }
                                """)
                        .contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employees[0].employeeId").value(5));
    }

    @Test
    @WithMockUser
    void removeFromUnknownProjectReturns404() throws Exception { // A_3
        mockMvc.perform(delete("/projects/{id}/employees/{employeeId}", 9999L, 5L).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void removeEmployeeNotAssignedReturns404() throws Exception { // A_4
        saveAssignment(projectA, 8L, "Test");

        mockMvc.perform(delete("/projects/{id}/employees/{employeeId}", projectA.getId(), 5L).with(csrf()))
                .andExpect(status().isNotFound());

        assertThat(projectEmployeeRepository.findAll()).hasSize(1);
    }

    @Test
    void removeWithoutAuthenticationReturns401() throws Exception {
        mockMvc.perform(delete("/projects/{id}/employees/{employeeId}", projectA.getId(), 5L).with(csrf()))
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

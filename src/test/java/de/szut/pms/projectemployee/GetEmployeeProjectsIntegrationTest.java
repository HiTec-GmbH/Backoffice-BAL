package de.szut.pms.projectemployee;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import de.szut.pms.employee.EmployeeClient;
import de.szut.pms.employee.EmployeeDto;
import de.szut.pms.employee.EmployeeNotFoundException;
import de.szut.pms.employee.SkillDto;
import de.szut.pms.project.ProjectEntity;
import de.szut.pms.testcontainers.AbstractIntegrationTest;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GetEmployeeProjectsIntegrationTest extends AbstractIntegrationTest {

    @MockitoBean
    private EmployeeClient employeeClient;

    @BeforeEach
    void prepare() {
        when(employeeClient.findById(5L))
                .thenReturn(new EmployeeDto(5L, List.of(new SkillDto(1L, "Java"), new SkillDto(2L, "Test"))));
    }

    @Test
    @WithMockUser
    void getProjectsOfEmployee() throws Exception { // A_1
        ProjectEntity projectA = saveProject("A", "2027-01-01", "2027-03-31");
        ProjectEntity projectB = saveProject("B", "2027-04-01", "2027-06-30");
        saveAssignment(projectA, 5L, "Java");
        saveAssignment(projectB, 5L, "Test");

        mockMvc.perform(get("/employees/{id}/projects", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(5))
                .andExpect(jsonPath("$.projects.length()").value(2))
                .andExpect(jsonPath("$.projects[*].projectName", containsInAnyOrder("A", "B")))
                .andExpect(jsonPath("$.projects[*].startDate", containsInAnyOrder("2027-01-01", "2027-04-01")))
                .andExpect(jsonPath("$.projects[*].endDate", containsInAnyOrder("2027-03-31", "2027-06-30")))
                .andExpect(jsonPath("$.projects[*].qualification", containsInAnyOrder("Java", "Test")));
    }

    @Test
    @WithMockUser
    void getProjectsOfEmployeeWithoutProjects() throws Exception { // A_2
        mockMvc.perform(get("/employees/{id}/projects", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(5))
                .andExpect(jsonPath("$.projects").isEmpty());
    }

    @Test
    @WithMockUser
    void getProjectsOfUnknownEmployeeReturns404() throws Exception { // A_3
        when(employeeClient.findById(99L)).thenThrow(new EmployeeNotFoundException(99L));

        mockMvc.perform(get("/employees/{id}/projects", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProjectsWithoutAuthenticationReturns401() throws Exception {
        mockMvc.perform(get("/employees/{id}/projects", 5L))
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

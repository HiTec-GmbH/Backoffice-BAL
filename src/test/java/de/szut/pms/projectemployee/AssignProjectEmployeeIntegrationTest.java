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
import de.szut.pms.employee.EmployeeNotFoundException;
import de.szut.pms.employee.SkillDto;
import de.szut.pms.project.ProjectEntity;
import de.szut.pms.project.ProjectRepository;
import de.szut.pms.testcontainers.AbstractIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AssignProjectEmployeeIntegrationTest extends AbstractIntegrationTest {

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
        when(employeeClient.findById(5L))
                .thenReturn(new EmployeeDto(5L, List.of(new SkillDto(1L, "Java"))));
    }

    // Projektmitarbeiter zuerst löschen, sonst verhindert der Fremdschlüssel das Löschen der Projekte
    @AfterEach
    void cleanUp() {
        projectEmployeeRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    @WithMockUser
    void assignEmployeeSuccessfully() throws Exception { // A_1
        mockMvc.perform(post("/projects/{id}/employees", projectA.getId())
                        .content(body(5L, "java")).contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectId").value(projectA.getId()))
                .andExpect(jsonPath("$.projectName").value("A"))
                .andExpect(jsonPath("$.employees[0].employeeId").value(5))
                .andExpect(jsonPath("$.employees[0].qualification").value("Java"));

        assertThat(projectEmployeeRepository.findAll()).hasSize(1);
    }

    @Test
    @WithMockUser
    void assignToUnknownProjectReturns404() throws Exception { // A_2
        mockMvc.perform(post("/projects/{id}/employees", 9999L)
                        .content(body(5L, "Java")).contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isNotFound());

        assertThat(projectEmployeeRepository.findAll()).isEmpty();
    }

    @Test
    @WithMockUser
    void assignUnknownEmployeeReturns404() throws Exception { // A_3
        when(employeeClient.findById(99L)).thenThrow(new EmployeeNotFoundException(99L));

        mockMvc.perform(post("/projects/{id}/employees", projectA.getId())
                        .content(body(99L, "Java")).contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isNotFound());

        assertThat(projectEmployeeRepository.findAll()).isEmpty();
    }

    @Test
    @WithMockUser
    void assignWithoutQualificationReturns400() throws Exception { // A_4
        mockMvc.perform(post("/projects/{id}/employees", projectA.getId())
                        .content(body(5L, "Python")).contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isBadRequest());

        assertThat(projectEmployeeRepository.findAll()).isEmpty();
    }

    @Test
    @WithMockUser
    void assignWithOverlappingPeriodReturns409() throws Exception { // A_5
        saveAssignment(projectA, 5L, "Java");
        ProjectEntity projectB = saveProject("B", "2027-03-01", "2027-06-30");

        mockMvc.perform(post("/projects/{id}/employees", projectB.getId())
                        .content(body(5L, "Java")).contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", containsString("A")));

        assertThat(projectEmployeeRepository.findAll()).hasSize(1);
    }

    @Test
    @WithMockUser
    void assignWithAdjacentPeriodSucceeds() throws Exception { // A_6
        saveAssignment(projectA, 5L, "Java");
        ProjectEntity projectC = saveProject("C", "2027-04-01", "2027-06-30");

        mockMvc.perform(post("/projects/{id}/employees", projectC.getId())
                        .content(body(5L, "Java")).contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isCreated());

        assertThat(projectEmployeeRepository.findAll()).hasSize(2);
    }

    @Test
    @WithMockUser
    void assignTwiceToSameProjectReturns409() throws Exception { // A_7
        saveAssignment(projectA, 5L, "Java");

        mockMvc.perform(post("/projects/{id}/employees", projectA.getId())
                        .content(body(5L, "Java")).contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isConflict());

        assertThat(projectEmployeeRepository.findAll()).hasSize(1);
    }

    @Test
    void assignWithoutAuthenticationReturns401() throws Exception {
        mockMvc.perform(post("/projects/{id}/employees", projectA.getId())
                        .content(body(5L, "Java")).contentType(MediaType.APPLICATION_JSON).with(csrf()))
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

    private String body(Long employeeId, String qualification) {
        return """
                { "employeeId": %d, "qualification": "%s" }
                """.formatted(employeeId, qualification);
    }
}
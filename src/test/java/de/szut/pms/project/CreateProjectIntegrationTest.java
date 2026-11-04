package de.szut.pms.project;

import de.szut.pms.customer.CustomerClient;
import de.szut.pms.employee.EmployeeClient;
import de.szut.pms.employee.EmployeeDto;
import de.szut.pms.employee.EmployeeNotFoundException;
import de.szut.pms.testcontainers.AbstractIntegrationTest;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CreateProjectIntegrationTest extends AbstractIntegrationTest {

    @MockitoBean
    private EmployeeClient employeeClient;

    @MockitoBean
    private CustomerClient customerClient;

    @BeforeEach
    void prepareTest() {
        when(employeeClient.findById(1L)).thenReturn(new EmployeeDto(1L, List.of()));
        when(customerClient.existsById(100L)).thenReturn(true);
    }

    @Test
    @WithMockUser
    void createProjectSuccessfully() throws Exception {

        String content = projectJson(
                100L,
                1L,
                "2026-11-01",
                "2027-03-31"
        );

        mockMvc.perform(post("/projects")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name")
                        .value("Backoffice-Erweiterung"))
                .andExpect(jsonPath("$.customerId").value(100))
                .andExpect(jsonPath("$.projectLeaderId").value(1))
                .andExpect(jsonPath("$.startDate").value("2026-11-01"))
                .andExpect(jsonPath("$.endDate").value("2027-03-31"));

        assertThat(projectRepository.findAll()).hasSize(1);
    }

    @Test
    @WithMockUser
    void createProjectWithoutRequiredNameReturns400() throws Exception {

        String content = """
            {
                "customerId": 100,
                "customerContact": "Max Mustermann",
                "projectLeaderId": 1,
                "comment": "Erweiterung der Projektverwaltung",
                "startDate": "2026-11-01",
                "endDate": "2027-03-31"
            }
            """;

        mockMvc.perform(post("/projects")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());

        assertThat(projectRepository.findAll()).isEmpty();
    }

    @Test
    @WithMockUser
    void createProjectWithInvalidDateRangeReturns400() throws Exception {

        String content = projectJson(
                100L,
                1L,
                "2027-03-31",
                "2026-11-01"
        );

        mockMvc.perform(post("/projects")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.endDate").exists());

        assertThat(projectRepository.findAll()).isEmpty();
    }

    @Test
    @WithMockUser
    void createProjectWithUnknownEmployeeReturns404() throws Exception {

        when(employeeClient.findById(999L)).thenThrow(new EmployeeNotFoundException(999L));

        String content = projectJson(
                100L,
                999L,
                "2026-11-01",
                "2027-03-31"
        );

        mockMvc.perform(post("/projects")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail")
                        .value("Es existiert kein Mitarbeiter mit der ID 999."));

        assertThat(projectRepository.findAll()).isEmpty();
    }

    @Test
    @WithMockUser
    void createProjectWithUnknownCustomerReturns404() throws Exception {

        when(customerClient.existsById(999L)).thenReturn(false);

        String content = projectJson(
                999L,
                1L,
                "2026-11-01",
                "2027-03-31"
        );

        mockMvc.perform(post("/projects")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail")
                        .value("Es existiert kein Kunde mit der ID 999."));

        assertThat(projectRepository.findAll()).isEmpty();
    }

    private String projectJson(
            Long customerId,
            Long projectLeaderId,
            String startDate,
            String endDate) {

        return """
                {
                    "name": "Backoffice-Erweiterung",
                    "customerId": %d,
                    "customerContact": "Max Mustermann",
                    "projectLeaderId": %d,
                    "comment": "Erweiterung der Projektverwaltung",
                    "startDate": "%s",
                    "endDate": "%s"
                }
                """.formatted(
                customerId,
                projectLeaderId,
                startDate,
                endDate
        );
    }
}
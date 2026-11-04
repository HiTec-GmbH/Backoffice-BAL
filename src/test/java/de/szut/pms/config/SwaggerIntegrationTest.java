package de.szut.pms.config;

import org.junit.jupiter.api.Test;

import de.szut.pms.testcontainers.AbstractIntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SwaggerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void swaggerUiIsReachableWithoutToken() throws Exception { // A_1
        mockMvc.perform(get("/swagger"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void apiDocsListAllEndpointsWithoutToken() throws Exception { // A_1
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/projects']").exists())
                .andExpect(jsonPath("$.paths['/projects/{id}']").exists())
                .andExpect(jsonPath("$.paths['/projects/{projectId}/employees']").exists())
                .andExpect(jsonPath("$.paths['/projects/{projectId}/employees/{employeeId}']").exists())
                .andExpect(jsonPath("$.paths['/employees/{employeeId}/projects']").exists());
    }

    @Test
    void apiDocsDescribeStatusCodes() throws Exception { // A_2
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/projects/{projectId}/employees'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/projects/{projectId}/employees'].post.responses['409']").exists())
                .andExpect(jsonPath("$.paths['/projects/{id}'].put.responses['409']").exists())
                .andExpect(jsonPath("$.paths['/projects/{id}'].delete.responses['204']").exists());
    }

    @Test
    void apiDocsOfferBearerTokenInput() throws Exception { // A_3
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
    }
}

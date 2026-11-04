package de.szut.pms.security;

import org.junit.jupiter.api.Test;

import de.szut.pms.testcontainers.AbstractIntegrationTest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticationIntegrationTest extends AbstractIntegrationTest {

    @Test
    void requestWithValidJwtReturnsData() throws Exception { // A_1
        mockMvc.perform(get("/projects").with(jwt()))
                .andExpect(status().isOk());
    }

    @Test
    void requestWithoutTokenReturns401() throws Exception { // A_2
        mockMvc.perform(get("/projects"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/projects/{id}/employees", 1L))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/employees/{id}/projects", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestWithInvalidTokenReturns401() throws Exception { // A_3
        mockMvc.perform(get("/projects").header("Authorization", "Bearer ungueltig"))
                .andExpect(status().isUnauthorized());
    }
}

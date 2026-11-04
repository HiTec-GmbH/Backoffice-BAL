package de.szut.pms.testcontainers;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import de.szut.pms.project.ProjectRepository;
import de.szut.pms.projectemployee.ProjectEmployeeRepository;

/**
 * Startet einen echten Postgres-Container über Testcontainers und die volle
 * Anwendung. Zum Authentifizieren in Tests nicht Authentik ansprechen —
 * dafür gibt es {@code @WithMockUser}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = PostgresContextInitializer.class)
public class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ProjectRepository projectRepository;

    @Autowired
    protected ProjectEmployeeRepository projectEmployeeRepository;

    // Projektmitarbeiter zuerst löschen, sonst verhindert der Fremdschlüssel das Löschen der Projekte
    @BeforeEach
    void cleanDatabase() {
        projectEmployeeRepository.deleteAll();
        projectRepository.deleteAll();
    }
}

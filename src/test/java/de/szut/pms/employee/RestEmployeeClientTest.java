package de.szut.pms.employee;

import java.net.URI;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RestEmployeeClientTest {

    private final RestEmployeeClient client = new RestEmployeeClient();

    @BeforeEach
    void prepare() {
        // Port 1 ist nicht belegt, die Verbindung schlägt also sofort fehl
        ReflectionTestUtils.setField(client, "apiUrl", URI.create("http://localhost:1"));

        Jwt jwt = Jwt.withTokenValue("test-token").header("alg", "none").claim("sub", "john").build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void unreachableEmployeeServiceThrowsServiceUnavailable() {
        assertThatThrownBy(() -> client.findById(5L))
                .isInstanceOf(EmployeeServiceUnavailableException.class);
    }
}

package de.szut.pms.employee;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Component
public class RestEmployeeClient implements EmployeeClient{

    private final RestClient restClient = RestClient.create();

    @Value("${employee-service.base-url}")
    private URI apiUrl;

    @Override 
    public EmployeeDto findById(Long employeeId) {
        try {
            return restClient.get()
                    .uri(apiUrl + "/employees/{id}", employeeId)
                    .headers(h -> h.setBearerAuth(currentToken()))
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (request, response) -> {
                        throw new EmployeeNotFoundException(employeeId);
                    })
                    .body(EmployeeDto.class);
        } catch (ResourceAccessException ex) {
            // Keine Verbindung (Service aus, Timeout): eigene Exception, damit der Client 503 statt 500 bekommt
            throw new EmployeeServiceUnavailableException(ex);
        }
    }
    
    // Token Relay: Der Employee-Service ist ebenfalls abgesichert und akzeptiert dank
    // Single Sign-on denselben Token, deshalb wird kein neuer angefordert.
    private String currentToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getTokenValue();
        }
        throw new IllegalStateException("Kein Bearer-Token in der aktuellen Anfrage.");
    }
    
}

package de.szut.pms.employee;

import org.springframework.stereotype.Component;

@Component
public class RestEmployeeClient implements EmployeeClient {

    @Override
    public boolean existsById(Long employeeId) {
        // TODO: Employee-Service über REST aufrufen
        // TODO: Bearer Token weitergeben
        return employeeId != null;
    }
}
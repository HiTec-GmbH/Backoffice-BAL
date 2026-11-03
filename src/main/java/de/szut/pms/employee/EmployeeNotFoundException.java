package de.szut.pms.employee;

public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(Long employeeId) {
        super("Es existiert kein Mitarbeiter mit der ID " + employeeId + ".");
    }
}

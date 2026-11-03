package de.szut.pms.employee;

public class EmployeeAlreadyAssignedException extends RuntimeException {
    public EmployeeAlreadyAssignedException(Long employeeId, Long projectId) {
        super("Der Mitarbeiter " + employeeId + " ist dem Projekt " + projectId + " bereits zugeordnet.");
    }
}
package de.szut.pms.projectemployee;

public class ProjectEmployeeNotFoundException extends RuntimeException {

    public ProjectEmployeeNotFoundException(Long employeeId, Long projectId) {
        super("Der Mitarbeiter " + employeeId + " ist dem Projekt " + projectId + " nicht zugeordnet.");
    }
}

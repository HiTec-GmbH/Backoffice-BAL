package de.szut.pms.employee;

import de.szut.pms.project.ProjectEntity;

public class EmployeeOverlapException extends RuntimeException {
    public EmployeeOverlapException(Long employeeId, ProjectEntity other) {
        super("Der Mitarbeiter " + employeeId + " ist im Zeitraum bereits dem Projekt "
                + other.getId() + " (" + other.getName() + ", "
                + other.getStartDate() + " bis " + other.getEndDate() + ") zugeordnet.");
    }
}

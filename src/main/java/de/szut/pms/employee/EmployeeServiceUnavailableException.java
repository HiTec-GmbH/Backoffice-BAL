package de.szut.pms.employee;

public class EmployeeServiceUnavailableException extends RuntimeException {

    public EmployeeServiceUnavailableException(Throwable cause) {
        super("Der Employee-Service ist nicht erreichbar.", cause);
    }
}

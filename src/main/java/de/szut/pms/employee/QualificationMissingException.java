package de.szut.pms.employee;

public class QualificationMissingException extends RuntimeException {

    public QualificationMissingException(Long employeeId, String qualification) {
        super("Der Mitarbeiter " + employeeId + " besitzt die Qualifikation \"" + qualification + "\" nicht.");
    }
}
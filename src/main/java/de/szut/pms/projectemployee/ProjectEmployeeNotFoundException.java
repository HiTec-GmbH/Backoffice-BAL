package de.szut.pms.projectemployee;

public class ProjectEmployeeNotFoundException extends RuntimeException {

    public ProjectEmployeeNotFoundException(Long id) {
        super("Es gibt keinen Eintrag mit der Kennung " + id + ".");
    }
}

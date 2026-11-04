package de.szut.pms.project;

public class InvalidProjectException extends RuntimeException {

    private final String field;

    public InvalidProjectException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
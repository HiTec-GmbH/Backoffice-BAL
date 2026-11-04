package de.szut.pms.common;

import java.util.LinkedHashMap;
import java.util.Map;

import de.szut.pms.customer.CustomerNotFoundException;
import de.szut.pms.employee.EmployeeAlreadyAssignedException;
import de.szut.pms.employee.EmployeeNotFoundException;
import de.szut.pms.employee.EmployeeOverlapException;
import de.szut.pms.project.InvalidProjectException;
import de.szut.pms.project.ProjectNotFoundException;
import de.szut.pms.employee.QualificationMissingException;
import de.szut.pms.projectemployee.ProjectEmployeeNotFoundException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Zentrale Übersetzung von Ausnahmen in HTTP-Antworten nach RFC 9457
 * (ProblemDetail). Eine Methode je Ausnahmetyp, wie in Tutorial 4, AB 03.
 * <p>
 * Ergänze hier eigene {@code @ExceptionHandler}-Methoden für die fachlichen
 * Ausnahmen deiner Ressourcen — nach demselben Muster wie
 * {@link #handleNotFound}.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

    @ExceptionHandler(ProjectEmployeeNotFoundException.class)
    public ProblemDetail handleNotFound(ProjectEmployeeNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Nicht gefunden", ex.getMessage());
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ProblemDetail handleNotFound(EmployeeNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Nicht gefunden", ex.getMessage());
    }

    @ExceptionHandler(QualificationMissingException.class)
    public ProblemDetail handleNotFound(QualificationMissingException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Qualifikation fehlt", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(
                error -> errors.put(error.getField(), error.getDefaultMessage()));

        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Ungültige Eingabe",
                "Die Anfrage enthält ungültige Felder.");
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ProblemDetail handleCustomerNotFound(CustomerNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Kunde nicht gefunden", ex.getMessage());
    }

    @ExceptionHandler(InvalidProjectException.class)
    public ProblemDetail handleInvalidProject(InvalidProjectException ex) {
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Ungültige Projektdaten",
                "Das Projekt enthält ungültige Angaben.");
        problem.setProperty("errors", Map.of(ex.getField(), ex.getMessage()));
        return problem;
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ProblemDetail handleProjectNotFound(ProjectNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Projekt nicht gefunden", ex.getMessage());
    }

    @ExceptionHandler(EmployeeAlreadyAssignedException.class)
    public ProblemDetail handleEmployeeAlreadyAssigned(EmployeeAlreadyAssignedException ex) {
        return problem(HttpStatus.CONFLICT, "Mitarbeiter bereits zugeordnet", ex.getMessage());
    }

    @ExceptionHandler(EmployeeOverlapException.class)
    public ProblemDetail handleEmployeeOverlap(EmployeeOverlapException ex) {
        return problem(HttpStatus.CONFLICT, "Zeitliche Überschneidung", ex.getMessage());
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}

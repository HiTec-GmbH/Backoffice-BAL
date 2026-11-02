package de.szut.pms.common;

import java.util.LinkedHashMap;
import java.util.Map;

import de.szut.pms.customer.CustomerNotFoundException;
import de.szut.pms.employee.EmployeeNotFoundException;
import de.szut.pms.project.InvalidProjectException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import de.szut.pms.hello.HelloNotFoundException;

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

    @ExceptionHandler(HelloNotFoundException.class)
    public ProblemDetail handleNotFound(HelloNotFoundException ex) {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Nicht gefunden");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(
                error -> errors.put(error.getField(), error.getDefaultMessage()));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Die Anfrage enthält ungültige Felder.");
        problem.setTitle("Ungültige Eingabe");
        problem.setProperty("errors", errors);
        return problem;
    }

    // Mitarbeiter nicht gefunden
    @ExceptionHandler(EmployeeNotFoundException.class)
    public ProblemDetail handleEmployeeNotFound(
            EmployeeNotFoundException ex) {

        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.NOT_FOUND,
                        ex.getMessage()
                );

        problem.setTitle("Mitarbeiter nicht gefunden");

        return problem;
    }

    // Kunde nicht gefunden
    @ExceptionHandler(CustomerNotFoundException.class)
    public ProblemDetail handleCustomerNotFound(
            CustomerNotFoundException ex) {

        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.NOT_FOUND,
                        ex.getMessage()
                );

        problem.setTitle("Kunde nicht gefunden");

        return problem;
    }

    // Ungültige Projektdaten
    @ExceptionHandler(InvalidProjectException.class)
    public ProblemDetail handleInvalidProject(
            InvalidProjectException ex) {

        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        "Das Projekt enthält ungültige Angaben."
                );

        problem.setTitle("Ungültige Projektdaten");

        problem.setProperty(
                "errors",
                Map.of(ex.getField(), ex.getMessage())
        );

        return problem;
    }
}

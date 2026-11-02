package de.szut.pms.customer;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long customerId) {
        super("Es existiert kein Kunde mit der ID "
                + customerId + ".");
    }
}
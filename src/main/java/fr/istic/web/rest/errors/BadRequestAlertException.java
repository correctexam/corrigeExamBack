package fr.istic.web.rest.errors;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class BadRequestAlertException extends WebApplicationException {
    private static final long serialVersionUID = 1L;

    public BadRequestAlertException(String message, String entityName, String errorKey) {
        super(Response.status(BAD_REQUEST).entity(message).header("message", "error." + errorKey).header("params", entityName).build());
    }
}

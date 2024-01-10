package fr.istic.web.rest.errors;

import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class UserNotAuthenticatedException extends WebApplicationException {
    private static final long serialVersionUID = 1L;

    public UserNotAuthenticatedException() {
        this("Authentication is required");
    }

    public UserNotAuthenticatedException(String message) {
        super(Response.status(UNAUTHORIZED).entity(message).entity(message).build());
    }
}

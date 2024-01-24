package fr.istic.web.rest.errors;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class EmailNotFoundException extends WebApplicationException {

    public EmailNotFoundException() {
        super(Response.status(BAD_REQUEST).entity("Email address not registered").build());
    }
}

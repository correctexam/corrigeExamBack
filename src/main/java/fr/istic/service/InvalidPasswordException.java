package fr.istic.service;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class InvalidPasswordException extends WebApplicationException {

    public InvalidPasswordException() {
        super(Response.status(BAD_REQUEST).entity("Incorrect Password").build());
    }
}

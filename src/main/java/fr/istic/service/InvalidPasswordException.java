package fr.istic.service;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class InvalidPasswordException extends WebApplicationException {

    public InvalidPasswordException() {
        super(Response.status(BAD_REQUEST).entity("Incorrect Password").build());
    }
}

package fr.istic.security;

import jakarta.ws.rs.NotAuthorizedException;

public class UsernameNotFoundException extends NotAuthorizedException {

    public UsernameNotFoundException(String message) {
        super(message);
    }
}

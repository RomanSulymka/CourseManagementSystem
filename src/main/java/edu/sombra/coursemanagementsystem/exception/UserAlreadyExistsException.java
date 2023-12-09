package edu.sombra.coursemanagementsystem.exception;

import lombok.Generated;

@Generated
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String email) {
        super("User with this email is already exist: " + email);
    }
}
